package academy.backend.market_pulse.cli;

import java.util.concurrent.Callable;

import academy.backend.market_pulse.filter.InstrumentFilter;
import academy.backend.market_pulse.filter.NoOpFilter;
import academy.backend.market_pulse.filter.TypeFilter;
import academy.backend.market_pulse.model.Instrument;
import academy.backend.market_pulse.repository.InstrumentRepository;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

@Command(name = "list", description = "Список инструментов")
public class ListCommand implements Callable<Integer> {

    @Option(names = "--type", description = "Фильтр по типу инструмента")
    private String type;

    private final InstrumentRepository repository;

    public ListCommand(InstrumentRepository repository) {
        this.repository = repository;
    }

    @Override
    public Integer call() {
        InstrumentFilter filter = (type == null) ? new NoOpFilter() : new TypeFilter(type);

        for (Instrument instrument : repository) {
            if (filter.matches(instrument)) {
                System.out.println(instrument.getDescription());
            }
        }
        return 0;
    }
}
