package academy.backend.market_pulse.util;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Обобщённый реестр «ключ → значение», переиспользуемый где угодно — например, для реестра
 * фабрик инструментов или кеша котировок (см. «План семинара.md», семинар 3, этап 6).
 */
public class Registry<K, V> {

    private final Map<K, V> items = new HashMap<>();

    public void register(K key, V value) {
        items.put(key, value);
    }

    public Optional<V> get(K key) {
        return Optional.ofNullable(items.get(key));
    }
}
