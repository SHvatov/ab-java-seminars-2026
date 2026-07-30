package academy.backend.market_pulse.cli;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

import academy.backend.market_pulse.gather.MovingAverageGatherer;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

@Command(name = "history", description = "Скользящее среднее по истории цен инструмента")
public class HistoryCommand implements Callable<Integer> {

    @Parameters(index = "0", description = "Тикер инструмента")
    private String ticker;

    @Option(names = "--sma", description = "Окно скользящего среднего (по умолчанию 3)")
    private int window = 3;

    private final Map<String, List<BigDecimal>> priceHistory;

    public HistoryCommand(Map<String, List<BigDecimal>> priceHistory) {
        this.priceHistory = priceHistory;
    }

    @Override
    public Integer call() {
        List<BigDecimal> prices = priceHistory.get(ticker.toUpperCase());
        if (prices == null) {
            System.out.println("Нет истории цен по тикеру: " + ticker.toUpperCase());
            return 1;
        }

        List<BigDecimal> sma = prices.stream()
                .gather(MovingAverageGatherer.movingAverage(window))
                .toList();

        System.out.println("Цены: " + prices);
        System.out.println("SMA(" + window + "): " + sma);
        return 0;
    }
}
