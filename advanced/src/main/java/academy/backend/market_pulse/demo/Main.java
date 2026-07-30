package academy.backend.market_pulse.demo;

import java.math.BigDecimal;
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
 * вручную через {@code addSubcommand}: каждой нужны уже готовые общие на приложение
 * {@code repository}/{@code watchlist}/сервисы. Каталог инструментов, котировок и истории цен засеян
 * заглушкой (см. «План семинара.md», семинар 6): аналитические команды (`stats`, `compare`,
 * `movers`, `history`) работают на этих данных до появления реального источника на семинаре 7.
 */
public class Main {

    public static void main(String[] args) {
        InstrumentRepository repository = new InMemoryInstrumentRepository();
        Watchlist watchlist = new Watchlist();
        QuoteSource source = new QuoteSource(sampleData(repository));
        QuoteService quoteService = new QuoteService(source);

        CommandLine cli = new CommandLine(new MarketPulseCli())
                .addSubcommand(new SearchCommand(repository))
                .addSubcommand(new AddCommand(repository))
                .addSubcommand(new ListCommand(repository))
                .addSubcommand(new WatchCommand(watchlist))
                .addSubcommand(new QuoteCommand(quoteService))
                .addSubcommand(new StatsCommand(source))
                .addSubcommand(new CompareCommand(quoteService))
                .addSubcommand(new MoversCommand(source))
                .addSubcommand(new HistoryCommand(samplePriceHistory()));
        int exitCode = cli.execute(args);
        System.exit(exitCode);
    }

    /**
     * Заглушка каталога: наполняет репозиторий заготовленными инструментами и возвращает котировки
     * по ним для источника. Реальные данные (сетевой источник) придут на смену на семинаре 7.
     */
    private static Map<String, Quote> sampleData(InstrumentRepository repository) {
        Instrument sber = InstrumentFactories.create("STOCK", "SBER", "Сбербанк", Currency.RUB);
        Instrument gazp = InstrumentFactories.create("STOCK", "GAZP", "Газпром", Currency.RUB);
        Instrument lkoh = InstrumentFactories.create("STOCK", "LKOH", "Лукойл", Currency.RUB);
        Instrument aapl = InstrumentFactories.create("STOCK", "AAPL", "Apple", Currency.USD);
        Instrument ofz = InstrumentFactories.create("BOND", "OFZ26240", "ОФЗ 26240", Currency.RUB);
        Instrument tmos = InstrumentFactories.create("ETF", "TMOS", "Т-Капитал iMOEX", Currency.RUB);

        List<Instrument> catalog = List.of(sber, gazp, lkoh, aapl, ofz, tmos);
        catalog.forEach(repository::add);

        Map<String, Quote> quotes = new LinkedHashMap<>();
        quotes.put("SBER", new Quote(sber, new BigDecimal("250.00"), new BigDecimal("1.20")));
        quotes.put("GAZP", new Quote(gazp, new BigDecimal("120.50"), new BigDecimal("-0.80")));
        quotes.put("LKOH", new Quote(lkoh, new BigDecimal("7100.00"), new BigDecimal("2.30")));
        quotes.put("AAPL", new Quote(aapl, new BigDecimal("225.30"), new BigDecimal("0.50")));
        quotes.put("OFZ26240", new Quote(ofz, new BigDecimal("780.00"), new BigDecimal("-0.15")));
        quotes.put("TMOS", new Quote(tmos, new BigDecimal("6.85"), new BigDecimal("0.90")));
        return quotes;
    }

    /**
     * Заглушка истории цен по тикерам — для команды `history` (скользящее среднее через Gatherer API).
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
