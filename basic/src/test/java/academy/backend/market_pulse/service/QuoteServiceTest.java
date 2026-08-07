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
}
