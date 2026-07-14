# Generics: устройство

# Generics: устройство

> Сопроводительный материал к семинару 3 (продвинутый трек).

---

## Содержание

1. Зачем понадобились generics
2. Type erasure: как компилятор стирает параметры типов
3. Мостовые методы (bridge methods)
4. Ограничения, порождённые erasure
5. Bounded type parameters: extends и множественные границы
6. Wildcards: ? extends, ? super и правило PECS
7. Raw types и heap pollution

---

## 1. Зачем понадобились generics

До Java 5 коллекции стандартной библиотеки хранили `Object` — единственный тип, общий для всего. Чтобы получить обратно значение конкретного типа, требовался явный каст, и ошибка типа проявлялась не при компиляции, а в рантайме, в момент каста:

```java
List list = new ArrayList();
list.add("AAPL");
list.add(42); // компилятор это разрешит — тип элементов list никак не ограничен

String ticker = (String) list.get(1); // ClassCastException в рантайме — а могли бы узнать раньше
```

Generics вводят параметр типа в объявление класса или метода, позволяя компилятору проверять типы на этапе компиляции:

```java
List<String> list = new ArrayList<>();
list.add("AAPL");
list.add(42); // ошибка компиляции — не дойдёт даже до запуска

String ticker = list.get(0); // каст не нужен — компилятор уже знает тип
```

---

## 2. Type erasure: как компилятор стирает параметры типов

Ключевое решение разработчиков Java при добавлении generics в версии 5 — реализовать их через *стирание типов* (type erasure), а не через специализацию (как, например, шаблоны C++, которые генерируют отдельный машинный код для каждой инстанциации). Причина — обратная совместимость: огромное количество уже скомпилированного байт-кода до Java 5 не должно было сломаться при переходе на новую версию.

Стирание означает: параметры типов существуют только на этапе компиляции, для проверки типов и вывода нужных кастов. В скомпилированном байт-коде параметр типа заменяется на его верхнюю границу (`Object`, если граница не указана явно, или указанный `extends`-тип) — а вызывающий код получает автоматически вставленные компилятором касты:

```java
// то, что пишет разработчик
List<String> list = new ArrayList<>();
list.add("AAPL");
String ticker = list.get(0);

// приблизительный эквивалент байт-кода после стирания
List list = new ArrayList();
list.add("AAPL");
String ticker = (String) list.get(0); // каст вставлен компилятором автоматически
```

`List<String>` и `List<Integer>` — это **один и тот же** класс `java.util.List` в рантайме; различие между ними существует только в исходном коде и в проверках компилятора. Убедиться в этом можно через рефлексию:

```java
List<String> strings = new ArrayList<>();
List<Integer> integers = new ArrayList<>();
System.out.println(strings.getClass() == integers.getClass()); // true
```

---

## 3. Мостовые методы (bridge methods)

Стирание типов создаёт проблему при переопределении generic-методов в подклассах. Рассмотрим generic-интерфейс и его реализацию:

```java
public interface Converter<T> {
    T convert(String input);
}

public class TickerConverter implements Converter<String> {
    @Override
    public String convert(String input) {
        return input.toUpperCase();
    }
}
```

После стирания типов метод в интерфейсе `Converter` выглядит как `Object convert(String)` (граница параметра `T` не указана — верхняя граница `Object`). Но `TickerConverter.convert` объявлен как `String convert(String)` — с точки зрения JVM (для которой типы уже стёрты) это **разные** сигнатуры, а значит формально `TickerConverter` не переопределяет метод интерфейса, а перегружает его.

Чтобы механизм переопределения (полиморфизм подтипов — семинар 1) продолжал работать корректно, компилятор генерирует дополнительный синтетический *мостовой метод* — метод-обёртку с сигнатурой, совпадающей со стёртой сигнатурой интерфейса, делегирующий вызов в реальную реализацию:

```java
// сгенерированный компилятором мостовой метод (не пишется разработчиком)
public Object convert(String input) {
    return convert(input); // делегирует в String convert(String)
}
```

Мостовые методы не видны в исходном коде и обычно не требуют внимания на практике — но объясняют, почему `TickerConverter.class.getDeclaredMethods()` в рефлексии может показать два метода `convert` вместо одного, что иногда удивляет при отладке через рефлексию или при работе с байт-код-инструментами.

