# Семинар 1. Введение в ООП (продвинутый трек)

**Тема модуля:** Погружение в объектно-ориентированное программирование

**Тема семинара:** Проектирование доменной области. ООП-принципы. Устройство объектов в памяти JVM.

**Проект:** Market Pulse — CLI-терминал для аналитики фондового рынка (T-Invest API)

---

## Пререквизиты

`Java Basics` `Переменные и типы` `Условия и циклы` `Методы и классы` `Git (ветки, MR)` `Установленный JDK 25` `Установленный IntelliJ IDEA` `Установленный VisualVM` `Maven (базовый уровень)`

---

## План семинара

| **#** | **Этап**                                     | **Время** |
| ----- | --------------------------------------------- | --------- |
| 1     | Знакомство и представление проекта            | ~10 мин   |
| 2     | Теория: ООП — принципы и их взаимосвязь       | ~15 мин   |
| 3     | Брейншторминг: моделируем домен Т-Инвестиций  | ~10 мин   |
| 4     | Совместная реализация кода                    | ~40 мин   |
| 5     | Теория: устройство памяти в JVM               | ~15 мин   |
| 6     | Практика: VisualVM и JOL                      | ~25 мин   |
| 7     | Итоги                                         | ~5 мин    |

---

### Этап 1. Знакомство и представление проекта (~10 мин)

**Знакомство.** Коротко представиться, попросить каждого назвать имя и ответить на один вопрос: *«Что ты уже писал на Java, и что хочешь понять глубже?»* — для продвинутого трека вопрос точнее, он сразу задаёт тон.

**Представление проекта.** Рассказываем историю Серёжи: он хочет заработать на квартиру и решает попробовать инвестировать. Но он не знает, какие акции покупать и когда — поэтому нам нужен инструмент для анализа рынка. Так появляется **Market Pulse** — CLI-терминал для аналитики фондового рынка на базе T-Invest API.

Показываем README проекта, пробегаемся по функциональным требованиям. Студенты клонируют репозиторий и создают ветку — Git им уже знаком, поэтому без инструкций:

```bash
git clone <repo-url>
cd ab-java-seminars-2026
git checkout -b seminar-01/domain-model
```

---

### Этап 2. Теория: ООП (~15 мин)

Презентация. Цель — не пересказать учебник, а показать **зачем** и **как принципы работают вместе**. Аудитория продвинутая, поэтому темп выше, определения короче, акцент на взаимосвязи: инкапсуляция как защита инвариантов, наследование как IS-A с ценой каскадных изменений (с намёком на ISP), два вида полиморфизма, и то, как всё это связано в единую систему.

Полный конспект лекции: [`lectures/oop-principles.md`](lectures/oop-principles.md). Расширенный материал для самостоятельного изучения: [`docs/materials/seminar1/oop.md`](../../materials/seminar1/oop.md).

---

### Этап 3. Брейншторминг: моделируем домен (~10 мин)

Студенты работают самостоятельно или в парах на доске / в Miro.

**Задание:**

> Представьте, что вы проектируете MVP Market Pulse. Нам нужно реализовать минимально рабочее приложение на моках. Подумайте:

- > Какие сущности из мира Т-Инвестиций вам понадобятся?
- > Какие из них связаны? Как именно?
- > Какие данные и поведение у каждой?

Через 8 минут — короткое обсуждение. Студенты называют сущности, семинарист фиксирует на доске и приходит к референсной модели.

**Ожидаемый результат:**

```other
Instrument (инструмент: акция, облигация, ETF)
  ├── Stock      (акция)
  ├── Bond       (облигация)
  └── Etf        (биржевой фонд)

Quote          (котировка → агрегация с Instrument)
Portfolio      (портфель)
  └── Position  (позиция → композиция внутри Portfolio)
```

---

### Этап 4. Совместная реализация кода (~40 мин)

Структура та же, что и в базовом треке, но темп выше и обсуждения глубже.

