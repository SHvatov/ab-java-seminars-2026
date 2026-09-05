package academy.backend.market_pulse.factory;

import java.util.HashMap;
import java.util.Map;

import academy.backend.market_pulse.model.Currency;
import academy.backend.market_pulse.model.Instrument;

/**
 * Реестр фабрик инструментов. Каждая фабрика регистрирует себя сама при загрузке своего класса —
 * по аналогии с {@code DriverManager} в JDBC.
 */
public final class InstrumentFactories {

    private static final Map<String, InstrumentFactory> REGISTRY = new HashMap<>();

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
        REGISTRY.put(type.toUpperCase(), factory);
    }

    public static Instrument create(String type, String ticker, String name, Currency currency) {
        InstrumentFactory factory = REGISTRY.get(type.toUpperCase());
        if (factory == null) {
            throw new IllegalArgumentException("Unknown instrument type: " + type);
        }
        return factory.create(ticker, name, currency);
    }

    private static void loadClass(Class<?> factoryClass) {
        try {
            Class.forName(factoryClass.getName());
        } catch (ClassNotFoundException e) {
            throw new ExceptionInInitializerError(e);
        }
    }
}
