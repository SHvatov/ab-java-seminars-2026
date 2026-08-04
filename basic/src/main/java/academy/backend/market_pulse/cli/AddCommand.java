package academy.backend.market_pulse.cli;

import java.io.IOException;
import java.util.concurrent.Callable;

import academy.backend.market_pulse.exception.DuplicateTickerException;
import academy.backend.market_pulse.factory.InstrumentFactories;
import academy.backend.market_pulse.model.Currency;
import academy.backend.market_pulse.model.Instrument;
import academy.backend.market_pulse.repository.InstrumentRepository;
import academy.backend.market_pulse.storage.InstrumentStorage;
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
    private final InstrumentStorage storage;

    public AddCommand(InstrumentRepository repository, InstrumentStorage storage) {
        this.repository = repository;
        this.storage = storage;
    }

    @Override
    public Integer call() {
        try {
            Instrument instrument = InstrumentFactories.create(type, ticker, name, currency);
            repository.add(instrument);
            storage.save(instrument);   // сохраняем на диск — переживёт перезапуск (семинар 7)
            System.out.println("Добавлено: " + instrument.getDescription());
            return 0;
        } catch (DuplicateTickerException | IllegalArgumentException e) {
            System.out.println("Не удалось добавить инструмент: " + e.getMessage());
            return 1;
        } catch (IOException e) {
            System.out.println("Инструмент добавлен в память, но не сохранён на диск: " + e.getMessage());
            return 1;
        }
    }
}
