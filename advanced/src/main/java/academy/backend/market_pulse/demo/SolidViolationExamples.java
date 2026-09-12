package academy.backend.market_pulse.demo;

import java.math.BigDecimal;
import java.nio.file.Path;
import java.util.List;

import academy.backend.market_pulse.model.Bond;
import academy.backend.market_pulse.model.Currency;
import academy.backend.market_pulse.model.Etf;
import academy.backend.market_pulse.model.Instrument;
import academy.backend.market_pulse.model.Quote;
import academy.backend.market_pulse.model.Stock;

/**
 * Каталог нарушений принципов SOLID — по одному примеру на принцип, на домене Market Pulse.
 *
 * <p>Весь код в этом файле написан намеренно плохо: это материал для разбора, а не образец для
 * подражания. У каждого примера в комментарии — что именно нарушено, к чему это приводит на
 * дистанции и где в проекте та же задача решена правильно.
 */
public final class SolidViolationExamples {

    private SolidViolationExamples() {
    }

    // ================================================================================
    // S — Single Responsibility Principle
    // ================================================================================

    /**
     * Нарушение SRP: у класса сразу три причины для изменения — доменный расчёт, формат вывода и
     * способ доставки результата. Любая из трёх заставляет править один и тот же класс.
     *
     * <p>К чему ведёт: правка формата вывода трогает код с бизнес-логикой, и сломать расчёт можно
     * случайно, вообще не думая о нём; расчёт невозможно протестировать, не перехватывая
     * {@code System.out}; когда тот же расчёт понадобится для графика или выгрузки в файл, его
     * придётся копировать — а дальше исправлять формулу в двух местах и однажды забыть про одно.
     *
     * <p>В проекте правильно: CLI-команда разбирает аргументы и печатает результат, а решение
     * «подходит ли инструмент» вынесено в отдельную абстракцию {@code InstrumentFilter}.
     */
    static final class PositionReport {

        private final Instrument instrument;
        private final BigDecimal price;
        private final int quantity;

        PositionReport(Instrument instrument, BigDecimal price, int quantity) {
            this.instrument = instrument;
            this.price = price;
            this.quantity = quantity;
        }

        String render() {
            // причина для изменения №1 — доменный расчёт: завтра здесь появятся комиссия и налог
            BigDecimal total = price.multiply(BigDecimal.valueOf(quantity));

            // причина для изменения №2 — формат вывода: завтра попросят колонки и выравнивание
            return "=== " + instrument.getTicker() + " ===\n"
                    + "Позиция: " + quantity + " x " + price + " = " + total + " " + instrument.getCurrency();
        }

        void print() {
            // причина для изменения №3 — способ доставки: завтра это файл, JSON или HTTP-ответ
            System.out.println(render());
        }
    }

    // ================================================================================
    // O — Open/Closed Principle
    // ================================================================================

    /**
     * Нарушение OCP: поведение выбирается цепочкой {@code instanceof} по конкретным типам. Чтобы
     * добавить новый вид инструмента, нужно открыть и отредактировать этот метод — он закрыт для
     * расширения и открыт для изменения, ровно наоборот тому, что требуется.
     *
     * <p>К чему ведёт: таких цепочек в приложении заводится не одна (уровень риска, размер
     * комиссии, формат строки в выводе) — и при добавлении, скажем, фьючерса нужно найти и
     * поправить каждую. Пропущенную ветку компилятор не покажет: ошибка вылезет в рантайме уже у
     * пользователя.
     *
     * <p>В проекте правильно: {@link Instrument#getDescription()} и {@link Instrument#getType()} —
     * каждый подкласс отвечает за себя сам; новый тип добавляется новым классом и регистрацией
     * фабрики, без единой правки в существующем коде.
     */
    static String riskLevel(Instrument instrument) {
        if (instrument instanceof Bond) {
            return "низкий";
        }
        if (instrument instanceof Etf) {
            return "средний";
        }
        if (instrument instanceof Stock) {
            return "высокий";
        }
        // новый тип инструмента попадёт сюда — и узнаем мы об этом только в рантайме
        throw new IllegalStateException("Неизвестный тип инструмента: " + instrument.getType());
    }

    // ================================================================================
    // L — Liskov Substitution Principle
    // ================================================================================

    /** Базовый контракт: {@code save} сохраняет инструмент, после чего его можно найти. */
    abstract static class InstrumentStorage {

        abstract void save(Instrument instrument);

        abstract Instrument findByTicker(String ticker);
    }

    /**
     * Нарушение LSP: подкласс сужает контракт базового класса — {@code save}, который по контракту
     * обязан сохранять, не делает ничего и бросает исключение. Объект такого подкласса нельзя
     * подставить туда, где ожидается {@link InstrumentStorage}, хотя по типу он подходит.
     *
     * <p>К чему ведёт: клиентский код, написанный против базового типа, падает в рантайме на одной
     * конкретной реализации. Чтобы не падать, клиент начинает писать
     * {@code if (storage instanceof ReadOnlyStorage)} — и это сразу же нарушение OCP поверх
     * нарушения LSP. Тесты, написанные на базовом типе, зелёные; на подтипе — красные.
     *
     * <p>В проекте правильно: {@code getDividends} объявлен не в {@link Instrument} «для всех», а в
     * узком интерфейсе {@code DividendPaying}, который реализует только {@link Stock}.
     */
    static final class ReadOnlyStorage extends InstrumentStorage {

