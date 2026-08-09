# Executor Framework, Future и виртуальные потоки

Ручное управление потоками не масштабируется: поток дорог, а массив потоков с `join` — рутина. Executor Framework
отделяет задачи от исполнителя и переиспользует потоки; `CompletableFuture` строит асинхронные конвейеры; виртуальные
потоки снимают ограничение на число одновременных ожиданий. Материал разбирает всё три темы.

## Содержание

1. Executor и ExecutorService
2. Callable и Future
3. Пулы потоков
4. CompletableFuture
5. Обработка ошибок в конвейере
6. Виртуальные потоки (Project Loom)
7. I/O-bound против CPU-bound

---

## 1. Executor и ExecutorService

`Executor` — минимальная абстракция «выполнить задачу», скрывающая, где и как. `ExecutorService` расширяет её: отправка
задач с результатом, управление жизненным циклом, отслеживание. Идея — отделить **что** выполнить от **как**: один и тот
же код задач можно исполнить на разном числе потоков, не меняя сами задачи.

```java
void example() {
    try (ExecutorService pool = Executors.newFixedThreadPool(8)) {
        pool.submit(() -> doWork());
    }   // AutoCloseable (Java 21+): close() дожидается задач и гасит пул
}
```

`ExecutorService` реализует `AutoCloseable`, поэтому try-with-resources заменяет ручные `shutdown`/`awaitTermination`.

---

## 2. Callable и Future

`Runnable.run()` не возвращает значение и не бросает checked-исключения. `Callable<T>` умеет и то, и другое. `submit`
ставит задачу в пул и возвращает `Future<T>`:

```java
void example(ExecutorService pool) throws Exception {
    Future<Quote> future = pool.submit(() -> load("SBER"));
    Quote quote = future.get();          // блокирует до готовности
    boolean done = future.isDone();
    future.cancel(true);                 // попытка отмены (true — прервать, если уже выполняется)
}
```

`invokeAll(tasks)` запускает набор задач и возвращает список `Future` (удобно для «загрузить все тикеры»).

---

## 3. Пулы потоков

- `newFixedThreadPool(n)` — фиксированный пул; для CPU-bound задач `n` ≈ числу ядер.
- `newCachedThreadPool()` — растущий пул под всплески коротких задач.
- `newScheduledThreadPool(n)` — отложенные и периодические задачи.
- `newVirtualThreadPerTaskExecutor()` — виртуальный поток на задачу (Loom, раздел 6).

Почему пул, а не поток на задачу: создание потока дороже самой мелкой задачи, а число потоков ОС ограничено. Пул создаёт
потоки заранее и переиспользует.

---

## 4. CompletableFuture

`Future.get()` блокирует. `CompletableFuture` (Java 8+) описывает обработку результата по готовности:

```java
void example(ExecutorService executor, List<String> tickers) {
    List<CompletableFuture<Optional<Quote>>> futures = tickers.stream()
            .map(ticker -> CompletableFuture.supplyAsync(() -> load(ticker), executor))
            .toList();

    CompletableFuture.allOf(futures.toArray(CompletableFuture[]::new)).join();   // дождаться всех

    List<Quote> quotes = futures.stream()
            .map(CompletableFuture::join)     // результат уже готов
            .flatMap(Optional::stream)
            .toList();
}
```

Методы конвейера: `thenApply` (преобразовать), `thenAccept` (использовать), `thenCompose` (связать с другим CF),
`thenCombine` (объединить два), `allOf`/`anyOf` (дождаться всех/любого).

---

## 5. Обработка ошибок в конвейере

Исключения пропагируются вниз по конвейеру (от ранних стадий к поздним), не вверх. `exceptionally`/`handle` перехватывают
их:

```java
void example(ExecutorService executor, String ticker) {
    CompletableFuture.supplyAsync(() -> load(ticker), executor)
            .exceptionally(ex -> null);   // сбой одного тикера → null, а не падение всего пакета
}
```

Для `quotesFor` это ключевое свойство: одна неудачная загрузка не должна ронять весь запрос.

---

## 6. Виртуальные потоки (Project Loom)

Платформенный поток на блокирующем I/O простаивает, занимая поток ОС, поэтому пул ограничен. Виртуальные потоки (Java 21)
— лёгкие потоки, которых можно создавать миллионы: пока виртуальный поток ждёт I/O, он сходит с потока ОС и не занимает
его.

Так как задачи отделены от исполнителя, переход — замена фабрики:

```text
Executors.newFixedThreadPool(8)              // было: платформенные потоки, задачи в очереди
Executors.newVirtualThreadPerTaskExecutor()  // стало: виртуальный поток на задачу
```

Код задач не меняется. Не нужно подбирать размер пула — каждый тикер получает свой виртуальный поток. Писать можно
простой блокирующий код (`source.fetch(ticker)`), а эффективность получать как у неблокирующего I/O — рантайм сам снимает
поток на время ожидания.

---

## 7. I/O-bound против CPU-bound

Виртуальные потоки не ускоряют вычисления — они снимают ограничение на число одновременно **ждущих** задач:

- **I/O-bound** (много блокирующих сетевых/дисковых ожиданий — наш `quotesFor`): огромный выигрыш, тысячи одновременных
  ожиданий на горстке потоков ОС.
- **CPU-bound** (тяжёлые вычисления): выигрыша нет — считать нечем, кроме имеющихся ядер; нужен пул по числу ядер.

Правило: I/O-bound с высокой одновременностью → виртуальные потоки; CPU-bound → фиксированный пул по числу ядер. И
проверять выбор замером, а не интуицией.
