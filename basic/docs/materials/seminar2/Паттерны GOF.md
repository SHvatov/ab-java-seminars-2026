# Паттерны проектирования GoF

---

## Содержание

1. Что такое паттерны проектирования
2. Классификация: порождающие, структурные, поведенческие
3. Порождающие паттерны
4. Структурные паттерны
5. Поведенческие паттерны
6. Ссылки и источники

---

## 1. Что такое паттерны проектирования

**Паттерн проектирования** — типовое решение часто встречающейся проблемы проектирования программного обеспечения.
Паттерн не является готовым фрагментом кода, который можно скопировать в проект, — это скорее шаблон, который
адаптируется под конкретную задачу.

Термин связан с книгой *«Design Patterns: Elements of Reusable Object-Oriented Software»* (1994), написанной Эрихом
Гаммой, Ричардом Хелмом, Ральфом Джонсоном и Джоном Влиссидесом — авторов книги принято называть «Gang of Four» (GoF).
Книга описывает 23 паттерна, ставших общим словарём для обсуждения архитектурных решений: вместо описания структуры кода
словами достаточно назвать паттерн, чтобы собеседник, знакомый с каталогом, понял идею.

Возраст книги (более 30 лет) не делает каталог устаревшим: значительная часть паттернов GoF лежит в основе внутреннего
устройства современных фреймворков, включая Spring, — Singleton для разделяемых бинов, Factory Method и Proxy для
механизма Dependency Injection, Template Method и Strategy для конфигурируемого поведения.

---

## 2. Классификация: порождающие, структурные, поведенческие

Каталог GoF делит 23 паттерна на три группы по признаку решаемой задачи:

- **Порождающие (Creational)** — управляют процессом создания объектов, скрывая логику выбора конкретного класса и
  способ его инициализации от кода, который этот объект использует.
- **Структурные (Structural)** — определяют, как классы и объекты компонуются в более крупные структуры, обеспечивая
  гибкость состава системы.
- **Поведенческие (Behavioral)** — описывают, как объекты взаимодействуют и распределяют между собой ответственность за
  выполнение задачи.

Ниже разобраны наиболее часто встречающиеся на практике паттерны каждой группы — не все 23, а те, что действительно
регулярно применяются в промышленной разработке.

---

## 3. Порождающие паттерны

### Factory Method

**Суть.** Определяет интерфейс для создания объекта, позволяя подклассам или отдельным реализациям решать, объект какого
именно класса создать.

**Проблема.** Код, создающий объект через `new` в зависимости от условия, разрастается в цепочку `if/else` при каждом
новом варианте и нарушает OCP.

**Пример.** Отправка уведомления пользователю по одному из нескольких каналов, тип которого приходит строкой из
конфигурации:

```java
public interface NotificationFactory {
    Notification create(String recipient, String message);
}

public class EmailNotificationFactory implements NotificationFactory {
    @Override
    public Notification create(String recipient, String message) {
        return new EmailNotification(recipient, message);
    }
}

public final class NotificationFactories {
    private static final Map<String, NotificationFactory> REGISTRY = Map.of(
            "EMAIL", new EmailNotificationFactory(),
            "SMS", new SmsNotificationFactory()
    );

    public static Notification create(String channel, String recipient, String message) {
        return REGISTRY.get(channel.toUpperCase()).create(recipient, message);
    }
}
```

Добавление нового канала сводится к реализации новой фабрики и добавлению записи в реестр — существующий код не
изменяется.

> `Map` — часть Java Collection Framework; подробно рассматривается на семинаре 5. На данном этапе достаточно
> воспринимать его как ассоциативный контейнер «ключ → значение».

### Builder

**Суть.** Отделяет пошаговое конструирование сложного объекта от его представления, позволяя создавать объект с большим
числом обязательных и опциональных полей без телескопических конструкторов.

**Проблема.** Конструктор с несколькими опциональными параметрами превращается либо в набор перегрузок на каждую
комбинацию, либо в один конструктор с длинным списком аргументов, часть которых передаётся как `null` без пояснения, что
каждый из них означает.

**Пример.** Создание HTTP-запроса с обязательным URL и опциональными заголовками, телом и тайм-аутом:

```java
HttpRequest request = new HttpRequest.Builder("https://api.example.com/data")
        .header("Authorization", "Bearer token")
        .timeout(Duration.ofSeconds(5))
        .build();
```

