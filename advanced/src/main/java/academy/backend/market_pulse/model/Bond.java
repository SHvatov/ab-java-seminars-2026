package academy.backend.market_pulse.model;

/**
 * Облигация.
 */
public class Bond extends Instrument {

    private final double couponRate;
    private final int maturityYear;

    public Bond(String ticker, String name, Currency currency,
                double couponRate, int maturityYear) {
        super(ticker, name, currency);
        this.couponRate = couponRate;
        this.maturityYear = maturityYear;
    }

    public double getCouponRate() {
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
