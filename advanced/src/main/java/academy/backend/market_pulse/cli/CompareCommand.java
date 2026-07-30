package academy.backend.market_pulse.cli;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Callable;

import academy.backend.market_pulse.model.Quote;
import academy.backend.market_pulse.service.QuoteService;
import picocli.CommandLine.Command;
import picocli.CommandLine.Parameters;

@Command(name = "compare", description = "Сравнить доходность инструментов по тикерам (по убыванию изменения цены)")
public class CompareCommand implements Callable<Integer> {

    @Parameters(arity = "1..*", description = "Тикеры инструментов")
    private List<String> tickers;

    private final QuoteService quoteService;

    public CompareCommand(QuoteService quoteService) {
        this.quoteService = quoteService;
    }

    @Override
    public Integer call() {
        tickers.stream()
                .map(quoteService::quoteFor)          // String -> Optional<Quote>
                .flatMap(Optional::stream)            // отбрасываем ненайденные
                .sorted(Comparator.comparing(Quote::getChangePercent).reversed())
                .forEach(quote -> System.out.println(quote));
        return 0;
    }
}
