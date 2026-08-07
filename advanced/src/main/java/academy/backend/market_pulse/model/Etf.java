package academy.backend.market_pulse.model;

/**
 * Биржевой фонд (ETF).
 */
public class Etf extends Instrument {

    private final String trackingIndex;

    public Etf(String ticker, String name, Currency currency, String trackingIndex) {
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
