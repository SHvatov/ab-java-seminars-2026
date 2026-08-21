# Optional

---

## Проблема, которую решает Optional

Метод, возвращающий `null` при отсутствии значения, — классический источник `NullPointerException` для тех, кто
забудет проверить результат:

```java
public Instrument findByTicker(String ticker) {
    for (Instrument instrument : this) {
        if (instrument.getTicker().equalsIgnoreCase(ticker)) {
            return instrument;
        }
    }
    return null;
}
```

`Optional<T>` — контейнер, который либо содержит значение, либо пуст. В отличие от `null`, само наличие `Optional` в
сигнатуре метода явно предупреждает вызывающий код: результат может отсутствовать, и это нужно обработать.

```java
public Optional<Instrument> findByTicker(String ticker) {
    for (Instrument instrument : this) {
        if (instrument.getTicker().equalsIgnoreCase(ticker)) {
            return Optional.of(instrument);
        }
    }
    return Optional.empty();
}
```

---

## Базовый API

Без стримов — `Optional` в сочетании со `Stream API` рассматривается отдельно, позже по курсу.

| **Метод**                    | **Смысл**                                    |
|-------------------------------|-----------------------------------------------|
| `Optional.of(value)`         | Обернуть заведомо непустое значение          |
| `Optional.ofNullable(value)` | Обернуть значение, которое может быть `null` |
| `Optional.empty()`           | Пустой `Optional`                            |
| `isPresent()` / `isEmpty()`  | Проверка наличия значения                    |
| `orElse(default)`            | Значение по умолчанию, если пусто            |
| `orElseThrow(...)`           | Бросить исключение, если пусто               |
| `ifPresent(consumer)`        | Выполнить действие, если значение есть       |
| `map(function)`              | Преобразовать значение, если оно есть        |

Пример использования — обработка результата поиска без единой проверки на `null`:

```java
public Integer call() {
    return repository.findByTicker(ticker)
            .map(instrument -> {
                System.out.println(instrument.getDescription());
                return 0;
            })
            .orElseGet(() -> {
                System.out.println("Инструмент не найден: " + ticker);
                return 1;
            });
}
```

`orElse(default)` вычисляет `default` всегда, даже когда значение присутствует. Для дорогих вычислений или побочных
эффектов нужен именно `orElseGet(Supplier)`, вычисляющий значение лениво, только при пустом `Optional`.

---

## Где Optional неуместен

`Optional` — это тип **возвращаемого значения**, обозначающий «результата может не быть». Использовать его как тип
поля класса или параметра метода — общепринятая антипрактика: поле и так может быть `null`-проверено обычным способом,
а `Optional`-параметр только добавляет лишний уровень оборачивания на каждый вызов.
