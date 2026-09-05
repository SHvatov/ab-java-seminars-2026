package academy.backend.market_pulse.factory;

import java.util.HashMap;
import java.util.Map;
import java.util.ServiceLoader;

import academy.backend.market_pulse.model.Currency;
import academy.backend.market_pulse.model.Instrument;

/**
 * Реестр фабрик инструментов. Реализации {@link InstrumentFactory} не регистрируют себя вручную —
 * они обнаруживаются через {@link ServiceLoader} по записи в
 * {@code META-INF/services/academy.backend.market_pulse.factory.InstrumentFactory}. Добавление
 * нового типа инструмента не требует правки этого класса — только новая реализация и строка в
 * файле сервиса.
 */
public final class InstrumentFactories {

    private static final Map<String, InstrumentFactory> REGISTRY = load();

    private InstrumentFactories() {
    }

    public static Instrument create(String type, String ticker, String name, Currency currency) {
        InstrumentFactory factory = REGISTRY.get(type.toUpperCase());
        if (factory == null) {
            throw new IllegalArgumentException("Unknown instrument type: " + type);
        }
        return factory.create(ticker, name, currency);
    }

    private static Map<String, InstrumentFactory> load() {
        Map<String, InstrumentFactory> registry = new HashMap<>();
        for (InstrumentFactory factory : ServiceLoader.load(InstrumentFactory.class)) {
            registry.put(factory.type().toUpperCase(), factory);
        }
        return registry;
    }
}
