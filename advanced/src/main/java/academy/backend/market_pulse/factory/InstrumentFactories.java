package academy.backend.market_pulse.factory;

import java.util.ServiceLoader;

import academy.backend.market_pulse.model.Currency;
import academy.backend.market_pulse.model.Instrument;
import academy.backend.market_pulse.util.Registry;

/**
 * Реестр фабрик инструментов. Реализации {@link InstrumentFactory} не перечислены здесь явно —
 * они обнаруживаются через {@link ServiceLoader} по файлу
 * {@code META-INF/services/academy.backend.market_pulse.factory.InstrumentFactory}. Добавление
 * нового типа инструмента сводится к реализации нового {@link InstrumentFactory} и добавлению
 * одной строки в этот файл — ни этот класс, ни существующие фабрики не изменяются.
 */
public final class InstrumentFactories {

    private static final Registry<String, InstrumentFactory> REGISTRY = loadRegistry();

    private InstrumentFactories() {
    }

    public static Instrument create(String type, String ticker, String name, Currency currency) {
        return REGISTRY.get(type.toUpperCase())
                .map(factory -> factory.create(ticker, name, currency))
                .orElseThrow(() -> new IllegalArgumentException("Unknown instrument type: " + type));
    }

    private static Registry<String, InstrumentFactory> loadRegistry() {
        Registry<String, InstrumentFactory> registry = new Registry<>();
        for (InstrumentFactory factory : ServiceLoader.load(InstrumentFactory.class)) {
            registry.register(factory.getSupportedType().toUpperCase(), factory);
        }
        return registry;
    }
}
