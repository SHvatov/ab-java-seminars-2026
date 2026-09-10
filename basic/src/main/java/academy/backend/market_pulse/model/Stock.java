package academy.backend.market_pulse.model;

import java.math.BigDecimal;

public class Stock extends Instrument {

    private final String sector;
    // NOTICE: доходность и цена — всегда BigDecimal, не double (деньги).
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

    @Override
    public String getDescription() {
        // TODO: описание акции — сектор.
        throw new UnsupportedOperationException("getDescription для Stock");
    }

    public BigDecimal getDividends(BigDecimal currentPrice) {
        // TODO: currentPrice * dividendYield / 100, с явным RoundingMode.
        throw new UnsupportedOperationException("getDividends для Stock");
    }
}
