package academy.backend.market_pulse.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.stream.IntStream;

import org.junit.jupiter.api.Test;

import academy.backend.market_pulse.model.Currency;
import academy.backend.market_pulse.model.Quote;
import academy.backend.market_pulse.model.Stock;

class QuoteServiceTest {

    private Quote sberQuote() {
        Stock sber = new Stock("SBER", "Сбербанк", Currency.RUB, "Banks", new BigDecimal("6.5"));
        return new Quote(sber, new BigDecimal("250"), new BigDecimal("1.2"));
    }

    @Test
    void quoteForВозвращаетКотировкуИзИсточника() {
        QuoteSource source = mock(QuoteSource.class);        // подменяем сетевой источник
        Quote quote = sberQuote();
        when(source.fetch("SBER")).thenReturn(Optional.of(quote));

        QuoteService service = new QuoteService(source);

        assertEquals(Optional.of(quote), service.quoteFor("SBER"));
        verify(source).fetch("SBER");
    }

    @Test
    void кешПредотвращаетПовторныйЗапросКИсточнику() {
        QuoteSource source = mock(QuoteSource.class);
        when(source.fetch("SBER")).thenReturn(Optional.of(sberQuote()));

        QuoteService service = new QuoteService(source);
        service.quoteFor("SBER");
        service.quoteFor("SBER");

        verify(source, times(1)).fetch("SBER");   // ко второму разу берётся из кеша
    }

    @Test
    void ненайденнаяКотировкаВозвращаетEmpty() {
        QuoteSource source = mock(QuoteSource.class);
        when(source.fetch("XXX")).thenReturn(Optional.empty());

        QuoteService service = new QuoteService(source);

        assertTrue(service.quoteFor("XXX").isEmpty());
    }

    @Test
    void quotesForПропускаетНенайденныеИСохраняетПорядок() {
        Quote sber = sberQuote();
        Quote lkoh = new Quote(
                new Stock("LKOH", "Лукойл", Currency.RUB, "Oil", new BigDecimal("5")),
                new BigDecimal("7000"), new BigDecimal("2"));

        QuoteSource source = mock(QuoteSource.class);
        when(source.fetch("SBER")).thenReturn(Optional.of(sber));
        when(source.fetch("XXX")).thenReturn(Optional.empty());   // ненайденный — пропускается
        when(source.fetch("LKOH")).thenReturn(Optional.of(lkoh));

        QuoteService service = new QuoteService(source);
        List<Quote> quotes = service.quotesFor(List.of("SBER", "XXX", "LKOH"));

        // XXX выпал, порядок исходных тикеров сохранён. Замена flatMap(Optional::stream)
        // на map(Optional::get) уронила бы этот тест на "XXX".
        assertEquals(List.of(sber, lkoh), quotes);
    }

    @Test
    void quotesForНеТеряетРезультатыПодКонкуренцией() {
        int n = 1000;
        List<String> tickers = IntStream.range(0, n).mapToObj(i -> "T" + i).toList();
        // Ручной источник (не Mockito-мок: Mockito сериализует вызовы) с короткой задержкой: воркеры
        // массово перекрываются во времени и одновременно пишут в общий кеш. На HashMap такая гонка
        // теряет записи или рушит структуру; ConcurrentHashMap выдерживает.
        QuoteSource source = ticker -> {
            try {
                Thread.sleep(2);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            Stock stock = new Stock(ticker, ticker, Currency.RUB, "Sector", new BigDecimal("1"));
            return Optional.of(new Quote(stock, new BigDecimal("100"), BigDecimal.ZERO));
        };
        QuoteService service = new QuoteService(source);

        List<Quote> quotes = service.quotesFor(tickers);

        assertEquals(n, quotes.size());
        // Главная проверка — сам общий кеш, а не список результатов из CompletableFuture: на HashMap
        // часть записей терялась бы под гонкой, и cached(...) вернул бы empty. ConcurrentHashMap — нет.
        for (String ticker : tickers) {
            assertTrue(service.cached(ticker).isPresent(), "потеряно в кеше: " + ticker);
        }
    }

    @Test
    void кешДедуплицируетСетевыеВызовыМеждуВызовами() {
        QuoteSource source = mock(QuoteSource.class);
        when(source.fetch("SBER")).thenReturn(Optional.of(sberQuote()));
        QuoteService service = new QuoteService(source);

        service.quotesFor(List.of("SBER"));
        service.quotesFor(List.of("SBER"));   // второй раз берётся из кеша

        verify(source, times(1)).fetch("SBER");
    }
}
