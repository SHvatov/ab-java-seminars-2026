# Тестирование в Java

Автоматические тесты фиксируют ожидаемое поведение кода и перезапускаются за секунды, заменяя ручную перепроверку.
Материал разбирает JUnit 5 (стандарт тестирования в Java), Mockito (изоляция зависимостей), особенности тестирования
CLI-приложения и запуск тестов в CI.

## Содержание

1. Зачем тесты и пирамида тестирования
2. JUnit 5: аннотации и ассерты
3. Жизненный цикл теста
4. Тестирование исключений
5. Параметризованные тесты
6. Mockito: изоляция зависимостей
7. Что тестировать в CLI
8. Запуск тестов в CI

---

## 1. Зачем тесты и пирамида тестирования

Проверка запуском вручную не масштабируется: при каждом изменении невозможно перещёлкать все сценарии, а без этого
рефакторинг рискован. Тест — исполняемое утверждение о коде: он и документирует поведение, и мгновенно сигналит, когда
оно сломалось.

Пирамида тестирования расставляет типы тестов по количеству и стоимости:

- **Unit** — одна единица логики в изоляции; быстрые, их много; основа пирамиды.
- **Интеграционные** — несколько компонентов вместе (например, команда + репозиторий); медленнее, их меньше.
- **End-to-end** — приложение целиком; самые медленные и хрупкие, их совсем немного.

Опора — на unit-тесты: они дают максимум уверенности за минимум времени.

---

## 2. JUnit 5: аннотации и ассерты

Тест — метод с `@Test`. Проверки — статические методы `org.junit.jupiter.api.Assertions`:

```java
class WatchlistTest {

    @Test
    void добавлениеНовогоТикераВозвращаетTrue() {
        Watchlist watchlist = new Watchlist();
        assertTrue(watchlist.add("SBER"));
    }

    @Test
    void дубликатНеДобавляется() {
        Watchlist watchlist = new Watchlist();
        watchlist.add("SBER");
        assertFalse(watchlist.add("sber"));       // нормализация регистра
        assertEquals(1, watchlist.tickers().size());
    }
}
```

Ассерты: `assertEquals(ожидаемое, фактическое)`, `assertTrue`/`assertFalse`, `assertNull`/`assertNotNull`,
`assertThrows`, `assertAll` (сгруппировать несколько проверок). Порядок аргументов `assertEquals` — сначала ожидаемое,
потом фактическое; это влияет на текст сообщения об ошибке.

---

## 3. Жизненный цикл теста

- `@BeforeEach` — выполняется перед каждым тестом (общая подготовка: создать объект, наполнить данные).
- `@AfterEach` — после каждого теста (очистка).
- `@BeforeAll`/`@AfterAll` — один раз на класс (дорогие ресурсы); методы статические.
- `@DisplayName("...")` — человекочитаемое имя теста в отчёте.
- `@Disabled` — временно отключить тест.

```java
class InstrumentRepositoryTest {

    private InstrumentRepository repository;

    @BeforeEach
    void setUp() {
        repository = new InstrumentRepository();   // свежий репозиторий на каждый тест
    }

    @Test
    void findByTickerРегистронезависим() {
        repository.add(new Stock("SBER", "Сбербанк", Currency.RUB, "Banks", new BigDecimal("6.5")));
        assertTrue(repository.findByTicker("sber").isPresent());
    }
}
```

Изоляция тестов важна: каждый тест начинается с чистого состояния, иначе они начинают влиять друг на друга.

---

## 4. Тестирование исключений

Выбрасывание исключения — часть контракта метода. `assertThrows` проверяет тип и возвращает пойманное исключение для
дальнейших проверок (например, сообщения):

```java
@Test
void неизвестныйТипБросаетИсключение() {
    IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
            () -> InstrumentFactories.create("CRYPTO", "BTC", "Bitcoin", Currency.RUB));
    assertTrue(e.getMessage().contains("CRYPTO"));
}
```

---

## 5. Параметризованные тесты

Когда одно поведение проверяется на наборе входов, вместо копирования теста используют `@ParameterizedTest` с
источником данных:

