package academy.backend.market_pulse.benchmark;

import java.util.concurrent.TimeUnit;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Warmup;

/**
 * Сравнение стоимости кода возврата, исключения с трассировкой стека и исключения без неё (см.
 * «План семинара.md», семинар 4, этап 5). Запускается через JMH-плагин IntelliJ IDEA
 * (правый клик по классу → Run) либо через {@code org.openjdk.jmh.Main} из собранного jar.
 */
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@State(Scope.Thread)
@Warmup(iterations = 5, time = 1)
@Measurement(iterations = 5, time = 1)
@Fork(1)
public class ExceptionOverheadBenchmark {

    @Benchmark
    public int returnCode() {
        return isKnown("CRYPTO") ? 0 : 1;
    }

    @Benchmark
    public int throwWithStackTrace() {
        try {
            validateOrThrow("CRYPTO");
            return 0;
        } catch (IllegalArgumentException e) {
            return 1;
        }
    }

    @Benchmark
    public int throwWithoutStackTrace() {
        try {
            validateOrThrowFast("CRYPTO");
            return 0;
        } catch (FastValidationException e) {
            return 1;
        }
    }

    private void validateOrThrow(String type) {
        if (!isKnown(type)) {
            throw new IllegalArgumentException("Unknown instrument type: " + type);
        }
    }

    private void validateOrThrowFast(String type) {
        if (!isKnown(type)) {
            throw new FastValidationException("Unknown instrument type: " + type);
        }
    }

    private boolean isKnown(String type) {
        return "STOCK".equals(type) || "BOND".equals(type) || "ETF".equals(type);
    }

    static class FastValidationException extends RuntimeException {
        FastValidationException(String message) {
            super(message, null, false, false); // enableSuppression=false, writableStackTrace=false
        }
    }
}
