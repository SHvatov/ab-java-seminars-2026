package academy.backend.market_pulse.filter;

import academy.backend.market_pulse.model.Instrument;

public class NoOpFilter implements InstrumentFilter {

    @Override
    public boolean matches(Instrument instrument) {
        return true;
    }
}