#### 4.1. Вспомогательные типы

Итоговый код: [`Currency.java`](../../../src/main/java/ru/tbank/marketpulse/model/Currency.java)

```java
// model/Currency.java
public enum Currency {
    RUB, USD, EUR
}
```

#### 4.2. Базовый класс — инкапсуляция

Итоговый код: [`Instrument.java`](../../../src/main/java/ru/tbank/marketpulse/model/Instrument.java)

```java
// model/Instrument.java
public abstract class Instrument {

    private final String ticker;
    private final String name;
    private final Currency currency;

    public Instrument(String ticker, String name, Currency currency) {
        if (ticker == null || ticker.isBlank()) {
            throw new IllegalArgumentException("Ticker cannot be blank");
        }
        this.ticker = ticker;
        this.name = name;
        this.currency = currency;
    }

    public String getTicker()      { return ticker; }
    public String getName()        { return name; }
    public Currency getCurrency()  { return currency; }

    public abstract String getDescription();

    @Override
    public String toString() {
        return ticker + " — " + name + " (" + getDescription() + ")";
    }
}
```

#### 4.3. Наивная реализация → рефакторинг в полиморфизм

Пишем первую версию описания через `instanceof`-цепочку в статическом методе (teaching-only, в финальный код не входит):

```java
// util/InstrumentFormatter.java — наивный подход
public class InstrumentFormatter {

    public static String describe(Instrument instrument) {
        if (instrument instanceof Stock stock) {
            return "Акция, сектор: " + stock.getSector();
        } else if (instrument instanceof Bond bond) {
            return "Облигация, купон: " + bond.getCouponRate() + "%";
        } else if (instrument instanceof Etf etf) {
            return "ETF, индекс: " + etf.getTrackingIndex();
        }
        return "Неизвестный инструмент";
    }
}
```

> **Обсуждение:** что не так? Новый тип инструмента → нужно найти и обновить все такие цепочки по всей кодовой базе. Компилятор не предупредит.

Удаляем `InstrumentFormatter`, переносим логику в подклассы. Итоговый код: [`Stock.java`](../../../src/main/java/ru/tbank/marketpulse/model/Stock.java), [`Bond.java`](../../../src/main/java/ru/tbank/marketpulse/model/Bond.java), [`Etf.java`](../../../src/main/java/ru/tbank/marketpulse/model/Etf.java)

```java
// model/Stock.java
public class Stock extends Instrument {

    private final String sector;
    private final BigDecimal dividendYield;

    public Stock(String ticker, String name, Currency currency,
                 String sector, BigDecimal dividendYield) {
        super(ticker, name, currency);
        this.sector = sector;
        this.dividendYield = dividendYield;
    }

    public String getSector() { return sector; }

    public BigDecimal getDividends(BigDecimal currentPrice) {
        return currentPrice.multiply(dividendYield)
                           .divide(BigDecimal.valueOf(100));
    }

    @Override
    public String getDescription() {
        return "Акция, сектор: " + sector;
    }
}
```

```java
// model/Bond.java
public class Bond extends Instrument {

    private final BigDecimal couponRate;
    private final int maturityYear;

    public Bond(String ticker, String name, Currency currency,
                BigDecimal couponRate, int maturityYear) {
        super(ticker, name, currency);
        this.couponRate = couponRate;
        this.maturityYear = maturityYear;
    }

    public BigDecimal getCouponRate() { return couponRate; }
    public int getMaturityYear()      { return maturityYear; }

    @Override
    public String getDescription() {
        return "Облигация, купон: " + couponRate + "%, погашение: " + maturityYear;
    }
}
```

```java
// model/Etf.java
public class Etf extends Instrument {

    private final String trackingIndex;

    public Etf(String ticker, String name, Currency currency, String trackingIndex) {
        super(ticker, name, currency);
        this.trackingIndex = trackingIndex;
    }

    public String getTrackingIndex() { return trackingIndex; }

    @Override
    public String getDescription() {
        return "ETF, отслеживает индекс: " + trackingIndex;
    }
}
```