---

## 4. Ограничения, порождённые erasure

Информация о параметре типа недоступна в рантайме — отсюда набор ограничений, которые иначе выглядели бы произвольными:

**Нельзя создать экземпляр параметра типа.**

```java
public class Box<T> {
    private T value = new T(); // ошибка компиляции: Cannot instantiate the type T
}
```

JVM не знает во время выполнения, что такое `T` — стёрто до `Object`, вызвать конструктор неизвестного типа невозможно.

**Нельзя создать generic-массив.**

```java
public class Box<T> {
    private T[] items = new T[10]; // ошибка компиляции: Cannot create a generic array of T
}
```

Массивы в Java, в отличие от `List`, хранят информацию о своём типе элементов в рантайме (`reifiable type`) и проверяют её при каждой записи (`ArrayStoreException`, если тип не совпадает). Стёртый `T` этой информации предоставить не может — совмещение стёртых generic-типов с массивами, которые типы не стирают, было бы небезопасным. Стандартное решение — использовать `List<T>` вместо массива внутри generic-класса либо создавать массив как `Object[]` с последующим unchecked-кастом.

**Нельзя использовать `instanceof` с параметризованным типом.**

```java
if (list instanceof List<String>) { } // ошибка компиляции
if (list instanceof List<?>) { }      // допустимо — неизвестный параметр типа не проверяется
```

Проверить в рантайме, что список содержит именно `String`, невозможно — информация стёрта; можно проверить только то, что объект вообще является `List`, независимо от параметра.

**Нельзя перегружать методы, отличающиеся только параметром типа.**

```java
public void process(List<String> tickers) { }
public void process(List<Integer> counts) { } // ошибка компиляции: erasure of process(List<Integer>) is same as process(List<String>)
```

После стирания оба метода имеют одинаковую сигнатуру `process(List)` — для JVM это один и тот же метод, объявленный дважды.

**Нельзя иметь статическое поле, типизированное параметром класса.**

```java
public class Box<T> {
    private static T instance; // ошибка компиляции: non-static type variable T cannot be referenced from a static context
}
```

Параметр типа `T` привязан к конкретному экземпляру `Box`, а статическое поле общее для всех экземпляров разом — у которых `T` может быть разным (`Box<String>` и `Box<Integer>` — один класс `Box`, но разные экземпляры с разными подставленными `T`).

---

## 5. Bounded type parameters: extends и множественные границы

Без ограничения параметр типа по умолчанию ведёт себя как `Object` — доступны только методы `Object` (`toString`, `equals`, `hashCode`):

```java
public static <T> void printAll(List<T> items) {
    for (T item : items) {
        System.out.println(item); // доступен только toString() из Object
    }
}
```

`extends` сужает параметр до подтипа указанного класса или интерфейса, открывая доступ к его методам:

```java
public static <T extends Instrument> void printDescriptions(List<T> items) {
    for (T item : items) {
        System.out.println(item.getDescription()); // getDescription() доступен, T — точно Instrument или его подтип
    }
}
```

Несмотря на ключевое слово `extends`, оно работает одинаково и для наследования от класса, и для реализации интерфейса — в контексте bounded type parameters `extends` означает «является подтипом», а не только «расширяет класс».

**Множественные границы** — параметр типа может быть ограничен одновременно несколькими интерфейсами (не более чем одним классом, и класс, если есть, должен идти первым):

```java
public static <T extends Instrument & Comparable<T>> T max(List<T> items) {
    T result = items.get(0);
    for (T item : items) {
        if (item.compareTo(result) > 0) {
            result = item;
        }
    }
    return result;
}
```

`T extends Instrument & Comparable<T>` — `T` должен одновременно быть подтипом `Instrument` (для доступа к доменным методам) и реализовывать `Comparable<T>` (для сравнения через `compareTo`).

---

## 6. Wildcards: ? extends, ? super и правило PECS

Bounded type parameters (`<T extends Instrument>`) ограничивают параметр типа *метода или класса*. Wildcards (`?`) решают другую задачу — описывают допустимый диапазон типов в *точке использования* generic-типа, обычно в параметре метода.

**Проблема, которую решают wildcards.** `List<Stock>` не является подтипом `List<Instrument>`, даже если `Stock` — подтип `Instrument`, — generics инвариантны:

