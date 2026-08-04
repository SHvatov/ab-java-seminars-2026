package academy.backend.market_pulse.model;

import java.math.BigDecimal;
import java.math.RoundingMode;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Акция. В отличие от {@link Bond} и {@link Etf}, только у акций есть
 * дивидендная доходность — поэтому {@code getDividends} объявлен здесь,
 * а не в {@link Instrument} (см. «План семинара.md», этап 4.6 — нарушение LSP).
 */
public class Stock extends Instrument implements DividendPaying {

    private final String sector;
    // dividendYield — процент годовой дивидендной доходности, например 6.5
    private final BigDecimal dividendYield;

    @JsonCreator
    public Stock(@JsonProperty("ticker") String ticker,
                 @JsonProperty("name") String name,
                 @JsonProperty("currency") Currency currency,
                 @JsonProperty("sector") String sector,
                 @JsonProperty("dividendYield") BigDecimal dividendYield) {
        super(ticker, name, currency);
        this.sector = sector;
        this.dividendYield = dividendYield;
    }

    public String getSector() {
        return sector;
    }

    /**
     * Годовая дивидендная доходность в валюте инструмента: цена × доходность / 100.
     */
    @Override
    public BigDecimal getDividends(BigDecimal currentPrice) {
        return currentPrice.multiply(dividendYield)
                .divide(BigDecimal.valueOf(100), RoundingMode.HALF_UP);
    }

    @Override
    public String getDescription() {
        return "Акция, сектор: " + sector;
    }
}
