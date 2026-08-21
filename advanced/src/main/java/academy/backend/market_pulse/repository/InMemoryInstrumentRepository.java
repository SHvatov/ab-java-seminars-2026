package academy.backend.market_pulse.repository;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Optional;

import academy.backend.market_pulse.model.Instrument;

/**
 * Реализация {@link InstrumentRepository} поверх внутреннего массива фиксированного размера.
 * Заглушка: подключение реального источника данных запланировано на семинар 5.
 */
public class InMemoryInstrumentRepository implements InstrumentRepository {

    private final Instrument[] instruments = new Instrument[100];
    private int size = 0;

    @Override
    public void add(Instrument instrument) {
        instruments[size++] = instrument;
    }

    @Override
    public Optional<Instrument> findByTicker(String ticker) {
        for (Instrument instrument : this) {
            if (instrument.getTicker().equalsIgnoreCase(ticker)) {
                return Optional.of(instrument);
            }
        }
        return Optional.empty();
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
