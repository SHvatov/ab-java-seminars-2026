# Generics: type erasure, bounded types, wildcard

---

## Третий вид полиморфизма

Полиморфизм подтипов (`Instrument` → `Stock`/`Bond`/`Etf`, семинар 1) и перегрузка — два знакомых вида. Generics —
третий: **параметрический полиморфизм**. Уже использовали неявно: `Iterator<Instrument>`, `Predicate<Instrument>`,
`Comparator<Instrument>`.

---

## Type erasure

В отличие от, например, C++ шаблонов, параметры типов в Java существуют только на этапе компиляции и «стираются» в
байт-коде: `List<String>` и `List<Integer>` в скомпилированном классе — это один и тот же `List`, разница видна только
компилятору при проверке типов.

Прямое следствие — невозможно создать `new T()` или `new T[]` внутри generic-класса, и невозможна перегрузка методов,
отличающихся только параметром типа:

```java
public void process(List<String> tickers) { }
public void process(List<Integer> counts) { } // ошибка компиляции: erasure of process(List<Integer>) is same as process(List<String>)
```

После стирания оба метода имеют одинаковую сигнатуру `process(List)` — для JVM это один и тот же метод, объявленный
дважды.

---

## Bounded type parameter — `extends`

Ограничивает `T` подтипами указанного класса или интерфейса, давая доступ к его методам внутри generic-кода:

```java
public static <T extends Instrument> List<T> topByTicker(List<T> items, int n) {
    List<T> sorted = new ArrayList<>(items);
    sorted.sort(Comparator.comparing(Instrument::getTicker));
    return sorted.subList(0, Math.min(n, sorted.size()));
}
```

---

## Wildcards: `? extends`, `? super` и правило PECS

Bounded type parameters (`<T extends Instrument>`) ограничивают параметр типа *метода или класса*. Wildcards (`?`)
решают другую задачу — описывают допустимый диапазон типов в *точке использования* generic-типа, обычно в параметре
метода.

`List<Stock>` не является подтипом `List<Instrument>`, даже если `Stock` — подтип `Instrument`, — generics
инвариантны. Если бы `List<Stock>` принимался как `List<Instrument>`, внутри метода можно было бы вызвать
`items.add(bond)` — и получить `Bond` внутри списка, объявленного как список `Stock`.

**Правило PECS** (*Producer Extends, Consumer Super*) — мнемоника для выбора между `extends` и `super`: если
параметризованная коллекция или объект только **производит** (отдаёт) элементы типа `T` вызывающему коду — используем
`? extends T`; если только **потребляет** (принимает) элементы — используем `? super T`.

```java
public static <T> void sortWith(List<T> items, Comparator<? super T> comparator) {
    items.sort(comparator);
}
```

`Comparator<? super T>` **потребляет** значения типа `T` для сравнения (метод `compare(T, T)` принимает элементы, а не
отдаёт их) — по PECS это `super`-случай. Благодаря этому `sortWith(stocks, comparatorForInstrument)` компилируется:
`Comparator<Instrument>` умеет сравнивать `Instrument`, а значит умеет сравнивать и его подтип `Stock`, то есть
подходит как `Comparator<? super Stock>`.

---

Подробный разбор мостовых методов (bridge methods), ограничений, порождённых erasure, множественных границ и raw
types — в материале [«Generics: устройство»](../../materials/seminar3/Generics%3A%20устройство.md).
