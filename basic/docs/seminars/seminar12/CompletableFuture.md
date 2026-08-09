# CompletableFuture

---

## Проблема обычного Future

`Future.get()` — блокирующий: поток стоит и ждёт результат, ничего не делая. Из нескольких `Future` неудобно строить
цепочки: «когда будет результат A, преобразовать его; когда готовы A и B, объединить». `CompletableFuture` (Java 8+)
решает это — описывает, **что сделать с результатом, когда он будет готов**, не блокируясь.

---

## Запуск

```java
void example(ExecutorService executor, String ticker) {
    CompletableFuture<Quote> future = CompletableFuture.supplyAsync(() -> load(ticker), executor);
}
```

`supplyAsync(supplier, executor)` запускает задачу на переданном пуле и сразу возвращает `CompletableFuture`. Без
executor'а задача идёт в общий `ForkJoinPool.commonPool()`.

---

## Конвейеры

Методы `CompletionStage` строят обработку по мере готовности результата:

- `thenApply(fn)` — преобразовать результат;
- `thenAccept(consumer)` — использовать результат (без возврата);
- `thenCompose(fn)` — связать с другим `CompletableFuture` (flatMap-стиль);
- `thenCombine(other, fn)` — объединить результаты двух;
- `exceptionally(fn)` — обработать ошибку в конвейере.

```java
void example(ExecutorService executor, String ticker) {
    CompletableFuture.supplyAsync(() -> load(ticker), executor)
            .thenApply(Quote::getPrice)                 // преобразовали
            .exceptionally(ex -> BigDecimal.ZERO);      // подстраховались на случай ошибки
}
```

---

## Ожидание нескольких

`allOf(...)` возвращает `CompletableFuture`, завершающийся, когда завершены все переданные:

```java
void example(List<CompletableFuture<Optional<Quote>>> futures) {
    CompletableFuture.allOf(futures.toArray(CompletableFuture[]::new)).join();   // дождаться всех
    List<Quote> quotes = futures.stream()
            .map(CompletableFuture::join)               // результат уже готов — join не заблокирует
            .flatMap(Optional::stream)
            .toList();
}
```

---

## Обработка ошибок

Исключения в конвейере пропагируются вниз (от ранней стадии к поздним), а не вверх. `exceptionally`/`handle` перехватывают
их и дают запасное значение. Для нашего `quotesFor` это позволяет сбою загрузки одного тикера превратиться в «пропуск»
(пустой результат), не роняя весь пакет.

---

## Применение в проекте

`quotesFor` переводится с `Future.get` на `CompletableFuture.supplyAsync` + `allOf`: загрузка каждого тикера — отдельный
`CompletableFuture`, ошибки гасятся `exceptionally`, порядок результатов сохраняется порядком в списке futures.
