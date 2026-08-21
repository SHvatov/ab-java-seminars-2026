package academy.backend.market_pulse.factory;

import academy.backend.market_pulse.model.Currency;
import academy.backend.market_pulse.model.Etf;
import academy.backend.market_pulse.model.Instrument;

public class EtfFactory implements InstrumentFactory {

    @Override
    public String getSupportedType() {
        return "ETF";
    }

    @Override
    public Instrument create(String ticker, String name, Currency currency) {
        // trackingIndex не собирается через CLI на этом этапе — значение по умолчанию.
        return new Etf(ticker, name, currency, "Unspecified");
    }
}
