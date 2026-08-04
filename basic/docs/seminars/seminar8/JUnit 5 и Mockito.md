# JUnit 5 и Mockito

---

## Зачем автоматические тесты

Проверять код запуском вручную не масштабируется: при каждом изменении нельзя перещёлкать все сценарии. Автоматический
тест фиксирует ожидаемое поведение в коде и перезапускается за секунды — он и документация, и страховка при рефакторинге.

**Пирамида тестирования:** много быстрых **unit**-тестов (одна единица логики в изоляции), меньше **интеграционных**
(несколько компонентов вместе), совсем немного медленных **end-to-end**. Основа — unit-тесты.

---

## JUnit 5: основы

Тест — метод с аннотацией `@Test`. Проверки — статические методы `org.junit.jupiter.api.Assertions`.

```java
class WatchlistTest {

    @Test
    void добавлениеНовогоТикераВозвращаетTrue() {
        Watchlist watchlist = new Watchlist();
        assertTrue(watchlist.add("SBER"));
    }
}
```

Основные ассерты: `assertEquals(ожидаемое, фактическое)`, `assertTrue`/`assertFalse`, `assertNull`/`assertNotNull`,
`assertThrows(Type.class, () -> ...)`. Общая подготовка — в методе с `@BeforeEach` (выполняется перед каждым тестом);
человекочитаемое имя — `@DisplayName`.

---

## Arrange-Act-Assert

Тест структурируют в три шага: подготовить данные (Arrange), выполнить действие (Act), проверить результат (Assert).
Один тест — одно поведение.

```java
@Test
void дивидендыСчитаютсяКакЦенаНаДоходность() {
    Stock stock = new Stock("SBER", "Сбербанк", Currency.RUB, "Banks", new BigDecimal("10")); // Arrange
    BigDecimal dividends = stock.getDividends(new BigDecimal("250.00"));                        // Act
    assertEquals(new BigDecimal("25.00"), dividends);                                           // Assert
}
```

Сравнение `BigDecimal` через `assertEquals` учитывает масштаб: `25.00` не равно `25.0` — тест на деньги фиксирует и
точность.

---

## Тестирование ошибок

Исключение — часть контракта; его проверяют `assertThrows`, который возвращает пойманное исключение для дальнейших
проверок:

```java
@Test
void неизвестныйТипБросаетИсключение() {
    assertThrows(IllegalArgumentException.class,
            () -> InstrumentFactories.create("CRYPTO", "BTC", "Bitcoin", Currency.RUB));
}
```

---

## Mockito: изоляция зависимостей

Тестируемый класс часто зависит от того, что в тесте недоступно или нежелательно — например, `QuoteService` зависит от
`QuoteSource`, который ходит в сеть. Mockito создаёт **заглушку** (mock) по интерфейсу: мы задаём, что она вернёт, и
проверяем поведение в изоляции.

```java
@Test
void quoteForОтдаётКотировкуИзИсточника() {
    QuoteSource source = mock(QuoteSource.class);                 // заглушка вместо сети
    when(source.fetch("SBER")).thenReturn(Optional.of(sberQuote)); // программируем ответ
    QuoteService service = new QuoteService(source);

    assertEquals(Optional.of(sberQuote), service.quoteFor("SBER"));
    verify(source).fetch("SBER");                                  // проверяем факт вызова
}
```

`mock(...)` — создать заглушку, `when(...).thenReturn(...)` — задать ответ, `verify(...)` — проверить вызов. Тестируемый
класс не отличает заглушку от настоящего источника. Именно ради такой подмены `QuoteSource` был вынесен в интерфейс.

---

## Тестирование CLI-команды

Команда — это разбор аргументов (picocli) плюс логика. Запуск через `CommandLine.execute(...)` проверяет и парсинг, и
поведение; вывод перехватывают, временно подменив `System.out`:

```java
@Test
void listФильтруетПоТипу() {
    InstrumentRepository repository = new InstrumentRepository();
    repository.add(new Stock("SBER", "Сбербанк", Currency.RUB, "Banks", new BigDecimal("6.5")));

    ByteArrayOutputStream buffer = new ByteArrayOutputStream();
    PrintStream original = System.out;
    System.setOut(new PrintStream(buffer));
    try {
        new CommandLine(new ListCommand(repository)).execute("--type", "Stock");
    } finally {
        System.setOut(original);   // обязательно возвращаем System.out
    }
    assertTrue(buffer.toString().contains("Акция"));
}
```

Тестируют результат (вывод, `exit code`, эффект), а не точное форматирование каждой строки.

---

## Запуск в CI

Тесты ценны, когда прогоняются автоматически. Continuous Integration — сервер, который на каждый push собирает проект и
запускает `mvn test`; сломанные тесты видны сразу, до слияния. Пример конфигурации — в сопроводительном материале.
