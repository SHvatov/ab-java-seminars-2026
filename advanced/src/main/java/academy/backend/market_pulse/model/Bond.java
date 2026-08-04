package academy.backend.market_pulse.model;

import java.math.BigDecimal;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Облигация.
 */
public class Bond extends Instrument {

    private final BigDecimal couponRate;
    private final int maturityYear;

    @JsonCreator
    public Bond(@JsonProperty("ticker") String ticker,
                @JsonProperty("name") String name,
                @JsonProperty("currency") Currency currency,
                @JsonProperty("couponRate") BigDecimal couponRate,
                @JsonProperty("maturityYear") int maturityYear) {
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
        return "Облигация, купон: " + couponRate + "%, погашение: " + maturityYear;
    }
}
