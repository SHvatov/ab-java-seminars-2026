package academy.backend.market_pulse.model;

import java.math.BigDecimal;

/**
 * Способность инструмента приносить дивиденды. Вынесена из {@link Instrument} в отдельный
 * интерфейс, реализуемый только {@link Stock} — {@link Bond} и {@link Etf} дивидендов не платят
 * (см. «План семинара.md», семинар 2, этап 1 — исправление нарушения LSP).
 */
public interface DividendPaying {

    BigDecimal getDividends(BigDecimal currentPrice);
}
