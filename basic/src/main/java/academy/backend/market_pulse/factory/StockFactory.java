package academy.backend.market_pulse.factory;

import academy.backend.market_pulse.model.Currency;
import academy.backend.market_pulse.model.Instrument;

public class StockFactory implements InstrumentFactory {

    static {
        InstrumentFactories.register("STOCK", new StockFactory());
    }

    @Override
    public Instrument create(String ticker, String name, Currency currency) {
        // TODO: создать Stock(ticker, name, currency, sector, dividendYield) — sector/dividendYield
        // через CLI пока не собираются, использовать значения по умолчанию.
        throw new UnsupportedOperationException("create для StockFactory");
    }
}
