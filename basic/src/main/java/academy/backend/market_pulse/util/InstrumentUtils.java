package academy.backend.market_pulse.util;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import academy.backend.market_pulse.model.Instrument;

/**
 * Обобщённый метод с bounded type parameter — работает с любым подтипом {@link Instrument}
 * (см. «План семинара.md», семинар 3, этап 5).
 */
public final class InstrumentUtils {

    private InstrumentUtils() {
    }

    public static <T extends Instrument> List<T> topByTicker(List<T> items, int n) {
        List<T> sorted = new ArrayList<>(items);
        sorted.sort(Comparator.comparing(Instrument::getTicker));
        return sorted.subList(0, Math.min(n, sorted.size()));
    }
}
