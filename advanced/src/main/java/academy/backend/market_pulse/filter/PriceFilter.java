package academy.backend.market_pulse.filter;

import java.math.BigDecimal;

import academy.backend.market_pulse.model.Instrument;
import academy.backend.market_pulse.model.Stock;

/**
 * Отбор по цене — применим только к {@link Stock}: остальные типы инструментов сегодня не несут
 * числового значения цены, поэтому для них {@code matches} всегда возвращает {@code false}. В
 * роли цены — дивидендная доходность ({@link Stock#getDividendYield()}), единственный числовой
 * атрибут акции на этом этапе проекта.
 */
public class PriceFilter implements InstrumentFilter {

    public enum Operator {
        GE, LE, EQ
    }

    private final Operator operator;
    private final BigDecimal threshold;

    public PriceFilter(Operator operator, BigDecimal threshold) {
        this.operator = operator;
        this.threshold = threshold;
    }

    @Override
    public boolean matches(Instrument instrument) {
        if (!(instrument instanceof Stock stock)) {
            return false;
        }
        int comparison = stock.getDividendYield().compareTo(threshold);
        return switch (operator) {
            case GE -> comparison >= 0;
            case LE -> comparison <= 0;
            case EQ -> comparison == 0;
        };
    }
}
