package academy.backend.market_pulse.benchmark;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.TearDown;
import org.openjdk.jmh.annotations.Warmup;

/**
 * Классический пул платформенных потоков против виртуальных потоков на I/O-bound нагрузке (см. «План
 * семинара.md», семинар 12, этап 4). Блокирующий сетевой вызов смоделирован {@code Thread.sleep},
 * чтобы замер был воспроизводим и не зависел от сети. Ожидаемо: с ростом числа одновременно ждущих
 * задач фиксированный пул упирается в свой размер (задачи ждут очереди), а виртуальные потоки
 * масштабируются по числу задач. На CPU-bound нагрузке разницы не было бы. Запуск — JMH-плагином
 * IntelliJ IDEA либо {@code org.openjdk.jmh.Main} из собранного jar.
 *
 * <p>Исполнители создаются один раз в {@link #setup()} и переиспользуются: создание восьми
 * платформенных потоков не должно попадать в измеряемый метод — иначе {@code fixedPool} нёс бы
 * стоимость старта потоков, которой нет у виртуальных, и сравнение было бы нечестным.
 */
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@State(Scope.Benchmark)
@Warmup(iterations = 3, time = 1)
@Measurement(iterations = 5, time = 1)
@Fork(1)
public class VirtualThreadsBenchmark {

    /** Число одновременных I/O-задач. */
    @Param({"100", "1000"})
    private int tasks;

    private ExecutorService fixedPoolExecutor;
    private ExecutorService virtualExecutor;

    @Setup(Level.Trial)
    public void setup() {
        fixedPoolExecutor = Executors.newFixedThreadPool(8);   // 8 платформенных потоков — задачи ждут очереди
        virtualExecutor = Executors.newVirtualThreadPerTaskExecutor();   // виртуальный поток на задачу
    }

    @TearDown(Level.Trial)
    public void tearDown() {
        fixedPoolExecutor.close();
        virtualExecutor.close();
    }

    /** Имитация блокирующего сетевого ожидания. */
    private static void blockingIo() {
        try {
            Thread.sleep(5);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private long run(ExecutorService executor) {
        CompletableFuture<?>[] futures = IntStream.range(0, tasks)
                .mapToObj(i -> CompletableFuture.runAsync(VirtualThreadsBenchmark::blockingIo, executor))
                .toArray(CompletableFuture[]::new);
        CompletableFuture.allOf(futures).join();
        return tasks;
    }

    @Benchmark
    public long fixedPool() {
        return run(fixedPoolExecutor);
    }

    @Benchmark
    public long virtualThreads() {
        return run(virtualExecutor);
    }
}
