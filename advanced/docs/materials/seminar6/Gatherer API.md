# Gatherer API

Набор промежуточных операций стрима (`map`, `filter`, `limit`, `distinct`, `sorted`) фиксирован. Операции с состоянием
между элементами — скользящее окно, накопительная свёртка с промежуточными результатами, схлопывание подряд идущих
дубликатов — в этот набор не входили, и до недавнего времени их приходилось делать вне стрима. Gatherer API (JEP
461/485, стабилизирован в Java 24) даёт механизм собственных промежуточных операций: `Stream.gather(Gatherer)` — ровно
то же расширение для промежуточного шага, что `collect(Collector)` — для терминального.

## Содержание

1. Место Gatherer в конвейере
2. Четыре части Gatherer
3. Downstream и досрочное завершение
4. Встроенные gatherer
5. Кастомный gatherer: скользящее среднее
6. Последовательные и параллельные gatherer

---

## 1. Место Gatherer в конвейере

`gather` — промежуточная операция: она принимает стрим и возвращает стрим, поэтому конвейер после неё продолжается
(можно навесить `map`, `filter`, терминальную операцию). Этим Gatherer отличается от `Collector`, который завершает
конвейер и возвращает готовый результат. От `map`/`filter` он отличается наличием состояния между элементами: `map`
обрабатывает каждый элемент независимо, а gatherer помнит ранее увиденное (например, накопленное окно).

```java
void example(List<BigDecimal> prices) {
    List<BigDecimal> smoothed = prices.stream()
            .gather(MovingAverageGatherer.movingAverage(3))  // промежуточная операция с состоянием
            .filter(avg -> avg.signum() > 0)                 // конвейер продолжается
            .toList();
}
```

---

## 2. Четыре части Gatherer

`Gatherer<T, A, R>` (T — входной тип, A — тип состояния, R — выходной) состоит из четырёх компонентов; переопределяются
только нужные:

- **initializer** (`Supplier<A>`) — создаёт изменяемое состояние для одного прохода (окно, аккумулятор). Для gatherer
  без состояния — `Gatherer.defaultInitializer()`.
- **integrator** (`Integrator<A, T, R>`) — обрабатывает очередной элемент: читает и обновляет состояние, может испустить
  элементы вниз по конвейеру и вернуть `false`, чтобы досрочно оборвать обработку.
- **combiner** (`BinaryOperator<A>`) — сливает два состояния при параллельном выполнении. Для последовательных gatherer
  не нужен (`Gatherer.defaultCombiner()` запрещает распараллеливание).
- **finisher** — вызывается после последнего элемента и может испустить «хвост» (накопленный остаток).

Фабрики `Gatherer.of(...)` и `Gatherer.ofSequential(...)` собирают gatherer из этих частей; `ofSequential` создаёт
заведомо последовательный gatherer (без combiner).

---

## 3. Downstream и досрочное завершение

Integrator получает `Gatherer.Downstream<? super R>` — канал к следующей операции конвейера. Метод `downstream.push(r)`
передаёт элемент дальше и возвращает `boolean`: `false` означает, что ниже по конвейеру больше не принимают (например,
после `limit`). Integrator тоже возвращает `boolean`: `true` — продолжать, `false` — прекратить обработку источника.
Возврат результата `push` из integrator корректно пробрасывает сигнал остановки вверх и позволяет короткое замыкание.

---

## 4. Встроенные gatherer

Фабрики в `java.util.stream.Gatherers`:

- `windowFixed(n)` — неперекрывающиеся окна по `n` элементов (`List<T>` на выходе);
- `windowSliding(n)` — скользящие окна по `n` подряд идущих элементов;
- `fold(initial, folder)` — свёртка в одно значение (промежуточный аналог `reduce`);
- `scan(initial, scanner)` — накопительная свёртка, испускающая промежуточные значения на каждом шаге;
- `mapConcurrent(concurrency, mapper)` — параллельное отображение с ограничением числа одновременных задач.

```java
void example(List<BigDecimal> prices) {
    List<List<BigDecimal>> windows = prices.stream()
            .gather(Gatherers.windowSliding(3))
            .toList();
}
```

`windowSliding` материализует каждое окно списком — удобно, но на длинных сериях это накладно по памяти.

---

## 5. Кастомный gatherer: скользящее среднее

Скользящее среднее «на лету» держит окно последних `windowSize` цен и их сумму: на каждый элемент добавляет его в окно и
к сумме, при переполнении окна вычитает выпавшую цену, а как только окно заполнилось — испускает среднее. Сумма
обновляется инкрементально, поэтому все окна в памяти не хранятся.

```java
public final class MovingAverageGatherer {

    private MovingAverageGatherer() {
    }

    public static Gatherer<BigDecimal, ?, BigDecimal> movingAverage(int windowSize) {
        if (windowSize <= 0) {
            throw new IllegalArgumentException("Размер окна должен быть положительным");
        }
        return Gatherer.ofSequential(
                State::new,
                (state, price, downstream) -> {
                    state.window.addLast(price);
                    state.sum = state.sum.add(price);
                    if (state.window.size() > windowSize) {
                        state.sum = state.sum.subtract(state.window.removeFirst());
                    }
                    if (state.window.size() == windowSize) {
                        BigDecimal average = state.sum.divide(
                                BigDecimal.valueOf(windowSize), 2, RoundingMode.HALF_UP);
                        return downstream.push(average);
                    }
                    return true;
                });
    }

    private static final class State {
        private final Deque<BigDecimal> window = new ArrayDeque<>();
        private BigDecimal sum = BigDecimal.ZERO;
    }
}
```

Пока окно не заполнено (первые `windowSize - 1` элементов), integrator возвращает `true`, ничего не испуская, — на
выходе серия скользящих средних короче входа ровно на неполный начальный участок. В отличие от `windowSliding`, здесь не
создаётся ни одного списка-окна: состояние — очередь фиксированной длины и текущая сумма.

---

## 6. Последовательные и параллельные gatherer

Gatherer со скользящим окном по своей природе последователен: результат элемента зависит от предыдущих, поэтому он
создаётся через `ofSequential` и не имеет combiner — распараллеливание такого стрима запрещено. Gatherer, для которого
частичные состояния можно осмысленно слить, реализует combiner и допускает параллельное выполнение. Как и с обычными
операциями стрима, состояние gatherer не должно утекать наружу и меняться в обход конвейера — иначе теряются и
предсказуемость, и возможность параллелизма.
