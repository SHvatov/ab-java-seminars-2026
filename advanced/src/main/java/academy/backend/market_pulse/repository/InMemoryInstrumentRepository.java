package academy.backend.market_pulse.repository;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

import academy.backend.market_pulse.exception.DuplicateTickerException;
import academy.backend.market_pulse.model.Instrument;

/**
 * Реализация {@link InstrumentRepository} на {@link LinkedHashMap}: инструменты индексируются по
 * тикеру, поиск и проверка дубликата — за O(1) вместо линейного перебора массива-заглушки из
 * семинара 2, порядок добавления сохраняется. Ключ нормализуется к верхнему регистру для
 * регистронезависимого поиска (см. «План семинара.md», семинар 5, этап 3). Интерфейс
 * {@link InstrumentRepository} не меняется — Proxy/CGLIB из семинара 2 работают поверх той же
 * абстракции.
 */
public class InMemoryInstrumentRepository implements InstrumentRepository {

    private final Map<String, Instrument> instruments = new LinkedHashMap<>();

    @Override
    public void add(Instrument instrument) {
        String key = instrument.getTicker().toUpperCase();
        if (instruments.containsKey(key)) {
            throw new DuplicateTickerException(instrument.getTicker());
        }
        instruments.put(key, instrument);
    }

    @Override
    public Optional<Instrument> findByTicker(String ticker) {
        return Optional.ofNullable(instruments.get(ticker.toUpperCase()));
    }

    @Override
    public Iterator<Instrument> iterator() {
        return instruments.values().iterator();
    }
}
