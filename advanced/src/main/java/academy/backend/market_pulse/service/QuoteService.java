package academy.backend.market_pulse.service;

import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Queue;
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
     * Котировки по набору тикеров без отслеживания прогресса (см. {@link #quotesFor(Collection, IntConsumer)}).
     */
    public List<Quote> quotesFor(Collection<String> tickers) {
        return quotesFor(tickers, completed -> { });
    }

    /**
     * Параллельно загружает котировки по набору тикеров: по потоку на тикер (см. «План семинара.md»,
     * семинар 10, этап 2). Каждый поток пишет результат в свою ячейку массива, поэтому общего
     * изменяемого состояния нет и синхронизация не нужна; ненайденные пропускаются, порядок исходных
     * тикеров сохраняется. По завершении каждого потока вызывается {@code onProgress} с числом уже
     * загруженных котировок — для прогресс-бара в CLI. Счётчик атомарный, так как инкрементируется из
     * нескольких потоков.
     */
    public List<Quote> quotesFor(Collection<String> tickers, IntConsumer onProgress) {
        List<String> list = List.copyOf(tickers);
        Quote[] results = new Quote[list.size()];
        Thread[] threads = new Thread[list.size()];
        AtomicInteger completed = new AtomicInteger();

        for (int i = 0; i < list.size(); i++) {
            int index = i;
            threads[i] = Thread.ofPlatform().start(() -> {
                source.fetch(list.get(index)).ifPresent(quote -> results[index] = quote);
                onProgress.accept(completed.incrementAndGet());
            });
        }
        for (Thread thread : threads) {
            joinQuietly(thread);
        }
        return Arrays.stream(results).filter(Objects::nonNull).toList();
    }

    private void joinQuietly(Thread thread) {
        try {
            thread.join();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();   // восстанавливаем флаг прерывания
        }
    }
}
