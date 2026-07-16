# Vavr: Try и Either

---

## Зачем ещё одна библиотека

[Vavr](https://vavr.io/) — библиотека функциональных типов для Java: неизменяемые коллекции, `Option`, `Try`,
`Either` и другие конструкции, отсутствующие в стандартной библиотеке.

```xml
<dependency>
    <groupId>io.vavr</groupId>
    <artifactId>vavr</artifactId>
    <version>1.0.1</version>
</dependency>
```

---

## Try&lt;T&gt;

Оборачивает вычисление, способное бросить исключение, в значение — `Success(T)` либо `Failure(Throwable)`, без
`try/catch` в месте использования:

```java
void example() {
    Try<Currency> currency = Try.of(() -> Currency.valueOf(currencyString));

    currency.onSuccess(value -> System.out.println("Валюта распознана: " + value))
            .onFailure(error -> System.out.println("Некорректная валюта: " + currencyString));
}
```

---

## Either&lt;L, R&gt;

Представляет одно из двух значений — по соглашению `Left` для ошибки, `Right` для успеха. В отличие от исключения, тип
возможной ошибки виден прямо в сигнатуре метода:

```java
public Either<String, Instrument> parseInstrument(String type, String ticker, String name, String currencyStr) {
    try {
        Currency currency = Currency.valueOf(currencyStr.toUpperCase());
        return Either.right(InstrumentFactories.create(type, ticker, name, currency));
    } catch (IllegalArgumentException e) {
        return Either.left("Некорректный тип или валюта: " + e.getMessage());
    }
}
```

```java
parseInstrument(type, ticker, name, currencyStr)
        .peek(instrument -> repository.add(instrument))
        .peekLeft(error -> System.out.println("Ошибка: " + error));
```

---

## Область применения сегодня

Знакомство, не повсеместное внедрение. Полноценная стратегия обработки ошибок и исключений — тема семинара 4, где
также обсуждается, где в проекте уместны исключения, а где — типы вроде `Either`.
