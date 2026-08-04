package academy.backend.market_pulse.demo;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import academy.backend.market_pulse.cli.AddCommand;
import academy.backend.market_pulse.cli.CompareCommand;
import academy.backend.market_pulse.cli.HistoryCommand;
import academy.backend.market_pulse.cli.ListCommand;
import academy.backend.market_pulse.cli.MarketPulseCli;
import academy.backend.market_pulse.cli.MoversCommand;
import academy.backend.market_pulse.cli.QuoteCommand;
import academy.backend.market_pulse.cli.SearchCommand;
import academy.backend.market_pulse.cli.StatsCommand;
import academy.backend.market_pulse.cli.WatchCommand;
import academy.backend.market_pulse.model.Watchlist;
import academy.backend.market_pulse.repository.InMemoryInstrumentRepository;
import academy.backend.market_pulse.repository.InstrumentRepository;
import academy.backend.market_pulse.service.HttpQuoteSource;
import academy.backend.market_pulse.service.QuoteService;
import academy.backend.market_pulse.service.QuoteSource;
import academy.backend.market_pulse.storage.InstrumentStorage;
import academy.backend.market_pulse.storage.WatchlistStorage;
import picocli.CommandLine;

/**
 * Точка входа CLI. Каталог инструментов и watchlist загружаются из файловой системы при старте и
 * переживают перезапуск (см. «План семинара.md», семинар 7, часть 1). Котировки берутся из реального
 * T-Invest API через {@link HttpQuoteSource} (часть 2); токен — из переменной окружения
 * {@code TINVEST_TOKEN}. Команда {@code history} работает на заглушке истории цен (Gatherer API,
 * семинар 6) — истории цен в реальном API здесь не запрашиваются.
 */
public class Main {

    public static void main(String[] args) {
        Path dataDir = Path.of("data");
        InstrumentStorage instrumentStorage = new InstrumentStorage(dataDir);
        WatchlistStorage watchlistStorage = new WatchlistStorage(dataDir);

        InstrumentRepository repository = new InMemoryInstrumentRepository();
        Watchlist watchlist = new Watchlist();
        loadState(instrumentStorage, watchlistStorage, repository, watchlist, dataDir);

        String token = System.getenv("TINVEST_TOKEN");
        if (token == null || token.isBlank()) {
            System.err.println("Переменная окружения TINVEST_TOKEN не задана — "
                    + "сетевые команды (quote/stats/movers/compare) вернут пустой результат.");
        }
        QuoteSource source = new HttpQuoteSource(token, repository);
        QuoteService quoteService = new QuoteService(source);

        CommandLine cli = new CommandLine(new MarketPulseCli())
                .addSubcommand(new SearchCommand(repository))
                .addSubcommand(new AddCommand(repository, instrumentStorage))
                .addSubcommand(new ListCommand(repository))
                .addSubcommand(new WatchCommand(watchlist, watchlistStorage))
                .addSubcommand(new QuoteCommand(quoteService))
                .addSubcommand(new StatsCommand(watchlist, quoteService))
                .addSubcommand(new CompareCommand(quoteService))
                .addSubcommand(new MoversCommand(watchlist, quoteService))
                .addSubcommand(new HistoryCommand(samplePriceHistory()));
        int exitCode = cli.execute(args);
        System.exit(exitCode);
    }

    private static void loadState(InstrumentStorage instrumentStorage, WatchlistStorage watchlistStorage,
                                  InstrumentRepository repository, Watchlist watchlist, Path dataDir) {
        try {
            instrumentStorage.loadAll().forEach(repository::add);
            watchlistStorage.load().forEach(watchlist::add);
        } catch (IOException e) {
            System.err.println("Не удалось загрузить данные из " + dataDir + ": " + e.getMessage());
        }
    }

    /**
     * Заглушка истории цен по тикерам — для команды {@code history} (скользящее среднее через
     * Gatherer API, семинар 6).
     */
    private static Map<String, List<BigDecimal>> samplePriceHistory() {
        Map<String, List<BigDecimal>> history = new LinkedHashMap<>();
        history.put("SBER", List.of(
                new BigDecimal("244.00"), new BigDecimal("246.50"), new BigDecimal("245.00"),
                new BigDecimal("248.00"), new BigDecimal("250.00"), new BigDecimal("249.50"),
                new BigDecimal("251.20")));
        history.put("LKOH", List.of(
                new BigDecimal("6950.00"), new BigDecimal("7020.00"), new BigDecimal("6990.00"),
                new BigDecimal("7080.00"), new BigDecimal("7100.00"), new BigDecimal("7150.00")));
        return history;
    }
}
