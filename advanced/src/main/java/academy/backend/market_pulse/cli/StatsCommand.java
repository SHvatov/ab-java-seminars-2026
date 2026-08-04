package academy.backend.market_pulse.cli;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;

import academy.backend.market_pulse.model.Currency;
import academy.backend.market_pulse.model.Quote;
import academy.backend.market_pulse.model.Watchlist;
import academy.backend.market_pulse.service.QuoteService;
import picocli.CommandLine.Command;

@Command(name = "stats", description = "Сводка по котировкам отслеживаемых инструментов (watchlist): типы, валюты, доходность")
public class StatsCommand implements Callable<Integer> {

    private final Watchlist watchlist;
    private final QuoteService quoteService;

    public StatsCommand(Watchlist watchlist, QuoteService quoteService) {
        this.watchlist = watchlist;
        this.quoteService = quoteService;
    }

    @Override
    public Integer call() {
        List<Quote> quotes = watchlist.tickers().stream()
                .map(quoteService::quoteFor)
                .flatMap(Optional::stream)
                .toList();

        if (quotes.isEmpty()) {
            System.out.println("Нет данных: watchlist пуст или котировки недоступны.");
            return 0;
        }

        Map<String, Long> byType = quotes.stream()
                .collect(Collectors.groupingBy(
                        quote -> quote.getInstrument().getClass().getSimpleName(),
                        Collectors.counting()));

        Map<Currency, Long> byCurrency = quotes.stream()
                .collect(Collectors.groupingBy(
                        quote -> quote.getInstrument().getCurrency(),
                        Collectors.counting()));

        double avgChange = quotes.stream()
                .mapToDouble(quote -> quote.getChangePercent().doubleValue())
                .average()
                .orElse(0.0);

        // Денежная сумма — через reduce над BigDecimal, а не mapToDouble().sum(): точность сохраняется.
        BigDecimal totalPrice = quotes.stream()
                .map(Quote::getPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        byType.forEach((type, count) -> System.out.println(type + ": " + count));
        byCurrency.forEach((currency, count) -> System.out.println(currency + ": " + count));
        System.out.printf("Средняя доходность: %.2f%%%n", avgChange);
        System.out.println("Суммарная цена инструментов: " + totalPrice);
        return 0;
    }
}
