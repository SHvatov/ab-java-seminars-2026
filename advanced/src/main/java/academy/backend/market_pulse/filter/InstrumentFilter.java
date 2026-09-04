package academy.backend.market_pulse.filter;

import academy.backend.market_pulse.model.Instrument;

/**
 * Правило отбора инструментов.
 */
public interface InstrumentFilter {

    boolean matches(Instrument instrument);
}
