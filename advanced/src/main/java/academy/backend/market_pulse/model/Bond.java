package academy.backend.market_pulse.model;

import java.math.BigDecimal;

public class Bond extends Instrument {

    private final BigDecimal couponRate;
    private final int maturityYear;

    public Bond(String ticker, String name, Currency currency,
                BigDecimal couponRate, int maturityYear) {
        super(ticker, name, currency);
        this.couponRate = couponRate;
        this.maturityYear = maturityYear;
    }

    public BigDecimal getCouponRate() {
        return couponRate;
    }

    public int getMaturityYear() {
        return maturityYear;
    }

    @Override
    public String getDescription() {
        // TODO: описание облигации — купон и год погашения.
        throw new UnsupportedOperationException("getDescription для Bond");
    }
}
