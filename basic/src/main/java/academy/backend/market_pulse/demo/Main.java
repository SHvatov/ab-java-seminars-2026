package academy.backend.market_pulse.demo;

import academy.backend.market_pulse.cli.AddCommand;
import academy.backend.market_pulse.cli.ListCommand;
import academy.backend.market_pulse.cli.MarketPulseCli;
import academy.backend.market_pulse.cli.SearchCommand;
import academy.backend.market_pulse.repository.InstrumentRepository;
import picocli.CommandLine;

/**
 * Точка входа CLI (см. «План семинара.md», семинар 2, этап 4 — Command). Команды регистрируются
 * вручную через {@code addSubcommand}, а не через атрибут {@code subcommands} аннотации
 * {@code @Command}: у команд нет конструктора без аргументов, picocli не смог бы создать их
 * самостоятельно по одному классу — каждой нужен уже готовый {@code repository}.
 */
public class Main {

    public static void main(String[] args) {
        InstrumentRepository repository = new InstrumentRepository();
        CommandLine cli = new CommandLine(new MarketPulseCli())
                .addSubcommand(new SearchCommand(repository))
                .addSubcommand(new AddCommand(repository))
                .addSubcommand(new ListCommand(repository));
        int exitCode = cli.execute(args);
        System.exit(exitCode);
    }
}
