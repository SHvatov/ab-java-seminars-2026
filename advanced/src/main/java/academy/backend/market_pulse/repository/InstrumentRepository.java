package academy.backend.market_pulse.repository;

import academy.backend.market_pulse.model.Instrument;

/**
 * Хранилище инструментов. Выделено в интерфейс (в отличие от базового трека) — нужен для
 * JDK Dynamic Proxy и CGLIB, которым для работы требуется тип, отдельный от конкретной
 * реализации.
 */
public interface InstrumentRepository extends Iterable<Instrument> {

    void add(Instrument instrument);

    Instrument findByTicker(String ticker);
}