#### 4.4. BigDecimal — мотивация

```java
System.out.println(0.1 + 0.2); // 0.30000000000000004
```

`double` не может точно представить большинство десятичных дробей. Для финансовых расчётов — `BigDecimal`. Важный нюанс:

```java
new BigDecimal(278.50)    // наследует неточность double!
new BigDecimal("278.50")  // точно
```

#### 4.5. LSP — нарушение через getDividends

Хотим посчитать дивиденды для любого `Instrument`. Соблазн — вынести метод в базовый класс:

```java
// Плохая идея: добавляем в Instrument
public abstract BigDecimal getDividends(BigDecimal currentPrice);
```

Что происходит в `Bond` и `Etf`?

```java
// Bond — у облигации нет дивидендов
@Override
public BigDecimal getDividends(BigDecimal currentPrice) {
    return BigDecimal.ZERO;          // Вариант 1: тихо врём
    // throw new UnsupportedOperationException(); // Вариант 2: ломаем контракт
}
```

> **Обсуждение:** оба варианта нарушают LSP. Метод не принадлежит базовому классу — убираем его из `Instrument`, оставляем только в `Stock`. Именно так и сделано в итоговом [`Stock.java`](../../../src/main/java/ru/tbank/marketpulse/model/Stock.java).

#### 4.6. Агрегация vs композиция — через StockSnapshot → Quote

Хотим представить акцию с актуальной ценой. Первый порыв — наследоваться от `Stock` (teaching-only, в финальный код не входит):

```java
// model/StockSnapshot.java — попытка через наследование
public class StockSnapshot extends Stock {

    private final BigDecimal price;

    public StockSnapshot(String ticker, String name, Currency currency,
                         String sector, BigDecimal dividendYield, BigDecimal price) {
        super(ticker, name, currency, sector, dividendYield);
        this.price = price;
    }

    // Перегружаем — удобно, цена уже вшита
    public BigDecimal getDividends() {
        return getDividends(this.price);
    }

    // Но getDividends(BigDecimal) из родителя никуда не делся!
    @Override
    public BigDecimal getDividends(BigDecimal currentPrice) {
        // Вариант 1: игнорируем аргумент — зачем тогда он вообще есть?
        return getDividends();
        // Вариант 2: используем аргумент — тогда зачем нам this.price?
    }
}
```

> **Обсуждение:** перегрузка не заменяет переопределение. Контракт родителя нарушен — это снова LSP. `StockSnapshot` — не вид `Stock`, это `Stock` + рыночная цена в момент времени. Переименовываем в `Quote` и делаем агрегацию.

Итоговый код: [`Quote.java`](../../../src/main/java/ru/tbank/marketpulse/model/Quote.java)

```java
// model/Quote.java — агрегация
public class Quote {

    private final Instrument instrument;
    private final BigDecimal price;
    private final BigDecimal changePercent;

    public Quote(Instrument instrument, BigDecimal price, BigDecimal changePercent) {
        this.instrument = instrument;
        this.price = price;
        this.changePercent = changePercent;
    }

    public Instrument getInstrument()      { return instrument; }
    public BigDecimal getPrice()           { return price; }
    public BigDecimal getChangePercent()   { return changePercent; }

    // Дивиденды считаем здесь — цена известна, делегируем в Stock
    public BigDecimal getDividends() {
        if (instrument instanceof Stock stock) {
            return stock.getDividends(this.price);
        }
        return BigDecimal.ZERO;
    }

    @Override
    public String toString() {
        String direction = changePercent.signum() >= 0 ? "▲" : "▼";
        return instrument.getTicker() + ": " + price + " " + instrument.getCurrency()
               + " " + direction + " " + changePercent.abs() + "%";
    }
}
```

