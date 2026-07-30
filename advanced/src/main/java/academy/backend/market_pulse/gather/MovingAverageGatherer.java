package academy.backend.market_pulse.gather;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.stream.Gatherer;

/**
 * Кастомный {@link Gatherer} — скользящее среднее по последовательности цен (см. «План семинара.md»,
 * семинар 6, этап 4 — Gatherer API). Промежуточная операция с состоянием: держит окно из последних
 * {@code windowSize} цен и их сумму, и как только окно заполнено, испускает вниз по конвейеру среднее
 * по окну. Считает «на лету» (обновляет сумму инкрементально), не накапливая все окна в памяти —
 * этим отличается от {@code Gatherers.windowSliding(k)}, который сначала собирает списки-окна.
 */
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
