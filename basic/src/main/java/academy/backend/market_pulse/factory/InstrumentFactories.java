package academy.backend.market_pulse.factory;

import academy.backend.market_pulse.model.Currency;
import academy.backend.market_pulse.model.Instrument;
import academy.backend.market_pulse.util.Registry;

/**
 * Реестр фабрик инструментов. Каждая фабрика регистрирует себя сама при загрузке своего класса —
 * по аналогии с {@code DriverManager} в JDBC.
 */
public final class InstrumentFactories {

    private static final Registry<String, InstrumentFactory> REGISTRY = new Registry<>();

    static {
        // Статический блок фабрики выполняется только при загрузке её класса — форсируем загрузку,
        // иначе реестр останется пустым (тот же нюанс, что и с DriverManager до JDBC 4.0).
        loadClass(StockFactory.class);
        loadClass(BondFactory.class);
        loadClass(EtfFactory.class);
    }

    private InstrumentFactories() {
    }

    public static void register(String type, InstrumentFactory factory) {
        REGISTRY.register(type.toUpperCase(), factory);
    }

    public static Instrument create(String type, String ticker, String name, Currency currency) {
        return REGISTRY.get(type.toUpperCase())
                .map(factory -> factory.create(ticker, name, currency))
                .orElseThrow(() -> new IllegalArgumentException("Unknown instrument type: " + type));
    }

    private static void loadClass(Class<?> factoryClass) {
        try {
            Class.forName(factoryClass.getName());
        } catch (ClassNotFoundException e) {
            throw new ExceptionInInitializerError(e);
        }
    }
}
