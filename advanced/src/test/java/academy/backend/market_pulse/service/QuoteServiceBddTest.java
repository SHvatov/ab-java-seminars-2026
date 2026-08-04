package academy.backend.market_pulse.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import academy.backend.market_pulse.model.Currency;
import academy.backend.market_pulse.model.Quote;
import academy.backend.market_pulse.model.Stock;

/**
 * Тесты {@link QuoteService} в BDD-стиле Given-When-Then, оформленные через {@code @Nested} и
 * {@code @DisplayName} (см. «План семинара.md», семинар 8, этап 3). Отчёт JUnit читается как
 * спецификация поведения.
 */
@DisplayName("QuoteService")
class QuoteServiceBddTest {

    private Quote sberQuote() {
        Stock sber = new Stock("SBER", "Сбербанк", Currency.RUB, "Banks", new BigDecimal("6.5"));
        return new Quote(sber, new BigDecimal("250"), new BigDecimal("1.2"));
    }

    @Nested
    @DisplayName("когда котировка запрошена впервые")
    class WhenFirstRequested {

        @Test
        @DisplayName("тогда котировка берётся у источника")
        void беретКотировкуУИсточника() {
            // Given
            QuoteSource source = mock(QuoteSource.class);
            Quote quote = sberQuote();
            when(source.fetch("SBER")).thenReturn(Optional.of(quote));
            QuoteService service = new QuoteService(source);

            // When
            Optional<Quote> result = service.quoteFor("SBER");

            // Then
            assertEquals(Optional.of(quote), result);
            verify(source).fetch("SBER");
        }
    }

    @Nested
    @DisplayName("когда котировка уже в кеше")
    class WhenCached {

        @Test
        @DisplayName("тогда повторный запрос не идёт к источнику")
        void неХодитКИсточникуПовторно() {
            // Given
            QuoteSource source = mock(QuoteSource.class);
            when(source.fetch("SBER")).thenReturn(Optional.of(sberQuote()));
            QuoteService service = new QuoteService(source);

            // When
            service.quoteFor("SBER");
            service.quoteFor("SBER");

            // Then
            verify(source, times(1)).fetch("SBER");
        }
    }
}
