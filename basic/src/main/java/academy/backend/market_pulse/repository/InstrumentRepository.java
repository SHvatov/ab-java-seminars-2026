package academy.backend.market_pulse.repository;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

import academy.backend.market_pulse.exception.DuplicateTickerException;
import academy.backend.market_pulse.model.Instrument;

/**
 * In-memory хранилище инструментов, проиндексированных по тикеру. Массив-заглушка из семинара 2
 * заменён на {@link LinkedHashMap}: поиск и проверка дубликата — за O(1) вместо линейного перебора,
 * порядок добавления сохраняется для вывода. Ключ нормализуется к верхнему регистру, чтобы поиск
 * оставался регистронезависимым (см. «План семинара.md», семинар 5, этап 3).
 */
public class InstrumentRepository implements Iterable<Instrument> {

    private final Map<String, Instrument> instruments = new LinkedHashMap<>();

    public void add(Instrument instrument) {
        String key = instrument.getTicker().toUpperCase();
        if (instruments.containsKey(key)) {
            throw new DuplicateTickerException(instrument.getTicker());
        }
        instruments.put(key, instrument);
    }

    public Optional<Instrument> findByTicker(String ticker) {
        return Optional.ofNullable(instruments.get(ticker.toUpperCase()));
    }

    @Override
    public Iterator<Instrument> iterator() {
        return instruments.values().iterator();
    }
}