```java
@ParameterizedTest
@ValueSource(strings = {"SBER", "sber", "Sber"})
void тикерНормализуетсяКВерхнемуРегистру(String input) {
    Watchlist watchlist = new Watchlist();
    watchlist.add(input);
    assertTrue(watchlist.contains("SBER"));
}
```

`@ValueSource`, `@CsvSource`, `@MethodSource` задают наборы аргументов; тест выполняется по разу на каждый.

---

## 6. Mockito: изоляция зависимостей

Единица логики часто зависит от того, что в unit-тесте недоступно или нежелательно — сеть, база, время. Такую
зависимость подменяют **заглушкой** (mock), поведение которой задают в тесте. `QuoteService` зависит от `QuoteSource`,
ходящего в сеть; в тесте источник мокается:

```java
@Test
void quoteForОтдаётКотировкуИзИсточника() {
    QuoteSource source = mock(QuoteSource.class);
    Quote sberQuote = new Quote(sber(), new BigDecimal("250"), new BigDecimal("1.2"));
    when(source.fetch("SBER")).thenReturn(Optional.of(sberQuote));

    QuoteService service = new QuoteService(source);

    assertEquals(Optional.of(sberQuote), service.quoteFor("SBER"));
    verify(source).fetch("SBER");
}
```

- `mock(Type.class)` — создать заглушку;
- `when(mock.method(args)).thenReturn(value)` — задать ответ на конкретный вызов;
- `verify(mock).method(args)` — проверить, что вызов был (с `times(n)`, `never()` — сколько раз);
- `when(...).thenThrow(...)` — заставить заглушку бросить исключение (тест обработки ошибок сети).

Тестируемый класс не отличает заглушку от настоящего объекта. Возможность подмены — причина, по которой зависимости
объявляют через интерфейс (`QuoteSource`): границу с внешним миром можно заменить в тесте.

---

## 7. Что тестировать в CLI

CLI-команда — это разбор аргументов (picocli) плюс логика. Тестируют наблюдаемый результат: `exit code`, эффект на
состояние (репозиторий), вывод — но не точное форматирование каждой строки (оно хрупко и часто меняется). Запуск через
`CommandLine.execute(...)` проверяет и парсинг аргументов, и поведение команды.

Вывод в консоль перехватывают, временно подменив `System.out` буфером:

```java
@Test
void listФильтруетПоТипу() {
    InstrumentRepository repository = new InstrumentRepository();
    repository.add(new Stock("SBER", "Сбербанк", Currency.RUB, "Banks", new BigDecimal("6.5")));
    repository.add(new Bond("OFZ", "ОФЗ", Currency.RUB, new BigDecimal("8"), 2030));

    ByteArrayOutputStream buffer = new ByteArrayOutputStream();
    PrintStream original = System.out;
    System.setOut(new PrintStream(buffer, true, StandardCharsets.UTF_8));
    try {
        int code = new CommandLine(new ListCommand(repository)).execute("--type", "Stock");
        assertEquals(0, code);
    } finally {
        System.setOut(original);   // System.out глобален — обязательно восстанавливаем
    }

    assertTrue(buffer.toString(StandardCharsets.UTF_8).contains("Акция"));
    assertFalse(buffer.toString(StandardCharsets.UTF_8).contains("Облигация"));
}
```

`System.out` — глобальное состояние, поэтому его подмену обязательно откатывают в `finally`, иначе следующий тест
получит буфер вместо консоли.

---

## 8. Запуск тестов в CI

Тесты приносят пользу, когда прогоняются автоматически при каждом изменении, а не по памяти разработчика. Continuous
Integration — сервер, который на каждый push собирает проект и запускает `mvn test`; сломанный тест виден сразу и не
попадает в общую ветку. Минимальная конфигурация для GitHub Actions:

```yaml
# .github/workflows/tests.yml
name: tests
on: [push, pull_request]
jobs:
  test:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
      - uses: actions/setup-java@v4
        with:
          distribution: temurin
          java-version: '25'
      - run: mvn -B test
```

`maven-surefire-plugin` находит тесты (классы `*Test`) и запускает их в фазе `test`. Красный билд блокирует слияние —
регрессия не доходит до основной ветки.
