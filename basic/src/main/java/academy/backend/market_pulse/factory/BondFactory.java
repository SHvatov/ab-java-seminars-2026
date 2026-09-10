package academy.backend.market_pulse.factory;

import academy.backend.market_pulse.model.Currency;
import academy.backend.market_pulse.model.Instrument;

public class BondFactory implements InstrumentFactory {

    static {
        InstrumentFactories.register("BOND", new BondFactory());
    }

    @Override
    public Instrument create(String ticker, String name, Currency currency) {
        // TODO: создать Bond(ticker, name, currency, couponRate, maturityYear) — couponRate/
        // maturityYear через CLI пока не собираются, использовать значения по умолчанию.
        throw new UnsupportedOperationException("create для BondFactory");
    }
}
