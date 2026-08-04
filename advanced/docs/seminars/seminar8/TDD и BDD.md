# TDD и BDD

---

## TDD: разработка через тесты

Test-Driven Development меняет порядок: сначала тест, потом код. Цикл — **red-green-refactor**:

1. **Red** — написать падающий тест на желаемое поведение (кода ещё нет).
2. **Green** — написать минимальный код, чтобы тест прошёл.
3. **Refactor** — улучшить код при зелёных тестах; они страхуют от регрессий.

Тест здесь — сначала формулировка требования, потом его проверка.

```java
// Red — метода remove ещё нет, тест не проходит
@Test
void removeУбираетТикер() {
    Watchlist watchlist = new Watchlist();
    watchlist.add("SBER");
    assertTrue(watchlist.remove("sber"));
    assertFalse(watchlist.contains("SBER"));
}
```

```java
// Green — минимальная реализация в Watchlist
public boolean remove(String ticker) {
    return tickers.remove(ticker.toUpperCase());
}
```

После зелёного теста код можно рефакторить спокойно — поведение зафиксировано.

---

## BDD: описываем поведение

Behavior-Driven Development смещает фокус с «тестируем метод» на «описываем поведение» в терминах, близких к
требованию: **Given** (предусловие) — **When** (действие) — **Then** (результат). В JUnit 5 это выражается `@Nested` и
`@DisplayName` без отдельного фреймворка:

```java
@DisplayName("QuoteService")
class QuoteServiceBddTest {

    @Nested
    @DisplayName("когда котировка уже в кеше")
    class WhenCached {
        @Test
        @DisplayName("тогда повторный запрос не идёт к источнику")
        void неХодитКИсточникуПовторно() {
            QuoteSource source = mock(QuoteSource.class);                 // Given
            when(source.fetch("SBER")).thenReturn(Optional.of(sberQuote));
            QuoteService service = new QuoteService(source);

            service.quoteFor("SBER");                                     // When
            service.quoteFor("SBER");

            verify(source, times(1)).fetch("SBER");                      // Then
        }
    }
}
```

Отчёт JUnit читается как спецификация поведения. Полноценный BDD с человекочитаемыми сценариями дают Cucumber/Gherkin,
но идеологию Given-When-Then удобно применять и в чистом JUnit.

---

## TDD и BDD вместе

TDD — про порядок (тест до кода), BDD — про формулировку (поведение, а не метод). Они не исключают друг друга: можно
вести разработку через тесты, описывая их в стиле Given-When-Then. Общий принцип — тест выражает намерение раньше и
яснее, чем реализация.
