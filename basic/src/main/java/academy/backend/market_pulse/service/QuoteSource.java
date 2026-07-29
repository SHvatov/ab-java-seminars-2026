package academy.backend.market_pulse.service;

import java.util.Map;
import java.util.Optional;

import academy.backend.market_pulse.model.Quote;

/**
 * Заглушка внешнего источника котировок: отдаёт заранее заготовленные данные по тикеру, без
 * реального сетевого взаимодействия. Полноценный сетевой клиент придёт на смену на семинаре 7
 * (см. «План семинара.md», семинар 5, этап 4).
 */
public class QuoteSource {

    private final Map<String, Quote> data;

    public QuoteSource(Map<String, Quote> data) {
        this.data = data;
    }

    public Optional<Quote> fetch(String ticker) {
        return Optional.ofNullable(data.get(ticker.toUpperCase()));
    }
}
