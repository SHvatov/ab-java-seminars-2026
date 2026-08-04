# JUnit 5 и Mockito

---

## JUnit 5

Тест — метод с `@Test`; проверки — `Assertions` (`assertEquals`, `assertTrue`, `assertThrows`); структура —
Arrange-Act-Assert (подготовить, выполнить, проверить). `@BeforeEach` — общая подготовка перед каждым тестом,
`@DisplayName` — читаемое имя, `@Nested` — группировка.

```java
@Test
void дивидендыСчитаютсяКакЦенаНаДоходность() {
    Stock stock = new Stock("SBER", "Сбербанк", Currency.RUB, "Banks", new BigDecimal("10"));
    assertEquals(new BigDecimal("25.00"), stock.getDividends(new BigDecimal("250.00")));
}
```

Сравнение `BigDecimal` через `assertEquals` учитывает масштаб (`25.00` ≠ `25.0`) — тест на деньги фиксирует и точность.

---

## Тестирование ошибок

```java
@Test
void повторныйТикерБросаетИсключение() {
    InstrumentRepository repository = new InMemoryInstrumentRepository();
    repository.add(sber());
    assertThrows(DuplicateTickerException.class, () -> repository.add(sber()));
}
```

---

## Mockito: изоляция зависимостей

`QuoteService` зависит от сетевого `QuoteSource`. Чтобы тест не требовал сети, источник подменяется заглушкой:

```java
QuoteSource source = mock(QuoteSource.class);
when(source.fetch("SBER")).thenReturn(Optional.of(sberQuote));
QuoteService service = new QuoteService(source);

assertEquals(Optional.of(sberQuote), service.quoteFor("SBER"));
verify(source).fetch("SBER");
```

`mock` — заглушка по интерфейсу, `when(...).thenReturn(...)` — программируемый ответ, `verify(...)` — проверка вызова
(с `times(n)` — количества). Интерфейс `QuoteSource` — граница, которую подменяет mock; ради этого он и был выделен.
