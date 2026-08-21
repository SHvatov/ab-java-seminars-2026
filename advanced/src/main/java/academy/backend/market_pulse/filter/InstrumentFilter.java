package academy.backend.market_pulse.filter;

import academy.backend.market_pulse.model.Instrument;

/**
 * Правило отбора инструментов (см. «План семинара.md», семинар 2, этап 4 — Strategy).
 */
public interface InstrumentFilter {

    boolean matches(Instrument instrument);
}