Обязательные поля передаются через конструктор `Builder`, что делает их обязательность явной на уровне компиляции;
опциональные поля устанавливаются именованными методами со значениями по умолчанию.

> `Duration` — класс из пакета `java.time`; работа со временем в Java подробно не рассматривается в рамках курса и
> разбирается точечно, по мере необходимости.

### Singleton

**Суть.** Гарантирует, что у класса существует не более одного экземпляра, и предоставляет глобальную точку доступа к
нему.

**Проблема.** Некоторые сущности по своей природе должны быть представлены ровно одним объектом в рамках приложения —
например, единая точка конфигурации или общий пул соединений.

**Пример.** Класс конфигурации приложения, читаемый из файла один раз при первом обращении:

```java
public class AppConfig {
    private static volatile AppConfig instance;
    private final Properties properties;

    private AppConfig() {
        properties = loadFromFile();
    }

    public static AppConfig getInstance() {
        if (instance == null) {
            synchronized (AppConfig.class) {
                if (instance == null) {
                    instance = new AppConfig();
                }
            }
        }
        return instance;
    }
}
```

Singleton — один из наиболее часто критикуемых паттернов: неограниченный глобальный доступ к состоянию усложняет
тестирование и создаёт скрытые связи между модулями, которые не видны в сигнатурах методов. Применение оправдано только
для действительно уникальных в рамках процесса сущностей.

> Ключевые слова `volatile` и `synchronized`, а также паттерн двойной проверки блокировки (double-checked locking),
> используемые в примере, относятся к многопоточному программированию — тема подробно разбирается на семинарах 10–11.
> Класс `Properties` — часть Java Collection Framework, семинар 5.

---

## 4. Структурные паттерны

### Adapter

**Суть.** Преобразует интерфейс одного класса в интерфейс, ожидаемый клиентским кодом, позволяя классам с несовместимыми
интерфейсами работать вместе.

**Проблема.** Внешняя библиотека или API возвращает данные в формате, не совпадающем с интерфейсом, ожидаемым остальной
частью приложения, а изменить исходный класс невозможно или нежелательно.

**Пример.** Сторонняя библиотека логирования имеет собственный интерфейс, несовместимый с интерфейсом логирования,
принятым в проекте:

```java
public class ThirdPartyLoggerAdapter implements AppLogger {
    private final ThirdPartyLogger delegate;

    public ThirdPartyLoggerAdapter(ThirdPartyLogger delegate) {
        this.delegate = delegate;
    }

    @Override
    public void info(String message) {
        delegate.logMessage(LogLevel.INFO, message);
    }
}
```

Код, работающий с `AppLogger`, не зависит от интерфейса сторонней библиотеки; при смене библиотеки логирования меняется
только адаптер.

### Facade

**Суть.** Предоставляет упрощённый интерфейс к сложной подсистеме, скрывая детали взаимодействия нескольких компонентов
за одним понятным методом.

**Проблема.** Клиентский код вынужден напрямую координировать работу нескольких классов подсистемы, зная детали их
взаимодействия и порядок вызовов.

**Пример.** Запуск компьютера требует согласованной инициализации нескольких подсистем в определённом порядке:

```java
public class ComputerFacade {
    private final CPU cpu = new CPU();
    private final Memory memory = new Memory();
    private final HardDrive hardDrive = new HardDrive();

    public void start() {
        cpu.freeze();
        memory.load(hardDrive.read(BOOT_ADDRESS, SECTOR_SIZE));
        cpu.execute();
    }
}
```

Клиентский код вызывает единственный метод `start()`, не зная о существовании `CPU`, `Memory` и `HardDrive` по
отдельности.

### Decorator

**Суть.** Динамически добавляет объекту новую функциональность, оборачивая его в другой объект с тем же интерфейсом, —
гибкая альтернатива созданию подклассов под каждую комбинацию поведения.

**Проблема.** Расширение поведения через наследование требует отдельного подкласса на каждую комбинацию дополнительных
возможностей; число подклассов растёт комбинаторно.

**Пример.** Стандартная библиотека Java использует Decorator в пакете `java.io`: `BufferedInputStream` оборачивает
`FileInputStream`, добавляя буферизацию, не изменяя ни `FileInputStream`, ни интерфейс `InputStream`:

```java
InputStream stream = new BufferedInputStream(new FileInputStream("data.txt"));
```

