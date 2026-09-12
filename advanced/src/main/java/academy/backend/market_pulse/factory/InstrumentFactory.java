package academy.backend.market_pulse.factory;

import academy.backend.market_pulse.model.Currency;
import academy.backend.market_pulse.model.Instrument;

/**
 * Создание {@link Instrument} по строковому типу. Реализации обнаруживаются через
 * {@link java.util.ServiceLoader} — см. {@link InstrumentFactories}.
 */
public interface InstrumentFactory {

    /**
     * Тип инструмента, за который отвечает фабрика ({@code STOCK}, {@code BOND}, {@code ETF}).
     */
    String type();

    Instrument create(String ticker, String name, Currency currency);
}
