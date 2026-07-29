package academy.backend.market_pulse.model;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Список тикеров, которые отслеживает пользователь. Уникальность и быстрая проверка «отслеживается
 * ли» обеспечиваются {@link LinkedHashSet}: O(1) на add/contains, порядок добавления сохраняется.
 * Тикеры нормализуются к верхнему регистру (см. «План семинара.md», семинар 5, этап 3).
 */
public class Watchlist {

    private final Set<String> tickers = new LinkedHashSet<>();

    public boolean add(String ticker) {
        return tickers.add(ticker.toUpperCase());
    }

    public boolean contains(String ticker) {
        return tickers.contains(ticker.toUpperCase());
    }

    public Set<String> tickers() {
        return Collections.unmodifiableSet(tickers);
    }
}
