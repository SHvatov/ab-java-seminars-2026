package academy.backend.market_pulse.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Биржевой фонд (ETF).
 */
public class Etf extends Instrument {

    private final String trackingIndex;

    @JsonCreator
    public Etf(@JsonProperty("ticker") String ticker,
               @JsonProperty("name") String name,
               @JsonProperty("currency") Currency currency,
               @JsonProperty("trackingIndex") String trackingIndex) {
        super(ticker, name, currency);
        this.trackingIndex = trackingIndex;
    }

    public String getTrackingIndex() {
        return trackingIndex;
    }

    @Override
    public String getDescription() {
        return "ETF, отслеживает индекс: " + trackingIndex;
    }
}
