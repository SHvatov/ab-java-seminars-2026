package academy.backend.market_pulse.cli;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.function.Predicate;

import academy.backend.market_pulse.model.Currency;
import academy.backend.market_pulse.model.Instrument;
import academy.backend.market_pulse.repository.InstrumentRepository;
import academy.backend.market_pulse.util.InstrumentUtils;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

@Command(name = "list", description = "Список инструментов")
public class ListCommand implements Callable<Integer> {

    @Option(names = "--type", description = "Фильтр по типу инструмента")
    private String type;

    @Option(names = "--currency", description = "Фильтр по валюте инструмента (RUB, USD, EUR)")
    private Currency currency;

    @Option(names = "--sort-by-ticker", description = "Сортировать по тикеру")
    private boolean sortByTicker;

    @Option(names = "--top", description = "Показать только первые N инструментов по алфавиту тикера")
    private Integer top;

    private final InstrumentRepository repository;

    public ListCommand(InstrumentRepository repository) {
        this.repository = repository;
    }

    @Override
    public Integer call() {
        Predicate<Instrument> filter = (type == null)
                ? instrument -> true
                : instrument -> instrument.getClass().getSimpleName().equalsIgnoreCase(type);

        if (currency != null) {
            filter = filter.and(instrument -> instrument.getCurrency() == currency);
        }

        List<Instrument> matched = new ArrayList<>();
        for (Instrument instrument : repository) {
            if (filter.test(instrument)) {
                matched.add(instrument);
            }
        }

        if (sortByTicker) {
            matched.sort(Comparator.comparing(Instrument::getTicker));
        }

        List<Instrument> result = (top != null)
                ? InstrumentUtils.topByTicker(matched, top)
                : matched;

        result.forEach(instrument -> System.out.println(instrument.getDescription()));
        return 0;
    }
}
