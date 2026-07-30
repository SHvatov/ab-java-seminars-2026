package academy.backend.market_pulse.cli;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;

import academy.backend.market_pulse.model.Currency;
import academy.backend.market_pulse.model.Quote;
import academy.backend.market_pulse.service.QuoteSource;
import picocli.CommandLine.Command;

@Command(name = "stats", description = "Сводка по котировкам: распределение по типу и валюте, средняя доходность")
public class StatsCommand implements Callable<Integer> {

    private final QuoteSource source;

    public StatsCommand(QuoteSource source) {
        this.source = source;
    }

    @Override
    public Integer call() {
        Collection<Quote> quotes = source.all();

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

        byType.forEach((type, count) -> System.out.println(type + ": " + count));
        byCurrency.forEach((currency, count) -> System.out.println(currency + ": " + count));
        System.out.printf("Средняя доходность: %.2f%%%n", avgChange);
        return 0;
    }
}
