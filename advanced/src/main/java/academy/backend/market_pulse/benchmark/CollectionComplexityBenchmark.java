package academy.backend.market_pulse.benchmark;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Warmup;
import org.openjdk.jmh.infra.Blackhole;

/**
 * Эмпирический замер O-нотации: {@link ArrayList} против {@link LinkedList} на индексном доступе
 * (O(1) vs O(n)) и вставке в начало (O(n) vs O(1)). Параметр {@code size} показывает, как операция
 * масштабируется с ростом данных (см. «План семинара.md», семинар 5, этап 5). Запускается через
 * JMH-плагин IntelliJ IDEA либо {@code org.openjdk.jmh.Main} из собранного jar.
 */
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@State(Scope.Thread)
@Warmup(iterations = 5, time = 1)
@Measurement(iterations = 5, time = 1)
@Fork(1)
public class CollectionComplexityBenchmark {

    @Param({"1000", "100000"})
    private int size;

    private List<Integer> arrayList;
    private List<Integer> linkedList;

    @Setup
    public void setup() {
        arrayList = new ArrayList<>();
        linkedList = new LinkedList<>();
        for (int i = 0; i < size; i++) {
            arrayList.add(i);
            linkedList.add(i);
        }
    }

    @Benchmark
    public Integer arrayListGetMiddle() {
        return arrayList.get(size / 2);      // O(1)
    }

    @Benchmark
    public Integer linkedListGetMiddle() {
        return linkedList.get(size / 2);     // O(n)
    }

    @Benchmark
    public void arrayListAddFirst(Blackhole bh) {
        arrayList.add(0, 42);                // O(n): сдвиг всех элементов
        arrayList.remove(0);
        bh.consume(arrayList);
    }

    @Benchmark
    public void linkedListAddFirst(Blackhole bh) {
        linkedList.addFirst(42);             // O(1): перестановка ссылок
        linkedList.removeFirst();
        bh.consume(linkedList);
    }
}
