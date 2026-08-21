# Optional

---

## Проблема, которую решает Optional

Метод, возвращающий `null` при отсутствии значения, — классический источник `NullPointerException` для тех, кто
забудет проверить результат. `Optional<T>` — контейнер, который либо содержит значение, либо пуст. В отличие от
`null`, само наличие `Optional` в сигнатуре метода явно предупреждает вызывающий код: результат может отсутствовать, и
это нужно обработать.

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

## orElse vs orElseGet

```java
@Override
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

`orElse(default)` вычисляет `default` всегда, даже когда значение присутствует — аргумент передаётся по значению и
вычисляется в момент вызова `orElse`, независимо от того, понадобится ли он. Для дорогих вычислений или побочных
эффектов нужен именно `orElseGet(Supplier)`, вычисляющий значение лениво — только при пустом `Optional`. Это тот
случай, где `Supplier<T>` объясняет, почему у `Optional` есть два похожих метода, а не один.

---

## Где Optional неуместен

`Optional` — это тип **возвращаемого значения**, обозначающий «результата может не быть». Использовать его как тип
поля класса или параметра метода — общепринятая антипрактика: поле и так может быть `null`-проверено обычным способом,
а `Optional`-параметр только добавляет лишний уровень оборачивания на каждый вызов.
