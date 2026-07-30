# Stream API

Stream API (`java.util.stream`) позволяет обрабатывать последовательности данных декларативно: описывать *что* нужно
получить из коллекции, а не *как* пошагово это накапливать. Этот материал разбирает устройство стрима, виды операций и
коллекторы. Параллельное выполнение и расширение конвейера собственными операциями вынесены в отдельные материалы
(«Spliterator и параллельные стримы», «Gatherer API»).

## Содержание

1. Стрим как конвейер
2. Источник, промежуточные операции, терминальная
3. Ленивость и одноразовость
4. Промежуточные операции
5. Терминальные операции и reduce
6. Коллекторы
7. Числовые стримы и точность
8. flatMap и Optional.stream()
9. Типичные ошибки

---

## 1. Стрим как конвейер

Стрим — конвейер обработки элементов, а не хранилище. Элементы поступают из источника (коллекции, массива, генератора) и
«протекают» через цепочку операций; исходная коллекция при этом не изменяется. Уровень описания отличает стрим от цикла:
императивный цикл описывает механику накопления, стрим — намерение через именованные операции (`filter`, `map`,
`groupingBy`).

---

## 2. Источник, промежуточные операции, терминальная

Конвейер состоит из источника (`collection.stream()`, `Arrays.stream(...)`, `Stream.of(...)`, `Stream.iterate(...)`),
цепочки промежуточных операций и ровно одной терминальной операции, которая запускает обработку.

```java
void example(List<Instrument> instruments) {
    List<String> rubTickers = instruments.stream()          // источник
            .filter(i -> i.getCurrency() == Currency.RUB)   // промежуточная
            .map(Instrument::getTicker)                     // промежуточная
            .sorted()                                       // промежуточная
            .toList();                                      // терминальная
}
```

Источник стрима технически — `Spliterator`: он сообщает размер и порядок и умеет делиться на части (это используется при
параллельном выполнении). Без терминальной операции конвейер не выполняется.

---

## 3. Ленивость и одноразовость

Промежуточные операции ленивы: они лишь достраивают конвейер, а обработка начинается при вызове терминальной операции и
идёт по элементам — каждый элемент проходит всю цепочку, прежде чем берётся следующий. Отсюда два эффекта: отсутствие
промежуточных коллекций между операциями и короткое замыкание — `limit(n)`, `findFirst`, `anyMatch` останавливают
обработку, как только результат определён.

```java
void example(List<Quote> quotes) {
    Optional<Quote> firstGainer = quotes.stream()
            .filter(q -> q.getChangePercent().signum() > 0)
            .findFirst();   // остановится на первом подходящем
}
```

Стрим одноразовый: после терминальной операции повторный её вызов бросает `IllegalStateException`.

---

## 4. Промежуточные операции

- `filter(Predicate)` — оставляет подходящие элементы.
- `map(Function)` — преобразует элемент; `mapToInt`/`mapToDouble` дают числовой стрим.
- `flatMap(Function)` — преобразует элемент в стрим и сливает результаты в один стрим (см. раздел 8).
- `sorted()` / `sorted(Comparator)` — упорядочивает (требует пройти все элементы).
- `limit(n)` / `skip(n)` — берёт/пропускает первые `n`.
- `distinct()` — убирает дубликаты по `equals`/`hashCode`.
- `peek(Consumer)` — отладочное подглядывание, не для логики.

```java
void example(List<Quote> quotes) {
    List<Quote> topThreeGainers = quotes.stream()
            .filter(q -> q.getChangePercent().signum() > 0)
            .sorted(Comparator.comparing(Quote::getChangePercent).reversed())
            .limit(3)
            .toList();
}
```

---

## 5. Терминальные операции и reduce

`forEach`, `toList`/`toArray`, `count`, `min`/`max` (возвращают `Optional`), `anyMatch`/`allMatch`/`noneMatch`,
`findFirst`/`findAny`, `reduce`. `reduce` полезен там, где стандартного коллектора нет, — например, для суммы
`BigDecimal`:

```java
void example(List<Quote> quotes) {
    BigDecimal totalPrice = quotes.stream()
            .map(Quote::getPrice)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
}
```

---

## 6. Коллекторы

`collect(Collector)` собирает результат по правилу коллектора (`java.util.stream.Collectors`):

- `toList`, `toSet`, `toMap`, `toCollection`;
- `groupingBy(classifier)` → `Map<K, List<V>>`; со вторым коллектором меняет тип значения группы:
  `groupingBy(key, counting())`, `groupingBy(key, averagingDouble(fn))`;
- `partitioningBy(predicate)` — группировка на две части;
- `counting`, `summingInt/Double`, `averagingInt/Double`, `reducing` — коллекторы-агрегаты;
- `joining(sep, prefix, suffix)`.

```java
void example(List<Quote> quotes) {
    Map<String, Long> byType = quotes.stream()
            .collect(Collectors.groupingBy(
                    q -> q.getInstrument().getClass().getSimpleName(),
                    Collectors.counting()));
}
```

Для параллельных стримов существует `groupingByConcurrent` — он пишет в одну конкурентную `Map` вместо слияния
промежуточных (подробнее — в материале «Spliterator и параллельные стримы»).

---

## 7. Числовые стримы и точность

`mapToInt`/`mapToLong`/`mapToDouble` дают специализированные стримы (`IntStream`, `DoubleStream`) с `sum()`,
`average()`, `summaryStatistics()` — без боксинга:

```java
void example(List<Quote> quotes) {
    double avgChange = quotes.stream()
            .mapToDouble(q -> q.getChangePercent().doubleValue())
            .average()
            .orElse(0.0);
}
```

Оговорка о точности: перевод `BigDecimal` в `double` теряет точность. Для отображаемого среднего процента это допустимо;
для денежных величин действует правило доменной модели — оставаться на `BigDecimal` и агрегировать через `reduce`, а не
переводить в `double`.

---

## 8. flatMap и Optional.stream()

`flatMap` применяется, когда каждый элемент преобразуется в стрим значений, и эти стримы нужно слить в один. Частая
идиома — отбросить отсутствующие значения без проверок на `null`, опираясь на `Optional.stream()` (пустой стрим для
`Optional.empty()`, одноэлементный — для присутствующего значения):

```java
void example(List<String> tickers, QuoteService quoteService) {
    List<Quote> found = tickers.stream()
            .map(quoteService::quoteFor)   // String -> Optional<Quote>
            .flatMap(Optional::stream)
            .toList();
}
```

---

## 9. Типичные ошибки

- **Переиспользование стрима** — он одноразовый; повторная обработка из нового `stream()`.
- **Побочные эффекты в промежуточных операциях** — `map`/`filter` должны быть чистыми; изменение внешнего состояния
  ломает предсказуемость и несовместимо с параллельным выполнением.
- **Ожидание, что стрим изменит источник** — `list.stream().sorted()` не сортирует `list`.
- **`peek` для логики** — при коротком замыкании может не выполниться для всех элементов.
- **Перевод денег в `double` ради `sum()`** — для денежных сумм `reduce` над `BigDecimal`.
