# Неблокирующее сетевое взаимодействие в Java

Высокоуровневый `HttpClient` скрывает механику сокетов. Под ней — три модели сетевого ввода-вывода, различающиеся тем,
как поток ждёт готовности данных: блокирующая, неблокирующая с мультиплексированием (`Selector`) и асинхронная
(`AsynchronousSocketChannel`). Материал разбирает все три, устройство `Selector` под разными ОС и границы применимости.

## Содержание

1. Проблема: поток на соединение
2. Блокирующая модель
3. Неблокирующая модель и Selector
4. Selector под капотом: epoll / kqueue / poll
5. Асинхронная модель: AsynchronousSocketChannel
6. Где что применять
7. Как с этим связан HttpClient

---

## 1. Проблема: поток на соединение

В простейшей серверной модели на каждое соединение выделяется поток, который блокируется на `read` до прихода данных.
Пока соединений десятки — это нормально. На тысячах соединений модель разваливается: каждый поток стоит памяти (стек), а
переключение контекста между тысячами потоков съедает процессор. Это классическая «проблема C10K» (10 000 соединений).
Решение — отвязать число потоков от числа соединений.

---

## 2. Блокирующая модель

`SocketChannel` (или старый `Socket`) в режиме по умолчанию блокирует поток на операциях:

```java
void example(SocketChannel channel, ByteBuffer buffer) throws IOException {
    int read = channel.read(buffer);   // поток стоит здесь до прихода данных
}
```

Просто и понятно, естественно для клиента с одним-двумя соединениями (как `HttpClient.send`). Цена — один поток на
соединение; масштабирование ограничено числом потоков.

---

## 3. Неблокирующая модель и Selector

Канал переводится в неблокирующий режим (`configureBlocking(false)`): `read`/`write` возвращают управление сразу, даже
если данных нет (вернув 0). Чтобы не опрашивать каналы в пустом цикле, применяют мультиплексор `Selector`: каналы
регистрируются в нём на интересующие события (`OP_ACCEPT`, `OP_READ`, `OP_WRITE`), а один поток в цикле `select()` ждёт
готовности любого из них и обрабатывает только готовые.

```java
void example() throws IOException {
    Selector selector = Selector.open();
    ServerSocketChannel server = ServerSocketChannel.open();
    server.bind(new InetSocketAddress(8080));
    server.configureBlocking(false);
    server.register(selector, SelectionKey.OP_ACCEPT);

    while (true) {
        selector.select();                                   // ждём готовности любого канала
        Iterator<SelectionKey> it = selector.selectedKeys().iterator();
        while (it.hasNext()) {
            SelectionKey key = it.next();
            it.remove();                                     // ключ обязательно убираем из набора
            if (key.isAcceptable()) {
                SocketChannel client = server.accept();
                client.configureBlocking(false);
                client.register(selector, SelectionKey.OP_READ);
            } else if (key.isReadable()) {
                SocketChannel client = (SocketChannel) key.channel();
                ByteBuffer buffer = ByteBuffer.allocate(1024);
                int read = client.read(buffer);
                if (read == -1) {
                    client.close();
                }
                // ... обработка прочитанного
            }
        }
    }
}
```

Один поток обслуживает тысячи соединений: работа идёт только по готовым каналам, а не по каждому в отдельном потоке.
Плата — существенно более сложный код с явным управлением состоянием каждого соединения.

---

## 4. Selector под капотом: epoll / kqueue / poll

`Selector` — переносимая обёртка над системным механизмом уведомления о готовности файловых дескрипторов. Реализация
выбирается JDK по операционной системе:

- **`epoll`** (Linux). Ядро хранит зарегистрированный набор дескрипторов и возвращает только готовые; стоимость `select`
  почти не зависит от числа наблюдаемых дескрипторов. Масштабируется до десятков-сотен тысяч.
- **`kqueue`** (macOS, BSD). Аналогичный событийный механизм с регистрируемым набором и возвратом только готовых
  событий.
- **`poll` / `select`** (переносимый fallback, в т.ч. Windows-варианты). Ядру каждый раз передаётся весь список
  дескрипторов, и оно линейно проверяет их — стоимость растёт с числом соединений.

Ключевая разница: `epoll`/`kqueue` работают за время, близкое к числу *готовых* событий, а `poll`/`select` — к числу
*всех* наблюдаемых дескрипторов. Прикладной код видит один и тот же `Selector.select()`; JDK подставляет
`EPollSelectorImpl`, `KQueueSelectorImpl` и т.п.

---

## 5. Асинхронная модель: AsynchronousSocketChannel

Асинхронный ввод-вывод (NIO.2, `java.nio.channels.Asynchronous*`) убирает ручной цикл `select`: операция запускается и
возвращает управление немедленно, а её завершение доставляется через `Future` или колбэк `CompletionHandler`. Пулом
потоков, доставляющим завершения, управляет JDK (`AsynchronousChannelGroup`).

```java
void example() throws Exception {
    AsynchronousServerSocketChannel server = AsynchronousServerSocketChannel.open()
            .bind(new InetSocketAddress(8080));
    server.accept(null, new CompletionHandler<AsynchronousSocketChannel, Void>() {
        @Override
        public void completed(AsynchronousSocketChannel client, Void attachment) {
            server.accept(null, this);                       // принимаем следующее соединение
            ByteBuffer buffer = ByteBuffer.allocate(1024);
            client.read(buffer, buffer, new CompletionHandler<Integer, ByteBuffer>() {
                @Override public void completed(Integer read, ByteBuffer buf) {
                    buf.flip();
                    client.write(buf);                       // эхо
                }
                @Override public void failed(Throwable exc, ByteBuffer buf) { }
            });
        }
        @Override public void failed(Throwable exc, Void attachment) { }
    });
}
```

Отличие от неблокирующей модели: там поток сам опрашивает готовность через `select` и выполняет `read`/`write`; здесь
завершение операции доставляется колбэком, а цикл ожидания скрыт в реализации. Цена — колбэк-ориентированный код
(«callback hell» при вложенных операциях).

---

## 6. Где что применять

| Модель | Потоки | Сложность | Когда |
|---|---|---|---|
| Блокирующая | поток на соединение | низкая | клиент, малое число соединений (CLI, `HttpClient`) |
| Неблокирующая (`Selector`) | немного потоков на тысячи соединений | высокая | высоконагруженные серверы, максимальный контроль |
| Асинхронная (`Async*Channel`) | пул JDK | средняя | событийная модель без ручного `select` |

Выбор — не «какая лучше», а «какая цена под какую нагрузку». Блокирующая проще и подходит клиенту; неблокирующая и
асинхронная нужны там, где соединений тысячи, а потоков столько заводить нельзя.

---

## 7. Как с этим связан HttpClient

`java.net.http.HttpClient` внутри построен на неблокирующих каналах и `Selector`: пул соединений, HTTP/2-мультиплексирование
и `sendAsync` опираются на событийную модель, скрытую за высокоуровневым API. `send(...)` выглядит блокирующим, но под
ним — тот же неблокирующий слой. Поэтому в Market Pulse (клиент с редкими запросами) достаточно `HttpClient`, а понимание
`Selector`/async нужно, чтобы знать, что происходит под капотом, и уметь читать высоконагруженный сетевой код.