        @Override
        void save(Instrument instrument) {
            throw new UnsupportedOperationException("Хранилище доступно только для чтения");
        }

        @Override
        Instrument findByTicker(String ticker) {
            return null;
        }
    }

    /** Клиент написан против базового типа и имеет полное право вызывать {@code save}. */
    static void importAll(InstrumentStorage storage, List<Instrument> instruments) {
        for (Instrument instrument : instruments) {
            storage.save(instrument);
        }
    }

    // ================================================================================
    // I — Interface Segregation Principle
    // ================================================================================

    /**
     * Нарушение ISP: один «толстый» интерфейс на все случаи жизни — поиск, история котировок,
     * подписка на реальное время и выгрузка в файл. Реализовать его целиком сегодня не может
     * никто: история и реальное время появятся в проекте только на следующих семинарах.
     *
     * <p>К чему ведёт: реализации обрастают методами-заглушками, которые бросают исключение (а это
     * попутно ещё и нарушение LSP); команда поиска, которой нужен ровно один метод, зависит от
     * истории и подписок и перекомпилируется при любом их изменении; добавление метода в интерфейс
     * ломает разом все реализации.
     *
     * <p>В проекте правильно: {@code InstrumentRepository} намеренно узкий — добавить, найти,
     * перебрать; отдельная способность «платит дивиденды» живёт в отдельном интерфейсе.
     */
    interface MarketDataSource {

        Instrument findByTicker(String ticker);

        List<Quote> loadHistory(String ticker, int days);

        void subscribeRealtime(String ticker);

        void exportToCsv(Path file);
    }

    /** Единственная сегодняшняя реализация: один метод работает, три — заглушки. */
    static final class InMemoryMarketDataSource implements MarketDataSource {

        @Override
        public Instrument findByTicker(String ticker) {
            return null;
        }

        @Override
        public List<Quote> loadHistory(String ticker, int days) {
            throw new UnsupportedOperationException("История появится вместе с реальным источником данных");
        }

        @Override
        public void subscribeRealtime(String ticker) {
            throw new UnsupportedOperationException("Подписка на котировки ещё не реализована");
        }

        @Override
        public void exportToCsv(Path file) {
            throw new UnsupportedOperationException("Выгрузка в CSV — вообще не задача источника данных");
        }
    }

    // ================================================================================
    // D — Dependency Inversion Principle
    // ================================================================================

    /** Низкоуровневая деталь: конкретный способ добыть цену. */
    static final class HttpQuoteLoader {

        BigDecimal load(String ticker) {
            // здесь был бы реальный HTTP-запрос к внешнему API
            return BigDecimal.TEN;
        }
    }

    /**
     * Нарушение DIP: высокоуровневая логика (сколько стоит портфель) зависит не от абстракции, а от
     * конкретного класса — и вдобавок сама же его создаёт. Точки, куда можно подставить другую
     * реализацию, просто нет.
     *
     * <p>К чему ведёт: тесты этого класса пойдут в сеть — медленно и нестабильно; переезд на другой
     * источник данных превращается в правку класса, который вообще-то про деньги, а не про
     * протоколы; обернуть вызовы логированием или замером времени нечем — нет шва, в который можно
     * вклиниться.
     *
     * <p>В проекте правильно: CLI-команды получают хранилище через конструктор, а не создают его
     * сами — и потому не знают, что именно им передали.
     */
    static final class PortfolioSummary {

        private final HttpQuoteLoader loader = new HttpQuoteLoader();

        BigDecimal totalValue(List<Instrument> instruments) {
            BigDecimal total = BigDecimal.ZERO;
            for (Instrument instrument : instruments) {
                total = total.add(loader.load(instrument.getTicker()));
            }
            return total;
        }
    }

    // ================================================================================

    public static void main(String[] args) {
        Stock stock = new Stock("SBER", "Сбербанк", Currency.RUB, "Financials", new BigDecimal("6.5"));
        List<Instrument> instruments = List.of(stock);

        System.out.println("SRP и OCP прямо сейчас отрабатывают корректно — в этом и коварство:");
        new PositionReport(stock, new BigDecimal("312.40"), 10).print();
        System.out.println("Уровень риска: " + riskLevel(stock));
        System.out.println("Стоимость портфеля: " + new PortfolioSummary().totalValue(instruments));
        System.out.println();

        System.out.println("LSP: клиент вызывает save у хранилища, объявленного как InstrumentStorage");
        try {
            importAll(new ReadOnlyStorage(), instruments);
        } catch (UnsupportedOperationException e) {
            System.out.println("  -> " + e.getClass().getSimpleName() + ": " + e.getMessage());
        }

        System.out.println("ISP: клиент вызывает метод интерфейса, который реализация не поддерживает");
        try {
            new InMemoryMarketDataSource().loadHistory("SBER", 30);
        } catch (UnsupportedOperationException e) {
            System.out.println("  -> " + e.getClass().getSimpleName() + ": " + e.getMessage());
        }

        System.out.println();
        System.out.println("Упали только LSP и ISP. SRP, OCP и DIP не ломают ничего сегодня —");
        System.out.println("они лишь делают каждое следующее изменение дороже. Тем и опасны.");
    }
}