Тот же принцип применяется, например, для добавления кэширования или логирования к объекту, реализующему общий
интерфейс, — без создания отдельного подкласса под каждую комбинацию возможностей.

> Классы `InputStream`, `FileInputStream` и `BufferedInputStream` относятся к пакету `java.io`; работа с вводом-выводом
> подробно рассматривается на семинаре 7.

### Proxy

**Суть.** Предоставляет объект-заместитель, контролирующий доступ к другому объекту — для отложенной инициализации,
контроля доступа, логирования или кэширования.

**Проблема.** Прямое обращение к ресурсоёмкому или удалённому объекту (сетевой клиент, тяжёлая инициализация)
нежелательно выполнять каждый раз, когда объект просто упоминается в коде.

**Пример.** Изображение высокого разрешения загружается с диска только при первом реальном обращении к нему (ленивая
инициализация), хотя объект-заместитель создаётся сразу:

```java
public class ProxyImage implements Image {
    private final String filename;
    private RealImage realImage;

    @Override
    public void display() {
        if (realImage == null) {
            realImage = new RealImage(filename);
        }
        realImage.display();
    }
}
```

Код, вызывающий `display()`, не отличает `ProxyImage` от `RealImage` — оба реализуют один и тот же интерфейс.

---

## 5. Поведенческие паттерны

### Command

**Суть.** Инкапсулирует запрос как объект, позволяя параметризовать вызывающий код разными запросами, ставить их в
очередь, логировать или поддерживать отмену.

**Проблема.** Прямой вызов метода получателя из кода отправителя жёстко связывает их между собой и не оставляет
возможности отложить выполнение, поставить действие в очередь или отменить его.

**Пример.** Классический пример — пульт управления, кнопки которого не знают, каким именно устройством они управляют:

```java
public interface Command {
    void execute();
}

public class LightOnCommand implements Command {
    private final Light light;

    public LightOnCommand(Light light) {
        this.light = light;
    }

    @Override
    public void execute() {
        light.turnOn();
    }
}

public class RemoteControl {
    public void pressButton(Command command) {
        command.execute();
    }
}
```

`RemoteControl` работает с любым объектом, реализующим `Command`, не зная, включает ли конкретная команда свет,
открывает дверь или запускает музыку.

### Iterator

**Суть.** Предоставляет стандартный способ последовательного доступа к элементам коллекции, не раскрывая её внутреннее
представление (массив, список, дерево).

**Проблема.** Код, обходящий коллекцию, вынужден знать её внутреннее устройство (индексацию массива, структуру связного
списка), из-за чего смена структуры хранения ломает весь обходящий код.

**Пример.** Собственная коллекция, реализующая стандартный интерфейс `Iterable`, скрывает способ хранения элементов от
кода, который её обходит:

```java
public class NameCollection implements Iterable<String> {
    private final String[] names;

    @Override
    public Iterator<String> iterator() {
        return new Iterator<>() {
            private int index = 0;

            @Override
            public boolean hasNext() {
                return index < names.length;
            }

            @Override
            public String next() {
                return names[index++];
            }
        };
    }
}
```

Смена внутреннего хранилища с массива на `ArrayList` не потребует изменения кода, использующего `for-each` по
`NameCollection`.

> `ArrayList` — реализация списка из Java Collection Framework; подробно рассматривается на семинаре 5.

### Strategy

**Суть.** Определяет семейство взаимозаменяемых алгоритмов, инкапсулируя каждый в отдельный объект с общим интерфейсом.

**Проблема.** Выбор между несколькими вариантами поведения, реализованный через условный оператор внутри метода,
разрастается при добавлении каждого нового варианта и нарушает OCP.

**Пример.** Оплата заказа одним из нескольких способов, каждый инкапсулирован в собственную реализацию:

```java
public interface PaymentStrategy {
    void pay(BigDecimal amount);
}

public class CreditCardPayment implements PaymentStrategy {
    @Override
    public void pay(BigDecimal amount) {
        // списание с карты
    }
}

public class Order {
    private PaymentStrategy paymentStrategy;

    public void setPaymentStrategy(PaymentStrategy strategy) {
        this.paymentStrategy = strategy;
    }

    public void checkout(BigDecimal amount) {
        paymentStrategy.pay(amount);
    }
}
```

Добавление нового способа оплаты сводится к реализации нового `PaymentStrategy` — код `Order` не изменяется.

### Template Method

