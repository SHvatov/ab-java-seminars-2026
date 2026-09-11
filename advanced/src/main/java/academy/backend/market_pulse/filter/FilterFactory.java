package academy.backend.market_pulse.filter;

import java.math.BigDecimal;

import academy.backend.market_pulse.model.Currency;

/**
 * Выбор конкретного {@link InstrumentFilter} по критериям, переданным из CLI. Критерии
 * взаимоисключающие: одновременно задаётся не больше одного — по типу, по тикеру, по валюте
 * или по цене. Комбинирование нескольких критериев сразу — задача паттерна Chain of
 * Responsibility, за рамками этого семинара.
 */
public final class FilterFactory {

    private FilterFactory() {
    }

    public static InstrumentFilter create(String type, String ticker, Currency currency,
                                           PriceFilter.Operator priceOperator, BigDecimal price) {
        int criteriaCount = count(type != null, ticker != null, currency != null, priceOperator != null);
        if (criteriaCount > 1) {
            throw new IllegalArgumentException(
                    "Одновременно можно задать только один критерий отбора: --type, --ticker, --currency или "
                            + "--price-op/--price");
        }
        if (type != null) {
            return new TypeFilter(type);
        }
        if (ticker != null) {
            return new TickerFilter(ticker);
        }
        if (currency != null) {
            return new CurrencyFilter(currency);
        }
        if (priceOperator != null) {
            return new PriceFilter(priceOperator, price);
        }
        return new NoOpFilter();
    }

    private static int count(boolean... flags) {
        int matched = 0;
        for (boolean flag : flags) {
            if (flag) {
                matched++;
            }
        }
        return matched;
    }
}