**Композиция для контраста** — `Portfolio` создаёт и владеет `Position`. Итоговый код: [`Portfolio.java`](../../../src/main/java/ru/tbank/marketpulse/model/Portfolio.java)

```java
// model/Portfolio.java
public class Portfolio {

    public static class Position {
        private final Instrument instrument;
        private final int quantity;

        private Position(Instrument instrument, int quantity) {
            this.instrument = instrument;
            this.quantity = quantity;
        }

        public Instrument getInstrument() { return instrument; }
        public int getQuantity()          { return quantity; }
    }

    private final String name;
    private final List<Position> positions = new ArrayList<>();

    public Portfolio(String name) { this.name = name; }

    public void addPosition(Instrument instrument, int quantity) {
        positions.add(new Position(instrument, quantity));
    }

    public List<Position> getPositions() {
        return Collections.unmodifiableList(positions);
    }
}
```

> **Акцент:** спросить студентов — что произойдёт с позициями, если портфель удалён? А с инструментом, если котировка удалена? Ответы разные — и это и есть разница между композицией и агрегацией.

---

### Этап 5. Теория: устройство памяти в JVM (~15 мин)

Heap / Non-Heap (Metaspace, Code Cache) / Stack — что где живёт; устройство объекта в памяти (Mark Word + Class Pointer + поля + padding, минимальный объект — 16 байт); где хранятся примитивы, объекты-обёртки и массивы; вопрос-ловушка `int[10][1000]` vs `int[1000][10]`.

Полный конспект лекции: [`lectures/jvm-memory.md`](lectures/jvm-memory.md). Расширенный материал для самостоятельного изучения: [`docs/materials/seminar1/java-process-memory-model.md`](../../materials/seminar1/java-process-memory-model.md).

---

### Этап 6. Практика: VisualVM и JOL (~25 мин)

#### 6.1. VisualVM — мониторинг в реальном времени (~10 мин)

Пишем простую программу, которая создаёт оба массива и держит их в памяти. Итоговый код: [`MemoryDemo.java`](../../../src/main/java/ru/tbank/marketpulse/demo/MemoryDemo.java)

```java
// demo/MemoryDemo.java
public class MemoryDemo {

    public static void main(String[] args) throws InterruptedException {
        System.out.println("Allocating int[10][1000]...");
        int[][] small = new int[10][1000];

        Thread.sleep(10_000);  // пауза — смотрим в VisualVM

        System.out.println("Allocating int[1000][10]...");
        int[][] large = new int[1000][10];

        Thread.sleep(10_000);  // пауза — смотрим снова

        System.out.println("Done. Press Enter to exit.");
        System.in.read();

        // Держим ссылки, чтобы GC не собрал
        System.out.println(small.length + " " + large.length);
    }
}
```

**Что смотрим в VisualVM:**

- Вкладка **Monitor** → Heap: как растёт при каждом `new`
- Вкладка **Heap Dump** (если студенты хотят глубже) → объекты типа `int[]`, их количество и суммарный размер

> **Акцент:** визуально видно, что `int[1000][10]` создаёт на порядок больше объектов в Heap, хотя данных — столько же.

#### 6.2. JOL — точные размеры (~15 мин)

**Подключение библиотеки через Maven.** Зависимость уже добавлена в [`advanced/pom.xml`](../../../pom.xml):

```xml
<!-- pom.xml -->
<dependency>
    <groupId>org.openjdk.jol</groupId>
    <artifactId>jol-core</artifactId>
    <version>0.17</version>
</dependency>
```

> **Акцент:** это хороший момент объяснить, что такое Maven-координаты (`groupId:artifactId:version`), где их искать (Maven Central), и как IntelliJ IDEA автоматически скачивает зависимости после изменения `pom.xml`.

**Смотрим на наши сущности.** Итоговый код: [`JolDemo.java`](../../../src/main/java/ru/tbank/marketpulse/demo/JolDemo.java)

