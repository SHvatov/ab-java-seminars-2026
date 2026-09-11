package academy.backend.market_pulse.filter;

import java.math.BigDecimal;

import academy.backend.market_pulse.model.Instrument;

/**
 * Отбор по цене — применим только к {@link academy.backend.market_pulse.model.Stock}: остальные
 * типы инструментов сегодня не несут числового значения цены, поэтому для них {@code matches}
 * всегда возвращает {@code false}. В роли цены — дивидендная доходность
 * ({@code Stock.getDividendYield()}), единственный числовой атрибут акции на этом этапе проекта.
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
        // TODO: false для инструментов, не являющихся Stock; для Stock — сравнить
        //  getDividendYield() с threshold согласно operator (GE/LE/EQ), см. BigDecimal.compareTo.
        throw new UnsupportedOperationException("matches для PriceFilter");
    }
}
