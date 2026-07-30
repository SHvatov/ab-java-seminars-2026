package academy.backend.market_pulse.repository;

import java.util.Optional;
import java.util.stream.Stream;

import academy.backend.market_pulse.model.Instrument;

/**
 * Хранилище инструментов. Выделено в интерфейс (в отличие от базового трека) — нужен для
 * JDK Dynamic Proxy и CGLIB на этапе 5, которым для работы требуется тип, отдельный от
 * конкретной реализации (см. «План семинара.md», семинар 2, этап 4 — Iterator).
 */
public interface InstrumentRepository extends Iterable<Instrument> {

    void add(Instrument instrument);

    Optional<Instrument> findByTicker(String ticker);

    /**
     * Потоковый доступ к инструментам — вход в Stream API для декларативной фильтрации, сортировки
     * и агрегации (см. «План семинара.md», семинар 6).
     */
    Stream<Instrument> stream();
}
