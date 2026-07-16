package academy.backend.market_pulse.repository;

import java.util.Iterator;
import java.util.NoSuchElementException;

import academy.backend.market_pulse.model.Instrument;

/**
 * Хранилище инструментов поверх внутреннего массива фиксированного размера. Перебор — через
 * {@link Iterable}/{@link Iterator}, без раскрытия массива наружу (см. «План семинара.md»,
 * семинар 2, этап 4 — Iterator). Заглушка: подключение реального источника данных запланировано
 * на семинар 5.
 */
public class InstrumentRepository implements Iterable<Instrument> {

    private final Instrument[] instruments = new Instrument[100];
    private int size = 0;

    public void add(Instrument instrument) {
        instruments[size++] = instrument;
    }

    public Instrument findByTicker(String ticker) {
        for (Instrument instrument : this) {
            if (instrument.getTicker().equalsIgnoreCase(ticker)) {
                return instrument;
            }
        }
        return null;
    }

    @Override
    public Iterator<Instrument> iterator() {
        return new Iterator<>() {
            private int cursor = 0;

            @Override
            public boolean hasNext() {
                return cursor < size;
            }

            @Override
            public Instrument next() {
                if (!hasNext()) {
                    throw new NoSuchElementException();
                }
                return instruments[cursor++];
            }
        };
    }
}
