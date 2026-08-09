# Executor Framework

---

## Зачем

Раньше поток на тикер создавался вручную: `Thread[]` + `join`. Проблемы: поток — тяжёлый ресурс, создавать
его на каждую задачу расточительно; ручное управление массивом потоков и ожиданием — рутина с местом для ошибок.
Executor Framework отделяет **что** выполнить (задача) от **как** выполнить (пул потоков) и переиспользует потоки.

---

## Executor и ExecutorService

- **`Executor`** — минимальная абстракция: «выполнить задачу», скрывая, где и как.
- **`ExecutorService`** — расширение: отправка задач с результатом, управление жизненным циклом, отслеживание
  выполнения.

Пулы создаются фабриками `Executors`:

```java
void example() {
    ExecutorService pool = Executors.newFixedThreadPool(8);   // 8 переиспользуемых потоков
}
```

---

## Callable и Future

`Runnable` не возвращает значение и не может бросать checked-исключения. `Callable<T>` — может и то, и другое.
`submit(Callable)` ставит задачу в пул и сразу возвращает `Future<T>` — «расписку» на будущий результат:

```java
void example(ExecutorService pool) throws Exception {
    Future<Integer> future = pool.submit(() -> 40 + 2);   // Callable
    Integer result = future.get();                        // блокирует до готовности
    boolean done = future.isDone();
    future.cancel(true);                                  // попытка отмены
}
```

- `get()` — дождаться и получить результат (блокирует);
- `cancel(...)`, `isDone()`, `isCancelled()` — управление и статус.

---

## Закрытие пула

`ExecutorService` реализует `AutoCloseable` (Java 21+): `close()` в try-with-resources дожидается завершения задач и
гасит пул — вручную звать `shutdown`/`awaitTermination` больше не нужно.

```java
void example(List<String> tickers) {
    try (ExecutorService pool = Executors.newFixedThreadPool(8)) {
        List<Future<Quote>> futures = tickers.stream()
                .map(t -> pool.submit(() -> load(t)))
                .toList();
        // ... собрать futures
    }   // здесь пул дождался всех задач и закрылся
}
```

---

## Применение в проекте

`quotesFor` переводится с ручных потоков на пул: задачи (`Callable`, загрузка одного тикера) отправляются
в `ExecutorService`, результаты собираются через `Future`. Кеш (`ConcurrentHashMap`) и прогресс-счётчик
(`AtomicInteger`) остаются как есть — меняется только механизм запуска.

> Задачи отделены от исполнителя, поэтому позже пул можно заменить на виртуальные потоки, не трогая код задач.
