# Selector и AsynchronousSocketChannel

---

## Три модели сетевого ввода-вывода

`HttpClient` из предыдущего этапа — высокоуровневая обёртка. Под прикладными API лежат три модели работы с сокетами,
различающиеся тем, как поток ждёт данные.

- **Блокирующая.** `read`/`write` блокируют поток до готовности данных. Один поток на соединение. Просто, но тысячи
  соединений — тысячи потоков.
- **Неблокирующая с мультиплексированием (`Selector`).** Один поток обслуживает много каналов, реагируя на события
  готовности. Масштабируется на десятки тысяч соединений.
- **Асинхронная (`AsynchronousSocketChannel`).** Операция запускается и возвращает управление сразу; завершение
  приходит через `Future` или колбэк `CompletionHandler`.

---

## Блокирующий SocketChannel

```java
void example(SocketChannel channel, ByteBuffer buffer) throws IOException {
    int read = channel.read(buffer);   // поток стоит здесь, пока не придут данные
}
```

Модель по умолчанию; так работает и `HttpClient.send`. Стоимость масштабирования — поток на соединение.

---

## Неблокирующий режим и Selector

Канал переводится в неблокирующий режим и регистрируется в `Selector` на интересующие события. Один поток в цикле ждёт
готовности любого из каналов и обрабатывает только готовые.

```java
void example() throws IOException {
    Selector selector = Selector.open();
    ServerSocketChannel server = ServerSocketChannel.open();
    server.bind(new InetSocketAddress(8080));
    server.configureBlocking(false);
    server.register(selector, SelectionKey.OP_ACCEPT);

    while (true) {
        selector.select();                       // блокируется, пока не готов хоть один канал
        Iterator<SelectionKey> keys = selector.selectedKeys().iterator();
        while (keys.hasNext()) {
            SelectionKey key = keys.next();
            keys.remove();
            if (key.isAcceptable()) {
                SocketChannel client = server.accept();
                client.configureBlocking(false);
                client.register(selector, SelectionKey.OP_READ);
            } else if (key.isReadable()) {
                // читаем из готового канала, не блокируя поток
            }
        }
    }
}
```

Один поток и один `select()` обслуживают все соединения — событийная модель вместо «поток на соединение».

---

## Selector под капотом: kqueue / epoll / poll

`Selector` — переносимая обёртка над системным механизмом уведомления о готовности дескрипторов:

- **`epoll`** — Linux; масштабируется до десятков тысяч дескрипторов, стоимость почти не растёт с их числом.
- **`kqueue`** — macOS/BSD; аналогичная событийная модель.
- **`poll`/`select`** — переносимый fallback; проще, но проверяет все дескрипторы линейно.

JDK сам выбирает реализацию под ОС (`EPollSelectorImpl`, `KQueueSelectorImpl`, …). Прикладной код видит один и тот же
`select()`; различается эффективность и внутреннее устройство.

---

## AsynchronousSocketChannel

Асинхронная модель: операция не блокирует и не требует ручного цикла `select` — завершение доставляется колбэком.

```java
void example() throws Exception {
    AsynchronousServerSocketChannel server = AsynchronousServerSocketChannel.open()
            .bind(new InetSocketAddress(8080));
    server.accept(null, new CompletionHandler<AsynchronousSocketChannel, Void>() {
        @Override
        public void completed(AsynchronousSocketChannel client, Void attachment) {
            server.accept(null, this);           // принимаем следующее соединение
            ByteBuffer buffer = ByteBuffer.allocate(1024);
            client.read(buffer, buffer, new CompletionHandler<Integer, ByteBuffer>() {
                @Override public void completed(Integer bytesRead, ByteBuffer buf) {
                    buf.flip();
                    client.write(buf);           // эхо: пишем прочитанное обратно
                }
                @Override public void failed(Throwable exc, ByteBuffer buf) { }
            });
        }
        @Override public void failed(Throwable exc, Void attachment) { }
    });
}
```

Пулом потоков, доставляющим завершения, управляет JDK (`AsynchronousChannelGroup`). Модель удобна для «выстрелил и
обрабатываю по готовности», но код становится колбэк-ориентированным.

---

## Что выбирать

- **Блокирующая** — для клиента и малого числа соединений (CLI, `HttpClient`): проще всего.
- **Неблокирующая с `Selector`** — для серверов с тысячами соединений на немногих потоках: максимальный контроль,
  сложнее код.
- **Асинхронная** — когда удобнее событийная/колбэк-модель без ручного цикла `select`.

Разница подходов — не «какой лучше», а «какая цена под какую нагрузку». Market Pulse — клиент с редкими запросами,
поэтому в проекте используется блокирующий `HttpClient`; эти модели важно понимать, чтобы знать, что скрыто под ним и
как устроены высоконагруженные сетевые приложения.
