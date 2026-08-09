package academy.backend.market_pulse.service;

import java.util.ArrayDeque;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Queue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.IntConsumer;

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
    // ConcurrentHashMap, а не HashMap: в кеш пишут параллельные воркеры quotesFor
    // (см. «План семинара.md», семинар 11).
    private final Map<String, Quote> cache = new ConcurrentHashMap<>();

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
     * Котировки по набору тикеров без отслеживания прогресса (см. {@link #quotesFor(Collection, IntConsumer)}).
     */
    public List<Quote> quotesFor(Collection<String> tickers) {
        return quotesFor(tickers, completed -> { });
    }

    /**
     * Параллельно загружает котировки по набору тикеров через {@link ExecutorService} на виртуальных
     * потоках (см. «План семинара.md», семинар 12): по одной задаче на тикер, каждая — отдельный
     * {@link CompletableFuture}. Ручное управление потоками (семинар 10) заменено пулом, задачи
     * отделены от исполнителя. Порядок результатов сохраняется порядком в списке futures, ненайденные
     * пропускаются. Котировки кешируются в общий {@link ConcurrentHashMap} через атомарный
     * {@code computeIfAbsent} (семинар 11); счётчик прогресса атомарный.
     */
    public List<Quote> quotesFor(Collection<String> tickers, IntConsumer onProgress) {
        List<String> list = List.copyOf(tickers);
        AtomicInteger completed = new AtomicInteger();

        try (ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor()) {
            List<CompletableFuture<Optional<Quote>>> futures = list.stream()
                    .map(ticker -> CompletableFuture.supplyAsync(() -> {
                        Optional<Quote> quote = cachedFetch(ticker);
                        onProgress.accept(completed.incrementAndGet());
                        return quote;
                    }, executor))
                    .toList();

            return futures.stream()
                    .map(CompletableFuture::join)
                    .flatMap(Optional::stream)
                    .toList();
        }
    }

    private Optional<Quote> cachedFetch(String ticker) {
        return Optional.ofNullable(
                cache.computeIfAbsent(ticker.toUpperCase(), key -> source.fetch(ticker).orElse(null)));
    }
}
