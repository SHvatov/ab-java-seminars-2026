package academy.backend.market_pulse.model;

import java.math.BigDecimal;

/**
 * Акция.
 */
public class Stock extends Instrument {

    private final String sector;
    // NOTICE: доходность и цена — всегда BigDecimal, не double (деньги; см. шаг 4.3 плана).
    private final BigDecimal dividendYield;

    public Stock(String ticker, String name, Currency currency,
                 String sector, BigDecimal dividendYield) {
        super(ticker, name, currency);
        this.sector = sector;
        this.dividendYield = dividendYield;
    }

    public String getSector() {
        return sector;
    }

    // TODO (шаг 4.2): добавить описание инструмента (сектор), когда в Instrument
    // появится соответствующий метод.

    // TODO (шаг 4.4): реализовать расчёт годовой дивидендной доходности —
    // currentPrice * dividendYield / 100, с явным RoundingMode. Обсудить на
    // семинаре, почему этот метод должен остаться только здесь, а не в Instrument.
}
