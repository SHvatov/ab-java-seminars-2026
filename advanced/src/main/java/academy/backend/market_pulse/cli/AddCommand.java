package academy.backend.market_pulse.cli;

import java.util.concurrent.Callable;

import academy.backend.market_pulse.factory.InstrumentFactories;
import academy.backend.market_pulse.model.Currency;
import academy.backend.market_pulse.model.Instrument;
import academy.backend.market_pulse.repository.InstrumentRepository;
import picocli.CommandLine.Command;
import picocli.CommandLine.Parameters;

@Command(name = "add", description = "Добавление инструмента в репозиторий")
public class AddCommand implements Callable<Integer> {

    @Parameters(index = "0", description = "Тип инструмента (STOCK, BOND, ETF)")
    private String type;

    @Parameters(index = "1", description = "Тикер инструмента")
    private String ticker;

    @Parameters(index = "2", description = "Название инструмента")
    private String name;

    @Parameters(index = "3", description = "Валюта инструмента (RUB, USD, EUR)")
    private Currency currency;

    private final InstrumentRepository repository;

    public AddCommand(InstrumentRepository repository) {
        this.repository = repository;
    }

    @Override
    public Integer call() {
        Instrument instrument = InstrumentFactories.create(type, ticker, name, currency);
        repository.add(instrument);
        System.out.println("Добавлено: " + instrument.getDescription());
        return 0;
    }
}
