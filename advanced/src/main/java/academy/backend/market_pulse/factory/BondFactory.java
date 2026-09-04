package academy.backend.market_pulse.factory;

import java.time.Year;

import academy.backend.market_pulse.model.Bond;
import academy.backend.market_pulse.model.Currency;
import academy.backend.market_pulse.model.Instrument;

public class BondFactory implements InstrumentFactory {

    static {
        InstrumentFactories.register("BOND", new BondFactory());
    }

    @Override
    public Instrument create(String ticker, String name, Currency currency) {
        // couponRate и maturityYear не собираются через CLI на этом этапе — значения по умолчанию.
        return new Bond(ticker, name, currency, 0.0, Year.now().getValue());
    }
}
