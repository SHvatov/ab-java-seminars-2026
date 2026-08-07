package academy.backend.market_pulse.service;

import java.util.ArrayDeque;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Queue;

import academy.backend.market_pulse.model.Quote;

/**
 * Обработка пользовательских запросов котировок. Запросы копятся в очереди {@link Queue} и
 * обрабатываются по порядку поступления (FIFO); полученные котировки складываются в кеш {@link Map},
 * чтобы повторный запрос не ходил к источнику снова. Разделение «положить запрос» и «обработать» —
 * заготовка под асинхронную обработку (см. «План семинара.md», семинар 5, этап 4).
 */
public class QuoteService {

    private final QuoteSource source;
    private final Queue<String> pending = new ArrayDeque<>();
    private final Map<String, Quote> cache = new HashMap<>();

    public QuoteService(QuoteSource source) {
        this.source = source;
    }

    public void request(String ticker) {
        pending.offer(ticker.toUpperCase());
    }

    public void processAll() {
        while (!pending.isEmpty()) {
            String ticker = pending.poll();
            if (cache.containsKey(ticker)) {
                continue;
            }
            source.fetch(ticker).ifPresent(quote -> cache.put(ticker, quote));
        }
    }

    public Optional<Quote> cached(String ticker) {
        return Optional.ofNullable(cache.get(ticker.toUpperCase()));
    }

    /**
     * Удобная обёртка «запросить и получить»: кладёт запрос в очередь, обрабатывает её и отдаёт
     * котировку из кеша. Используется командами, которым нужна котировка по конкретному тикеру
     * прямо сейчас (см. «План семинара.md», семинар 6, команда `compare`).
     */
    public Optional<Quote> quoteFor(String ticker) {
        request(ticker);
        processAll();
        return cached(ticker);
    }

    /**
     * Котировки по набору тикеров: ненайденные пропускаются, порядок исходных тикеров сохраняется.
     * Выделено из команд `stats`/`movers`/`compare`, где этот конвейер дублировался
     * (см. «План семинара.md», семинар 9, этап 2 — устранение дублирования).
     */
    public List<Quote> quotesFor(Collection<String> tickers) {
        return tickers.stream()
                .map(this::quoteFor)
                .flatMap(Optional::stream)
                .toList();
    }
}
