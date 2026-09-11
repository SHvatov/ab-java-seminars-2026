package academy.backend.market_pulse.filter;

import academy.backend.market_pulse.model.Instrument;

public class TickerFilter implements InstrumentFilter {

    private final String query;

    public TickerFilter(String query) {
        this.query = query;
    }

    @Override
    public boolean matches(Instrument instrument) {
        // TODO: инструмент подходит, если query встречается в его тикере, без учёта регистра.
        throw new UnsupportedOperationException("matches для TickerFilter");
    }
}
