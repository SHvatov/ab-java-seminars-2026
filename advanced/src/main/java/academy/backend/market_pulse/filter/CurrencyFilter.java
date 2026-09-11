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
        // TODO: инструмент подходит, если его валюта точно совпадает с currency.
        throw new UnsupportedOperationException("matches для CurrencyFilter");
    }
}
