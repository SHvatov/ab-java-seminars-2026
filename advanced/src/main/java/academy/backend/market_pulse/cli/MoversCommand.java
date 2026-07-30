package academy.backend.market_pulse.cli;

import java.util.Comparator;
import java.util.concurrent.Callable;

import academy.backend.market_pulse.model.Quote;
import academy.backend.market_pulse.service.QuoteSource;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

@Command(name = "movers", description = "Топ инструментов по изменению цены (по умолчанию — растущие)")
public class MoversCommand implements Callable<Integer> {

    @Option(names = "--top", description = "Сколько инструментов показать (по умолчанию 5)")
    private int top = 5;

    @Option(names = "--losers", description = "Показать сильнее всего упавшие вместо выросших")
    private boolean losers;

    private final QuoteSource source;

    public MoversCommand(QuoteSource source) {
        this.source = source;
    }

    @Override
    public Integer call() {
        Comparator<Quote> byChange = Comparator.comparing(Quote::getChangePercent);
        source.all().stream()
                .sorted(losers ? byChange : byChange.reversed())
                .limit(top)
                .forEach(quote -> System.out.println(quote));
        return 0;
    }
}
