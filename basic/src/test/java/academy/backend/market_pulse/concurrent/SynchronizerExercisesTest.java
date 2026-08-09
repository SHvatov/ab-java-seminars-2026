package academy.backend.market_pulse.concurrent;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Самостоятельная работа семинара 11: подобрать корректный синхронизатор под сценарий координации
 * потоков. Здесь — эталонное решение; студентам выдаётся версия, где выбор синхронизатора оставлен
 * пропуском, и тест должен стать зелёным только с правильным выбором.
 */
class SynchronizerExercisesTest {

    @Test
    @DisplayName("Дождаться завершения N воркеров → CountDownLatch")
    void дождатьсяЗавершенияВоркеров() throws InterruptedException {
        int workers = 4;
        CountDownLatch ready = new CountDownLatch(workers);
        AtomicInteger prepared = new AtomicInteger();

        for (int i = 0; i < workers; i++) {
            Thread.ofPlatform().start(() -> {
                prepared.incrementAndGet();
                ready.countDown();
            });
        }
        ready.await();   // главный поток ждёт, пока все N не отметятся

        assertEquals(workers, prepared.get());
    }

    @Test
    @DisplayName("Ограничить одновременность обращений → Semaphore")
    void ограничитьОдновременность() throws InterruptedException {
        int limit = 3;
        int tasks = 20;
        Semaphore semaphore = new Semaphore(limit);
        AtomicInteger concurrent = new AtomicInteger();
        AtomicInteger maxConcurrent = new AtomicInteger();
        CountDownLatch done = new CountDownLatch(tasks);

        for (int i = 0; i < tasks; i++) {
            Thread.ofPlatform().start(() -> {
                try {
                    semaphore.acquire();
                    maxConcurrent.accumulateAndGet(concurrent.incrementAndGet(), Math::max);
                    Thread.sleep(5);
                    concurrent.decrementAndGet();
                    semaphore.release();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    done.countDown();
                }
            });
        }
        done.await();

        assertTrue(maxConcurrent.get() <= limit, "одновременно не должно быть больше " + limit);
    }

    @Test
    @DisplayName("Собрать N потоков в точке → CyclicBarrier")
    void собратьПотокиВТочке() throws InterruptedException {
        int parties = 4;
        AtomicInteger tripped = new AtomicInteger();
        CyclicBarrier barrier = new CyclicBarrier(parties, tripped::incrementAndGet);
        CountDownLatch done = new CountDownLatch(parties);

        for (int i = 0; i < parties; i++) {
            Thread.ofPlatform().start(() -> {
                try {
                    barrier.await();
                } catch (Exception e) {
                    Thread.currentThread().interrupt();
                } finally {
                    done.countDown();
                }
            });
        }
        done.await();

        assertEquals(1, tripped.get());   // барьер сработал один раз — когда собрались все
    }
}
