package academy.backend.market_pulse.demo;

import java.math.BigDecimal;
import java.util.Map;

import academy.backend.market_pulse.cli.AddCommand;
import academy.backend.market_pulse.cli.ListCommand;
import academy.backend.market_pulse.cli.MarketPulseCli;
import academy.backend.market_pulse.cli.QuoteCommand;
import academy.backend.market_pulse.cli.SearchCommand;
import academy.backend.market_pulse.cli.WatchCommand;
import academy.backend.market_pulse.factory.InstrumentFactories;
import academy.backend.market_pulse.model.Currency;
import academy.backend.market_pulse.model.Instrument;
import academy.backend.market_pulse.model.Quote;
import academy.backend.market_pulse.model.Watchlist;
import academy.backend.market_pulse.repository.InMemoryInstrumentRepository;
import academy.backend.market_pulse.repository.InstrumentRepository;
import academy.backend.market_pulse.service.QuoteService;
import academy.backend.market_pulse.service.QuoteSource;
import picocli.CommandLine;

/**
 * Точка входа CLI (см. «План семинара.md», семинар 2, этап 4 — Command). Команды регистрируются
 * вручную через {@code addSubcommand}, а не через атрибут {@code subcommands} аннотации
 * {@code @Command}: у команд нет конструктора без аргументов, picocli не смог бы создать их
 * самостоятельно по одному классу — каждой нужны уже готовые общие на приложение
 * {@code repository}/{@code watchlist}/сервисы (см. «План семинара.md», семинар 5).
 */
public class Main {

    public static void main(String[] args) {
        InstrumentRepository repository = new InMemoryInstrumentRepository();
        Watchlist watchlist = new Watchlist();
        QuoteService quoteService = new QuoteService(new QuoteSource(sampleQuotes()));

        CommandLine cli = new CommandLine(new MarketPulseCli())
                .addSubcommand(new SearchCommand(repository))
                .addSubcommand(new AddCommand(repository))
                .addSubcommand(new ListCommand(repository))
                .addSubcommand(new WatchCommand(watchlist))
                .addSubcommand(new QuoteCommand(quoteService));
        int exitCode = cli.execute(args);
        System.exit(exitCode);
    }

    /**
     * Заглушка внешнего источника котировок — заранее заготовленные данные вместо реального
     * сетевого запроса (реальный источник появится на семинаре 7).
     */
    private static Map<String, Quote> sampleQuotes() {
        Instrument sber = InstrumentFactories.create("STOCK", "SBER", "Сбербанк", Currency.RUB);
        Instrument gazp = InstrumentFactories.create("STOCK", "GAZP", "Газпром", Currency.RUB);
        return Map.of(
                "SBER", new Quote(sber, new BigDecimal("250.00"), new BigDecimal("1.20")),
                "GAZP", new Quote(gazp, new BigDecimal("120.50"), new BigDecimal("-0.80")));
    }
}
