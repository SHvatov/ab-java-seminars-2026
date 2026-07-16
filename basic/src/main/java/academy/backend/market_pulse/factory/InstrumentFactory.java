package academy.backend.market_pulse.factory;

import academy.backend.market_pulse.model.Currency;
import academy.backend.market_pulse.model.Instrument;

/**
 * Создание {@link Instrument} по строковому типу (см. «План семинара.md», семинар 2, этап 4 —
 * Factory Method).
 */
public interface InstrumentFactory {

    Instrument create(String ticker, String name, Currency currency);
}
