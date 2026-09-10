package academy.backend.market_pulse.model;

import java.math.BigDecimal;

/**
 * Наивная попытка представить акцию с ценой на конкретный момент времени —
 * через наследование от {@link Stock}. Не взлетает: {@code getDividends(BigDecimal)}
 * из родителя никуда не делся, а перегрузка без аргумента — не переопределение,
 * и непонятно, какой из двух методов вызывать.
 *
 * @deprecated заменяется агрегацией — {@link Quote}. Удалить после рефакторинга.
 */
@Deprecated
public class StockSnapshot extends Stock {

    private final BigDecimal price;

    public StockSnapshot(String ticker, String name, Currency currency,
                          String sector, BigDecimal dividendYield, BigDecimal price) {
        super(ticker, name, currency, sector, dividendYield);
        this.price = price;
    }

    // Перегружаем — удобно, цена уже вшита в объект.
    public BigDecimal getDividends() {
        return getDividends(this.price);
    }

    // Но getDividends(BigDecimal) из Stock никуда не делся — какой из двух
    // вызовет клиентский код, работающий со Stock, а не со StockSnapshot?
}
