package academy.backend.market_pulse.model;

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
        // TODO: описание ETF — отслеживаемый индекс.
        throw new UnsupportedOperationException("getDescription для Etf");
    }
}