```java
// demo/JolDemo.java
import org.openjdk.jol.info.ClassLayout;
import org.openjdk.jol.info.GraphLayout;

public class JolDemo {

    public static void main(String[] args) {
        // Размер заголовка и layout пустого объекта
        System.out.println(ClassLayout.parseClass(Object.class).toPrintable());

        // Layout нашего Stock
        Stock stock = new Stock("SBER", "Сбербанк", Currency.RUB,
                                "Financials", new BigDecimal("6.5"));
        System.out.println(ClassLayout.parseInstance(stock).toPrintable());

        // Сравниваем размеры двух массивов
        int[][] small = new int[10][1000];
        int[][] large = new int[1000][10];

        System.out.println("int[10][1000] shallow size:  "
            + ClassLayout.parseInstance(small).instanceSize());
        System.out.println("int[1000][10] shallow size:  "
            + ClassLayout.parseInstance(large).instanceSize());

        // GraphLayout покажет полный граф — попробуйте сами!
        // System.out.println(GraphLayout.parseInstance(small).toFootprint());
        // System.out.println(GraphLayout.parseInstance(large).toFootprint());
    }
}
```

**Что разбираем в выводе JOL:**

```other
java.lang.Object object internals:
OFF  SZ   TYPE DESCRIPTION               VALUE
  0   8        (object header: mark)
  8   4        (object header: class)
 12   4        (object alignment padding)
Instance size: 16 bytes
```

- `OFF` — смещение в байтах
- `SZ` — размер поля
- Mark Word и Class Pointer — это и есть заголовок объекта

> **Намёк на GraphLayout:** `ClassLayout` показывает только сам объект (shallow). `GraphLayout.parseInstance(large).toFootprint()` покажет весь граф ссылок — суммарный размер всех вложенных массивов. Попробуйте после семинара: именно там будет видна разница между `int[10][1000]` и `int[1000][10]` в полном объёме.

---

## Исходный код

Структура пакетов:

```other
advanced/src/
└── main/
    └── java/
        └── ru/tbank/marketpulse/
            ├── model/
            │   ├── Currency.java
            │   ├── Instrument.java      ← абстрактный базовый класс
            │   ├── Stock.java
            │   ├── Bond.java
            │   ├── Etf.java
            │   ├── Quote.java           ← агрегация
            │   └── Portfolio.java       ← содержит вложенный класс Position (композиция)
            └── demo/
                ├── Main.java
                ├── MemoryDemo.java
                └── JolDemo.java
```

> Пакет `model` содержит доменные сущности. Пакет `demo` — временная точка входа для проверки модели и демонстрации поведения в памяти JVM; в следующих семинарах он будет заменён полноценным CLI.

Ссылки на итоговый код: [`model/`](../../../src/main/java/ru/tbank/marketpulse/model/), [`demo/`](../../../src/main/java/ru/tbank/marketpulse/demo/)

---

## Материалы

- Лекция (Этап 2): [`lectures/oop-principles.md`](lectures/oop-principles.md)
- Лекция (Этап 5): [`lectures/jvm-memory.md`](lectures/jvm-memory.md)
- Самостоятельное изучение: [`docs/materials/seminar1/oop.md`](../../materials/seminar1/oop.md), [`docs/materials/seminar1/java-process-memory-model.md`](../../materials/seminar1/java-process-memory-model.md)

---

## Навыки, полученные на семинаре

`Проектирование доменной модели` `Инкапсуляция и инварианты` `Наследование и иерархии типов` `Полиморфизм подтипов` `Абстрактные классы` `Принцип подстановки Лисков (LSP)` `Агрегация и композиция` `BigDecimal для финансовых расчётов` `Heap / Non-Heap / Stack в JVM` `Устройство объекта в памяти` `VisualVM — мониторинг в реальном времени` `JOL — анализ размеров объектов` `Подключение зависимостей через Maven`
