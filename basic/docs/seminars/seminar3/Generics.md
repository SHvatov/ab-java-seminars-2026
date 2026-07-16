# Generics

---

## Третий вид полиморфизма

Полиморфизм подтипов (`Instrument` → `Stock`/`Bond`/`Etf`) и перегрузка — два знакомых вида. Generics — третий:
**параметрический полиморфизм**. Код пишется один раз и работает с любым типом, который подставят в качестве
параметра.

---

## Мотивация

До появления generics (Java 5) коллекции хранили `Object`, и при извлечении элемента требовался явный каст:

```java
void example() {
    List list = new ArrayList();
    list.add("AAPL");
    String ticker = (String) list.get(0); // каст на совести программиста
}
```

Если положить в список не `String`, ошибка проявится не при компиляции, а в рантайме, при касте. Generics переносят
эту проверку на этап компиляции:

```java
void example() {
    List<String> list = new ArrayList<>();
    list.add("AAPL");
    String ticker = list.get(0); // каст не нужен, тип уже известен компилятору
}
```

`Iterator<Instrument>`, `Iterable<Instrument>`, `Predicate<Instrument>`, `Comparator<Instrument>` — всё это
generic-интерфейсы: `<Instrument>` — параметр типа, подставленный в объявление `Iterator<T>`, `Predicate<T>`,
`Comparator<T>`.

---

## Собственный generic-класс

Объявление параметра типа — в угловых скобках после имени класса:

```java
public class Box<T> {
    private T value;

    public void set(T value) {
        this.value = value;
    }

    public T get() {
        return value;
    }
}
```

---

## Bounded type parameter — `extends`

Иногда параметр типа должен быть не «любым», а подтипом конкретного класса или интерфейса:

```java
public static <T extends Instrument> void printAll(List<T> items) {
    for (T item : items) {
        System.out.println(item.getDescription()); // доступен метод Instrument, компилятор это знает
    }
}
```

Без `extends Instrument` компилятор не разрешил бы вызвать `getDescription()` — он не знал бы, что `T` — это вообще
`Instrument`. `extends` здесь означает «является подтипом», а не только «наследует класс» — так же работает и с
интерфейсами.

---

## Ключевое слово `super`

Обратное по смыслу ограничение: не «подтип», а «супертип» указанного. Используется с wildcard-параметром `?` в виде
`? super Instrument` — «этот тип или любой его предок». Если `extends` пригождается там, где значения нужно *читать*
(и точно знать, что они хотя бы `Instrument`), то `super` — там, где значения нужно *записывать* в структуру,
объявленную более общим типом.

---

Подробный разбор type erasure, мостовых методов и wildcard-типов (`? extends`, `? super`, правило PECS) — за рамками
базового трека.
