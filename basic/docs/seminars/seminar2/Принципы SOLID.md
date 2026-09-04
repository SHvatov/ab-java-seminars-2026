# Принципы SOLID

---

## Зачем нужны принципы поверх ООП

Механизмы ООП — наследование, интерфейсы, инкапсуляция — допускают как удачное, так и неудачное применение; сам по себе
язык не проверяет качество дизайна. SOLID — пять правил, которые делают код устойчивым к изменению и расширению:

- **S** — Single Responsibility Principle
- **O** — Open/Closed Principle
- **L** — Liskov Substitution Principle
- **I** — Interface Segregation Principle
- **D** — Dependency Inversion Principle

---

## LSP — принцип подстановки Лисков

Объект подтипа должен быть заменяем объектом базового типа без нарушения корректности программы. Метод базового класса
задаёт контракт — подкласс не вправе его сужать.

**Нарушение в проекте:** `getDividends(BigDecimal)` в `Instrument` не имеет осмысленной реализации для `Bond`:

```java
void example() {
    // оба варианта искажают контракт
    return BigDecimal.ZERO;                             // молчаливая подмена
    // throw new UnsupportedOperationException("...");  // громкий отказ
}
```

**Исправление** — вынести операцию в отдельную абстракцию, реализуемую только там, где она осмысленна:

```java
public interface DividendPaying {
    BigDecimal getDividends(BigDecimal shareCount);
}

public class Stock extends Instrument implements DividendPaying {
    @Override
    public BigDecimal getDividends(BigDecimal shareCount) {
        return dividendPerShare.multiply(shareCount);
    }
}
```

`Bond` интерфейс `DividendPaying` не реализует — некорректный вызов становится невозможен уже на уровне компиляции.

---

## ISP — принцип разделения интерфейса

Клиенты не должны зависеть от методов, которые не используют. Единый «толстый» интерфейс `Instrument` со всеми
возможными операциями заставил бы каждый подкласс реализовывать чужие обязанности — то же нарушение LSP, только по
другой причине.

```java
public interface DividendPaying {
    BigDecimal getDividends(BigDecimal shareCount);
}

public interface CouponBearing {
    BigDecimal getCouponYield();

    LocalDate getMaturityDate();
}

public interface Diversified {
    List<Instrument> getUnderlyingAssets();
}
```

Класс реализует только те интерфейсы, что соответствуют его природе: `Stock implements DividendPaying`,
`Bond implements CouponBearing`, `Etf implements Diversified`.

---

## SRP — принцип единственной ответственности

У класса должна быть одна причина для изменения. Класс, который одновременно считает доменную логику и форматирует
вывод, нарушает SRP так же, как CLI-команда, смешивающая парсинг аргументов с обращением к данным:

```java
public class Portfolio {
    public BigDecimal totalValue(Map<String, BigDecimal> prices) { /* доменная логика */ }

    public void printReport() { /* вторая причина для изменения — вынести отдельно */ }
}
```

Чем больше изменений - тем больше багов! Чем больше багов, тем меньше ЗП...

---

## OCP — принцип открытости/закрытости

Код должен быть открыт для расширения, но закрыт для изменения. `instanceof`-цепочка требовала правки при каждом новом
типе инструмента:

```java
void example() {
    if (type == InstrumentType.STOCK) return "Акция";
    else if (type == InstrumentType.BOND) return "Облигация";
    else throw new IllegalStateException("Unknown type");
}
```

Полиморфный `getDescription()` устраняет проблему: новый подкласс `Instrument` не требует изменения существующего кода.
Тот же принцип сегодня проверяется на Factory Method и Strategy — добавление нового типа или правила отбора сводится к
добавлению нового класса.

---

## DIP — принцип инверсии зависимостей (обзорно)

Модули верхнего уровня не должны зависеть от модулей нижнего уровня — оба зависят от абстракций.
Полноценно раскрывается при знакомстве с фреймворками, управляющими внедрением зависимостей — Spring, далее по курсу.

---

## KISS и DRY

**KISS** (Keep It Simple, Stupid) — простое решение предпочтительнее сложного при прочих равных: паттерн оправдан, когда
решает реальную проблему, а не потому, что был недавно изучен.

**DRY** (Don't Repeat Yourself) — устраняется дублирование *знания*, а не текста. Похожие, но независимо изменяющиеся
фрагменты кода не обязательно нарушают DRY.

---

Подробнее — в материале [«Принципы SOLID»](../../materials/seminar2/Принципы%20SOLID.md),
включая связь принципов друг с другом и с механизмами ООП.
