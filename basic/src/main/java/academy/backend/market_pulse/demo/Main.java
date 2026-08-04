package academy.backend.market_pulse.demo;

import java.io.IOException;
import java.nio.file.Path;

import academy.backend.market_pulse.cli.AddCommand;
import academy.backend.market_pulse.cli.CompareCommand;
import academy.backend.market_pulse.cli.ListCommand;
import academy.backend.market_pulse.cli.MarketPulseCli;
import academy.backend.market_pulse.cli.MoversCommand;
import academy.backend.market_pulse.cli.QuoteCommand;
import academy.backend.market_pulse.cli.SearchCommand;
import academy.backend.market_pulse.cli.StatsCommand;
import academy.backend.market_pulse.cli.WatchCommand;
import academy.backend.market_pulse.model.Watchlist;
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
 * T-Invest API через {@link HttpQuoteSource} (часть 2); токен читается из переменной окружения
 * {@code TINVEST_TOKEN}. Команды регистрируются вручную через {@code addSubcommand}, получая общие
 * на приложение репозиторий/watchlist/сервисы и хранилища.
 */
public class Main {

    public static void main(String[] args) {
        Path dataDir = Path.of("data");
        InstrumentStorage instrumentStorage = new InstrumentStorage(dataDir);
        WatchlistStorage watchlistStorage = new WatchlistStorage(dataDir);

        InstrumentRepository repository = new InstrumentRepository();
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
                .addSubcommand(new MoversCommand(watchlist, quoteService));
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
}