**Суть.** Определяет скелет алгоритма в базовом классе, оставляя реализацию отдельных шагов подклассам — структура
алгоритма фиксирована, детали варьируются.

**Проблема.** Несколько классов реализуют похожий по структуре процесс, дублируя общую последовательность шагов в каждой
реализации.

**Пример.** Приготовление напитка состоит из фиксированной последовательности шагов, часть которых различается между
чаем и кофе:

```java
public abstract class CaffeineBeverage {
    public final void prepare() {
        boilWater();
        brew();
        pourInCup();
        addCondiments();
    }

    protected abstract void brew();

    protected abstract void addCondiments();

    private void boilWater() { /* общий шаг */ }

    private void pourInCup() { /* общий шаг */ }
}

public class Tea extends CaffeineBeverage {
    @Override
    protected void brew() { /* заварить чай */ }

    @Override
    protected void addCondiments() { /* добавить лимон */ }
}
```

Метод `prepare()` фиксирует порядок шагов и не может быть переопределён; варьируются только `brew()` и
`addCondiments()`.

### Observer

**Суть.** Устанавливает зависимость «один ко многим»: при изменении состояния одного объекта все подписанные объекты
уведомляются автоматически.

**Проблема.** Компонентам, заинтересованным в изменении состояния другого объекта, приходится либо опрашивать его
состояние в цикле, либо объект-источник должен знать заранее обо всех заинтересованных получателях.

**Пример.** Метеостанция уведомляет все подписанные экраны об изменении показаний, не зная заранее, сколько экранов
подписано и что именно они делают с данными:

```java
public interface Observer {
    void update(float temperature);
}

public class WeatherStation {
    private final List<Observer> observers = new ArrayList<>();

    public void subscribe(Observer observer) {
        observers.add(observer);
    }

    public void setTemperature(float temperature) {
        for (Observer observer : observers) {
            observer.update(temperature);
        }
    }
}
```

> `List` и `ArrayList` — часть Java Collection Framework; подробно рассматриваются на семинаре 5.

### Chain of Responsibility

**Суть.** Передаёт запрос по цепочке обработчиков, каждый из которых решает, обработать запрос самому или передать
дальше по цепочке.

**Проблема.** Запрос должен пройти через несколько потенциальных обработчиков, но отправитель не должен знать заранее,
какой из них в итоге обработает запрос.

**Пример.** Обращение в службу поддержки эскалируется по уровням, пока не встретит обработчика, способного его закрыть:

```java
public abstract class SupportHandler {
    protected SupportHandler next;

    public void setNext(SupportHandler next) {
        this.next = next;
    }

    public void handle(Ticket ticket) {
        if (canHandle(ticket)) {
            resolve(ticket);
        } else if (next != null) {
            next.handle(ticket);
        }
    }

    protected abstract boolean canHandle(Ticket ticket);

    protected abstract void resolve(Ticket ticket);
}
```

Отправитель обращения вызывает `handle()` на первом обработчике цепочки, не зная, какой уровень поддержки в итоге
закроет обращение. Паттерн избыточен там, где эквивалентную маршрутизацию уже предоставляет используемый фреймворк или
библиотека — например, механизм подкоманд в CLI-библиотеках или цепочка middleware в веб-фреймворках; введение отдельной
реализации поверх уже работающей маршрутизации в таком случае дублирует функциональность.

---

## 6. Ссылки и источники

- Gamma E., Helm R., Johnson R., Vlissides J. *Design Patterns: Elements of Reusable Object-Oriented Software* (1994) —
  исходный каталог 23 паттернов.
- [Refactoring.Guru — Design Patterns](https://refactoring.guru/design-patterns) — каталог паттернов с классификацией по
  трём группам.
- [DigitalOcean — Gang of 4 Design Patterns Explained](https://www.digitalocean.com/community/tutorials/gangs-of-four-gof-design-patterns) —
  обзор применения паттернов в современных фреймворках.
- [Spring Framework Guru — Gang of Four Design Patterns](https://springframework.guru/gang-of-four-design-patterns/) —
  паттерны в контексте Spring.
- [dev.to — The Gang of Four (GoF) Design Patterns: A Developer's Guide](https://dev.to/lovestaco/the-gang-of-four-gof-design-patterns-a-developers-guide-473a) —
  краткий обзор трёх категорий паттернов.
- [Martin Fowler — bliki: Gang Of Four](https://martinfowler.com/bliki/GangOfFour.html) — заметка об историческом
  значении книги GoF.

