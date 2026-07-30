# Stream API

---

## Что такое стрим

Stream — конвейер обработки последовательности элементов, а не структура данных. Он не хранит данные и не изменяет
источник (коллекцию, из которой создан): он описывает набор операций над элементами. Stream API позволяет выразить
обработку декларативно — *что* нужно получить, а не *как* пошагово это накапливать.

---

## От коллекции к стриму: stream()

```java
void example(List<Instrument> instruments) {
    Stream<Instrument> stream = instruments.stream();
}
```

Из массива — `Arrays.stream(array)`, из отдельных значений — `Stream.of(...)`, генераторы — `Stream.iterate`/`generate`.
Обратно в коллекцию возвращает терминальная операция (`toList()`, `collect(...)`).

Источник стрима — `Spliterator` (не `Iterator`): он умеет не только перебирать, но и сообщать размер и порядок и
делиться на части. Эта способность делиться лежит в основе параллельных стримов.

---

## Промежуточные и терминальные операции

- **Промежуточные** возвращают новый стрим и соединяются в цепочку: `filter`, `map`, `sorted`, `limit`, `distinct`.
- **Терминальные** запускают конвейер и дают результат или побочный эффект: `forEach`, `toList`, `count`, `reduce`,
  `collect`.

Стрим завершается ровно одной терминальной операцией и одноразов: повторный вызов терминальной операции на нём бросает
`IllegalStateException`.

```java
void example(List<Instrument> instruments) {
    List<String> tickers = instruments.stream()
            .filter(i -> i.getCurrency() == Currency.RUB)
            .map(Instrument::getTicker)
            .sorted()
            .toList();
}
```

---

## Ленивость

Промежуточные операции ленивы: пока не вызвана терминальная, не обрабатывается ни один элемент, а элементы «протекают»
через конвейер по одному без промежуточных коллекций. Короткозамкнутые операции (`limit`, `findFirst`, `anyMatch`)
останавливают обработку, как только результат определён.

---

## Коллекторы

`collect(...)` собирает результат конвейера. Основные — из `java.util.stream.Collectors`:

- `groupingBy(classifier)` — в `Map` по ключу; со вторым коллектором — `groupingBy(key, counting())`,
  `groupingBy(key, averagingDouble(...))`;
- `toList`/`toSet`/`toMap`, `joining`, `partitioningBy`, `reducing`.

```java
void example(List<Quote> quotes) {
    Map<Currency, Long> byCurrency = quotes.stream()
            .collect(Collectors.groupingBy(
                    q -> q.getInstrument().getCurrency(),
                    Collectors.counting()));
}
```

---

## flatMap и Optional.stream()

`flatMap` разворачивает «стрим стримов» в один стрим. Частая идиома — отбросить отсутствующие значения без проверок на
`null`: `Optional.stream()` даёт пустой стрим для `Optional.empty()` и одноэлементный — для присутствующего значения.

```java
void example(List<String> tickers, QuoteService quoteService) {
    List<Quote> found = tickers.stream()
            .map(quoteService::quoteFor)   // String -> Optional<Quote>
            .flatMap(Optional::stream)
            .toList();
}
```

---

## Оговорки

- Стрим не меняет источник: `list.stream().sorted()` не сортирует `list`.
- `mapToInt`/`mapToDouble` дают числовые стримы с `sum()`/`average()`, но перевод `BigDecimal` в `double` теряет
  точность — для денежных сумм остаются `BigDecimal` и `reduce`.
- Промежуточные операции должны быть чистыми (без побочных эффектов) — это условие корректной параллельной обработки.
