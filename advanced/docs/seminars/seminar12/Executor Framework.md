# Executor Framework

---

## Задача отделена от исполнителя

Ручное управление потоками (`Thread[]` + `join`) заменяется пулом: **что** выполнить (задача) отделено от **как**
выполнить (пул). `Executor` — «выполнить задачу»; `ExecutorService` — отправка задач с результатом и управление
жизненным циклом.

```java
void example(List<String> tickers) {
    try (ExecutorService pool = Executors.newFixedThreadPool(8)) {   // AutoCloseable (Java 21+)
        List<Future<Optional<Quote>>> futures = tickers.stream()
                .map(t -> pool.submit(() -> load(t)))   // Callable → Future
                .toList();
        List<Quote> quotes = futures.stream().map(Executor::get).flatMap(Optional::stream).toList();
    }   // close() дожидается задач и гасит пул
}
```

---

## Callable, Future

`Callable<T>` (в отличие от `Runnable`) возвращает значение и бросает checked-исключения. `submit` возвращает `Future`:
`get()` (блокирует), `cancel`, `isDone`, `isCancelled`. `invokeAll` запускает набор задач и возвращает список `Future`.

---

## Виды пулов

- `newFixedThreadPool(n)` — фиксированный пул (CPU-bound: n ≈ числу ядер).
- `newCachedThreadPool()` — растущий пул под всплески коротких задач.
- `newScheduledThreadPool(n)` — отложенные и периодические задачи.
- `newVirtualThreadPerTaskExecutor()` — виртуальный поток на задачу (Loom).

Отделение задач от исполнителя означает, что смена пула (в т.ч. на виртуальные потоки) не затрагивает код задач.
Внутренняя механика (`ThreadPoolExecutor`: очередь задач, core/max pool size, политика отказа) — в deep-dive.
