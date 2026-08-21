package academy.backend.market_pulse.factory;

import academy.backend.market_pulse.model.Currency;
import academy.backend.market_pulse.model.Instrument;

/**
 * Создание {@link Instrument} по строковому типу (см. «План семинара.md», семинар 2, этап 4 —
 * Factory Method). Реализации обнаруживаются через {@link java.util.ServiceLoader} — новый тип
 * инструмента добавляется без изменения {@link InstrumentFactories} или существующих фабрик.
 */
public interface InstrumentFactory {

    String getSupportedType();

    Instrument create(String ticker, String name, Currency currency);
}