```java
public static void printAll(List<Instrument> items) {
    for (Instrument item : items) {
        System.out.println(item.getDescription());
    }
}

List<Stock> stocks = List.of(stock1, stock2);
printAll(stocks); // ошибка компиляции: List<Stock> не является List<Instrument>
```

Инвариантность — намеренное решение, а не недосмотр: если бы `List<Stock>` принимался как `List<Instrument>`, внутри `printAll` можно было бы вызвать `items.add(bond)` — и получить `Bond` внутри списка, объявленного как список `Stock`, что нарушило бы безопасность типов и привело бы к `ArrayStoreException`-подобной ситуации без единого explicit-каста.

**`? extends T`** — «список чего-то, что является подтипом `T`, но неизвестно, какого именно» — допускает *чтение* элементов как `T`, но запрещает *запись* (кроме `null`), поскольку компилятор не знает точный подтип:

```java
public static void printAll(List<? extends Instrument> items) {
    for (Instrument item : items) { // чтение — можно, любой подтип Instrument безопасно читается как Instrument
        System.out.println(item.getDescription());
    }
    // items.add(bond); // ошибка компиляции — компилятор не знает, что именно List<? extends Instrument> хранит на самом деле
}

printAll(stocks); // теперь компилируется: List<Stock> — это List<? extends Instrument>
```

**`? super T`** — «список чего-то, что является супертипом `T`» — наоборот, допускает *запись* элементов типа `T` (или его подтипов), но чтение возможно только как `Object`, поскольку компилятор знает лишь нижнюю границу:

```java
public static void addStock(List<? super Stock> items) {
    items.add(stock); // запись — можно: Stock точно подходит под любой супертип Stock
    // Stock s = items.get(0); // ошибка компиляции — тип элемента неизвестен точнее, чем Object
}

List<Instrument> instruments = new ArrayList<>();
addStock(instruments); // List<Instrument> — это List<? super Stock>, компилируется
```

**Правило PECS** (*Producer Extends, Consumer Super*) — мнемоника для выбора между `extends` и `super`: если параметризованная коллекция или объект только **производит** (отдаёт) элементы типа `T` вызывающему коду — используем `? extends T`; если только **потребляет** (принимает) элементы — используем `? super T`. Практический пример — уже встречавшийся на семинаре `Comparator`:

```java
public static <T> void sortWith(List<T> items, Comparator<? super T> comparator) {
    items.sort(comparator);
}
```

`Comparator<? super T>` **потребляет** значения типа `T` для сравнения (метод `compare(T, T)` принимает элементы, а не отдаёт их) — по PECS это `super`-случай. Благодаря этому `sortWith(stocks, comparatorForInstrument)` компилируется: `Comparator<Instrument>` умеет сравнивать `Instrument`, а значит умеет сравнивать и его подтип `Stock`, то есть подходит как `Comparator<? super Stock>`.

---

## 7. Raw types и heap pollution

**Raw type** — использование generic-класса без указания параметра типа вообще (`List` вместо `List<String>`). Разрешено ради обратной совместимости с кодом, написанным до Java 5, но компилятор выдаёт предупреждение `unchecked` и фактически отключает проверку типов для такой переменной — поведение как до появления generics:

```java
List list = new ArrayList(); // raw type — компилятор предупредит: unchecked call
list.add("AAPL");
list.add(42); // компилируется без ошибки, только с warning — компилятор больше не проверяет
```

**Heap pollution** — ситуация, когда переменная параметризованного типа фактически ссылается на объект с другим параметром типа, обычно как следствие смешения raw types и generic-типов или unchecked-кастов:

```java
@SuppressWarnings("unchecked")
List<String> strings = (List<String>) (List) List.of(1, 2, 3); // unchecked cast — компилятор доверяет разработчику

String s = strings.get(0); // ClassCastException в рантайме — ровно то, от чего должны были защищать generics
```

`@SuppressWarnings("unchecked")` не делает код безопаснее — он лишь подтверждает компилятору, что разработчик сознательно берёт на себя ответственность за корректность типов в месте, которое компилятор проверить не может. Появление этой аннотации в коде — сигнал внимательно перепроверить логику вручную, а не способ избавиться от «лишнего» предупреждения.