package academy.backend.market_pulse.demo;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import academy.backend.market_pulse.model.Currency;
import academy.backend.market_pulse.model.Instrument;
import academy.backend.market_pulse.model.Stock;
import academy.backend.market_pulse.util.InstrumentUtils;

/**
 * Демонстрация {@code Comparator<? super T>} и правила PECS (семинар 3, этап 6): компаратор для
 * более общего типа {@link Instrument} сортирует список более узкого подтипа {@link Stock}.
 */
public class GenericsDemo {

    public static void main(String[] args) {
        List<Stock> stocks = new ArrayList<>(List.of(
                new Stock("SBER", "Сбербанк", Currency.RUB, "Financials", new BigDecimal("6.5")),
                new Stock("GAZP", "Газпром", Currency.RUB, "Energy", new BigDecimal("4.0"))
        ));

        Comparator<Instrument> byTicker = Comparator.comparing(Instrument::getTicker);

        // sortWith(List<T>, Comparator<? super T>) принимает Comparator<Instrument> для List<Stock> —
        // с сигнатурой sortWith(List<T>, Comparator<T>) этот вызов не скомпилировался бы.
        InstrumentUtils.sortWith(stocks, byTicker);

        stocks.forEach(stock -> System.out.println(stock.getTicker()));
    }
}
