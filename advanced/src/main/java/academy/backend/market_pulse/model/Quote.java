package academy.backend.market_pulse.model;

import java.math.BigDecimal;

/**
 * Рыночная котировка: инструмент + цена + изменение за период. Использует
 * агрегацию, а не наследование от {@link Stock} (см. «План семинара.md»,
 * этап 4.7, шаги 1-2 — эволюция StockSnapshot → Quote).
 */
public class Quote {

    private final Instrument instrument;
    private final BigDecimal price;
    private final BigDecimal changePercent;

    public Quote(Instrument instrument, BigDecimal price, BigDecimal changePercent) {
        this.instrument = instrument;
        this.price = price;
        this.changePercent = changePercent;
    }

    public Instrument getInstrument() {
        return instrument;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public BigDecimal getChangePercent() {
        return changePercent;
    }

    public BigDecimal getDividends() {
        if (instrument instanceof Stock stock) {
            return stock.getDividends(this.price);
        }
        return BigDecimal.ZERO;
    }

    @Override
    public String toString() {
        String direction = changePercent.signum() >= 0 ? "▲" : "▼";
        return instrument.getTicker() + ": " + price + " " + instrument.getCurrency()
                + " " + direction + " " + changePercent.abs() + "%";
    }
}
