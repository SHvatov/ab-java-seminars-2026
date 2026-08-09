# CompletableFuture

---

## От Future к конвейерам

`Future.get()` блокирует. `CompletableFuture` (Java 8+) реализует `Future` и `CompletionStage`: описывает обработку
результата по готовности и строит из этого конвейеры, не блокируясь.

```java
CompletableFuture.supplyAsync(() -> load(ticker), executor)
        .thenApply(this::enrich)
        .exceptionally(ex -> Optional.empty());
```

---

## Категории методов CompletionStage

- **Последовательные:** `thenApply` (преобразовать), `thenAccept` (использовать), `thenRun` (просто продолжить).
- **Композиция:** `thenCompose` (связать с другим CF, flatMap), `thenCombine` (объединить два), `acceptEither`/`applyToEither`
  (кто первым).
- **Ошибки:** `exceptionally` (запасное значение), `handle` (результат-или-исключение).
- **`...Async`:** реально асинхронные аналоги.

---

## Где исполняется стадия

Тонкость, важная на практике:

- не-`Async`-метод (`thenApply`) выполняется в потоке, **завершившем предыдущую стадию** (или в вызывающем, если уже
  готово);
- `...Async` **без** executor'а — в `ForkJoinPool.commonPool()`;
- `...Async` **с** executor'ом — в заданном пуле.

Неучтённый пул — частый источник сюрпризов (например, блокирующая задача, случайно попавшая в `commonPool`, исчерпывает
его на всё приложение). Исключения пропагируются вниз по конвейеру (от ранних стадий к поздним).

---

## Ожидание набора

`allOf(...)` завершается, когда завершены все; порядок результатов сохраняется порядком в списке futures:

```java
CompletableFuture.allOf(futures.toArray(CompletableFuture[]::new)).join();
List<Quote> quotes = futures.stream().map(CompletableFuture::join).flatMap(Optional::stream).toList();
```

В проекте `quotesFor` переводится на `supplyAsync` + `allOf` с `exceptionally`: сбой одного тикера — пропуск, а не падение
пакета.
