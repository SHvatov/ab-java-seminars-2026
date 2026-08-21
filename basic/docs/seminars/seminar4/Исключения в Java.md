# Исключения в Java

---

## Коды возврата и sentinel values

Классический до-исключений подход: функция возвращает не результат, а признак ошибки — `-1`, `null`, глобальную
переменную вроде `errno` в C. Явный и дешёвый, но проверку результата ничто не гарантирует — компилятор не заставит,
и при цепочке вызовов легко забыть проверку на одном из шагов.

```
int fd = open("data.txt", O_RDONLY);
if (fd == -1) {
    perror("open failed"); // errno нужно проверить прямо сейчас, до следующего вызова
}
```

Java не свободна от того же паттерна там, где исключения исторически не использовались: `String.indexOf` возвращает
`-1`, `Map.get` возвращает `null` — тот же sentinel value в объектно-ориентированной обёртке.

---

## Исключения

`throw` прерывает нормальное выполнение и передаёт объект `Throwable` вверх по стеку вызовов, минуя все промежуточные
фреймы без подходящего `catch`, пока не найдётся обработчик — или пока исключение не долетит до самого верха и не
завершит поток. Ключевое отличие от кода возврата: исключение **нельзя случайно проигнорировать**.

```java
// стиль с кодами возврата — проверка после каждого шага
Instrument instrument = tryCreate(type, ticker, name, currency);
if (instrument == null) {
    return handleError();
}

// стиль с исключениями — линейный happy path, ошибки в одном месте
try {
    Instrument instrument = create(type, ticker, name, currency);
    repository.add(instrument);
} catch (IllegalArgumentException | DuplicateTickerException e) {
    return handleError(e);
}
```

---

## Иерархия Throwable

```
Throwable
 ├── Error                     — проблемы уровня JVM (OutOfMemoryError, StackOverflowError)
 │                                обычно не перехватываются приложением
 └── Exception
      ├── RuntimeException     — unchecked: NullPointerException, IllegalArgumentException,
      │                          IllegalStateException, NoSuchElementException
      └── остальные Exception  — checked: IOException и подобные
```

`Error` — уровень самой JVM, ловить в прикладном коде почти никогда не имеет смысла. `RuntimeException` (unchecked) —
программные ошибки, от которых вызывающий код обычно не ожидает восстановиться иначе, чем исправив сам вызов.

---

## Checked vs unchecked

Checked-исключения (`Exception`, кроме `RuntimeException`) компилятор требует либо поймать, либо объявить в `throws`.
Unchecked такого требования не несут. Вокруг checked-исключений — давняя дискуссия в Java-сообществе: они хорошо
документируют ожидаемые ошибки в сигнатуре, но плохо масштабируются по глубине вызовов, если обработать ошибку
осмысленно может только код на самом верхнем уровне. Ни C#, ни Kotlin не реализуют checked-исключения вовсе.

---

## Проектирование собственных исключений

Заводить свой тип оправдано, когда вызывающему коду важно программно отличить эту ошибку от прочих или нужен
структурированный контекст. В остальных случаях достаточно `IllegalArgumentException`/`IllegalStateException`. В
подавляющем большинстве случаев — наследоваться от `RuntimeException`, не от `Exception`/`Throwable` напрямую.

```java
public class DuplicateTickerException extends RuntimeException {

    private final String ticker;

    public DuplicateTickerException(String ticker) {
        super("Инструмент с тикером уже существует: " + ticker);
        this.ticker = ticker;
    }

    public String getTicker() {
        return ticker;
    }
}
```

**Не терять причину.** Если исключение оборачивает другое, обязательно передавать исходное как `cause`:

```java
public class InstrumentLoadException extends RuntimeException {
    public InstrumentLoadException(String message, Throwable cause) {
        super(message, cause);
    }
}
```

`catch (IOException e) { throw new RuntimeException("failed"); }` без передачи `e` уничтожает информацию о том, что
пошло не так на самом деле.

---

## try-with-resources и AutoCloseable

Ресурсы вроде файлов нужно закрывать явно. До Java 7 это писали через `try/finally`; проблема в том, что и тело
`try`, и сам `close()` в `finally` способны бросить исключение, и второе по умолчанию скрывает первое.

```java
try (FileInputStream stream = new FileInputStream("data.txt")) {
    // работа с потоком
} // close() вызывается автоматически, даже если тело блока бросит исключение
```

`try-with-resources` автоматически вызывает `close()` любого `AutoCloseable` при выходе из блока и корректно
обрабатывает случай, когда исключения возникают и в теле, и при закрытии.

---

## Стоимость исключений

Создание исключения не бесплатно — в основном из-за `fillInStackTrace()`, которая обходит весь текущий стек вызовов
и материализует его в массив `StackTraceElement`. Для обычного, не горячего пути (обработка ошибки пользовательского
ввода) эта стоимость не имеет значения. Подробный замер — на продвинутом треке.

---

## Когда исключения — не тот инструмент

Не для управления обычным потоком выполнения — «элемент не найден при поиске» не повод для исключения, для этого
подходит `Optional`. Не для валидации на каждой итерации горячего цикла. Не как замена документации — сообщение вроде
`"Error"` формально решает задачу «не потерять ошибку», но не помогает разработчику, читающему логи через полгода.
