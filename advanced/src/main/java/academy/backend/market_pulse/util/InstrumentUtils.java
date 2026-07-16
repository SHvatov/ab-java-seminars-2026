package academy.backend.market_pulse.util;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import academy.backend.market_pulse.model.Instrument;

/**
 * Обобщённые методы поверх домена (см. «План семинара.md», семинар 3, этапы 5-6).
 */
public final class InstrumentUtils {

    private InstrumentUtils() {
    }

    /**
     * Bounded type parameter — {@code extends Instrument} даёt доступ к {@code getTicker()}
     * внутри метода, оставаясь применимым к любому подтипу {@link Instrument}.
     */
    public static <T extends Instrument> List<T> topByTicker(List<T> items, int n) {
        List<T> sorted = new ArrayList<>(items);
        sorted.sort(Comparator.comparing(Instrument::getTicker));
        return sorted.subList(0, Math.min(n, sorted.size()));
    }

    /**
     * {@code Comparator<? super T>} — правило PECS, случай "consumer": компаратор для более
     * общего типа (например, {@code Comparator<Instrument>}) годится и для списка подтипа
     * (например, {@code List<Stock>}), потому что он лишь потребляет элементы для сравнения.
     */
    public static <T> void sortWith(List<T> items, Comparator<? super T> comparator) {
        items.sort(comparator);
    }
}
