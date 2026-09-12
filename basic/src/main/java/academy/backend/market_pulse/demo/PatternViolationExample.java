package academy.backend.market_pulse.demo;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import academy.backend.market_pulse.model.Bond;
import academy.backend.market_pulse.model.Currency;
import academy.backend.market_pulse.model.Instrument;
import academy.backend.market_pulse.model.Stock;

/**
 * Одна и та же задача, решённая двумя способами: без паттерна GoF и с ним.
 *
 * <p>Задача: пользователь ввёл {@code search AAPL} или {@code list} — приложение должно выполнить
 * нужное действие над каталогом инструментов и вернуть код возврата.
 *
 * <p>Паттерн — <b>Command</b>: запрос упаковывается в объект, и вызывающий код работает со всеми
 * запросами одинаково, не зная, что именно внутри. Оба варианта ниже дают одинаковый вывод —
 * разница не в результате, а в цене следующего изменения. Плюсы и минусы каждого — в комментариях;
 * ни один из вариантов не является «правильным ответом» сам по себе.
 */
public final class PatternViolationExample {

    private PatternViolationExample() {
    }

    // ================================================================================
    // Вариант A — без паттерна: одна точка входа и switch по имени команды
    // ================================================================================

    /**
     * Плюсы:
     * <ul>
     *     <li>вся обработка команд — в одном месте, читается сверху вниз, без прыжков по файлам;</li>
     *     <li>ноль дополнительных классов и абстракций;</li>
     *     <li>для двух команд такой код честнее и быстрее в написании — это ровно тот случай,
     *     когда KISS важнее паттерна.</li>
     * </ul>
     *
     * Минусы:
     * <ul>
     *     <li>метод растёт с каждой новой командой, а по функциональным требованиям проекта их
     *     будет не меньше семи: search, add, list, history, compare, favorites, subscribe;</li>
     *     <li>все команды живут в одном файле — правки разных людей встречаются в одном методе;</li>
     *     <li>отдельную команду не переиспользовать и не протестировать в отрыве от switch;</li>
     *     <li>общее поведение (разбор аргументов, коды возврата, справка) дублируется по веткам —
     *     и расходится, как только одну из веток поправят, а остальные забудут.</li>
     * </ul>
     */
    static int runWithoutPattern(String[] args, List<Instrument> catalog) {
        if (args.length == 0) {
            System.out.println("Укажите команду: search | list");
            return 1;
        }
        switch (args[0]) {
            case "search" -> {
                if (args.length < 2) {
                    System.out.println("search: укажите тикер");
                    return 1;
                }
                for (Instrument instrument : catalog) {
                    if (instrument.getTicker().equalsIgnoreCase(args[1])) {
                        System.out.println(instrument.getDescription());
                        return 0;
                    }
                }
                System.out.println("Инструмент не найден: " + args[1]);
                return 1;
            }
            case "list" -> {
                for (Instrument instrument : catalog) {
                    System.out.println(instrument.getDescription());
                }
                return 0;
            }
            default -> {
                System.out.println("Неизвестная команда: " + args[0]);
                return 1;
            }
        }
    }

    // ================================================================================
    // Вариант B — с паттерном Command: команда как объект
    // ================================================================================

    /**
     * Плюсы:
     * <ul>
     *     <li>новая команда — это новый класс; код, который их запускает, не меняется (OCP);</li>
     *     <li>каждую команду видно целиком в одном классе и можно протестировать отдельно;</li>
     *     <li>общее поведение (коды возврата, справка, логирование) добавляется в одном месте —
     *     там, где команда запускается;</li>
     *     <li>именно так устроен picocli: {@code @Command} + {@code Callable} — паттерн уже
     *     реализован на уровне библиотеки, в проекте его останется только использовать.</li>
     * </ul>
     *
     * Минусы:
     * <ul>
     *     <li>классов становится заметно больше, а поток управления «размазан» по файлам: чтобы
     *     понять, что делает search, нужно уйти в другой класс;</li>
     *     <li>для двух команд это оверинжиниринг — выигрыш начинает окупаться примерно с четвёртой;</li>
     *     <li>команды нужно где-то регистрировать, и этот реестр — ещё один кусок инфраструктуры,
     *     который тоже забывают обновлять.</li>
     * </ul>
     */
    interface CliCommand {

        String name();

        int execute(String[] args, List<Instrument> catalog);
    }

    static final class SearchCliCommand implements CliCommand {

        @Override
        public String name() {
            return "search";
        }

        @Override
        public int execute(String[] args, List<Instrument> catalog) {
            if (args.length < 2) {
                System.out.println("search: укажите тикер");
                return 1;
            }
            for (Instrument instrument : catalog) {
                if (instrument.getTicker().equalsIgnoreCase(args[1])) {
                    System.out.println(instrument.getDescription());
                    return 0;
                }
            }
            System.out.println("Инструмент не найден: " + args[1]);
            return 1;
        }
    }

    static final class ListCliCommand implements CliCommand {

        @Override
        public String name() {
            return "list";
        }

        @Override
        public int execute(String[] args, List<Instrument> catalog) {
            for (Instrument instrument : catalog) {
                System.out.println(instrument.getDescription());
            }
            return 0;
        }
    }

    /** Реестр команд: единственное место, которое знает обо всех командах сразу. */
    static Map<String, CliCommand> commandRegistry() {
        Map<String, CliCommand> commands = new LinkedHashMap<>();
        for (CliCommand command : List.of(new SearchCliCommand(), new ListCliCommand())) {
            commands.put(command.name(), command);
        }
        return commands;
    }

    /** Запуск: об устройстве конкретной команды этот код ничего не знает. */
    static int runWithPattern(String[] args, List<Instrument> catalog, Map<String, CliCommand> commands) {
        if (args.length == 0) {
            System.out.println("Укажите команду: " + String.join(" | ", commands.keySet()));
            return 1;
        }
        CliCommand command = commands.get(args[0]);
        if (command == null) {
            System.out.println("Неизвестная команда: " + args[0]);
            return 1;
        }
        return command.execute(args, catalog);
    }

    // ================================================================================

    public static void main(String[] args) {
        List<Instrument> catalog = List.of(
                new Stock("SBER", "Сбербанк", Currency.RUB, "Financials", new BigDecimal("6.5")),
                new Bond("OFZ26", "ОФЗ 26", Currency.RUB, new BigDecimal("7.0"), 2030));

        System.out.println("--- Вариант A: без паттерна, switch ---");
        runWithoutPattern(new String[]{"list"}, catalog);
        runWithoutPattern(new String[]{"search", "SBER"}, catalog);

        System.out.println();
        System.out.println("--- Вариант B: с паттерном Command ---");
        Map<String, CliCommand> commands = commandRegistry();
        runWithPattern(new String[]{"list"}, catalog, commands);
        runWithPattern(new String[]{"search", "SBER"}, catalog, commands);

        System.out.println();
        System.out.println("Вывод одинаковый. Разница проявится на седьмой команде — и на первом");
        System.out.println("требовании вроде «во всех командах логировать время выполнения».");
    }
}
