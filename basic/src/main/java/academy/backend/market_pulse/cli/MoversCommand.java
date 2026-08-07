package academy.backend.market_pulse.cli;

import java.util.Comparator;
import java.util.concurrent.Callable;

import academy.backend.market_pulse.model.Quote;
import academy.backend.market_pulse.model.Watchlist;
import academy.backend.market_pulse.service.QuoteService;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

@Command(name = "movers", description = "Топ отслеживаемых инструментов (watchlist) по изменению цены")
public class MoversCommand implements Callable<Integer> {

    @Option(names = "--top", description = "Сколько инструментов показать (по умолчанию 5)")
    private int top = 5;

    @Option(names = "--losers", description = "Показать сильнее всего упавшие вместо выросших")
    private boolean losers;

    private final Watchlist watchlist;
    private final QuoteService quoteService;

    public MoversCommand(Watchlist watchlist, QuoteService quoteService) {
        this.watchlist = watchlist;
        this.quoteService = quoteService;
    }

    @Override
    public Integer call() {
        Comparator<Quote> byChange = Comparator.comparing(Quote::getChangePercent);
        quoteService.quotesFor(watchlist.tickers()).stream()
                .sorted(losers ? byChange : byChange.reversed())
                .limit(top)
                .forEach(quote -> System.out.println(quote));
        return 0;
    }
}
