package academy.backend.market_pulse.factory;

import academy.backend.market_pulse.model.Currency;
import academy.backend.market_pulse.model.Etf;
import academy.backend.market_pulse.model.Instrument;

public class EtfFactory implements InstrumentFactory {

    static {
        InstrumentFactories.register("ETF", new EtfFactory());
    }

    @Override
    public Instrument create(String ticker, String name, Currency currency) {
        // TODO: создать Etf(ticker, name, currency, trackingIndex) — trackingIndex через CLI пока
        // не собирается, использовать значение по умолчанию.
        throw new UnsupportedOperationException("create для EtfFactory");
    }
}
