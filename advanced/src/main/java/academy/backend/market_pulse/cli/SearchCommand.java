package academy.backend.market_pulse.cli;

import java.util.concurrent.Callable;

import academy.backend.market_pulse.repository.InstrumentRepository;
import picocli.CommandLine.Command;
import picocli.CommandLine.Parameters;

@Command(name = "search", description = "Поиск инструмента по тикеру")
public class SearchCommand implements Callable<Integer> {

    @Parameters(index = "0", description = "Тикер инструмента")
    private String ticker;

    private final InstrumentRepository repository;

    public SearchCommand(InstrumentRepository repository) {
        this.repository = repository;
    }

    @Override
    public Integer call() {
        return repository.findByTicker(ticker)
                .map(instrument -> {
                    System.out.println(instrument.getDescription());
                    return 0;
                })
                .orElseGet(() -> {
                    System.out.println("Инструмент не найден: " + ticker);
                    return 1;
                });
    }
}
