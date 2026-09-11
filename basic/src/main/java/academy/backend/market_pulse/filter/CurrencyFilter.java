package academy.backend.market_pulse.filter;

import academy.backend.market_pulse.model.Currency;
import academy.backend.market_pulse.model.Instrument;

public class CurrencyFilter implements InstrumentFilter {

    private final Currency currency;

    public CurrencyFilter(Currency currency) {
        this.currency = currency;
    }

    @Override
    public boolean matches(Instrument instrument) {
        return instrument.getCurrency() == currency;
    }
}
