package academy.backend.market_pulse.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;

import org.junit.jupiter.api.Test;

class QuoteTest {

    private Stock sber() {
        return new Stock("SBER", "Сбербанк", Currency.RUB, "Banks", new BigDecimal("6.5"));
    }

    @Test
    void растущаяКотировкаПоказываетСтрелкуВверх() {
        Quote quote = new Quote(sber(), new BigDecimal("250.00"), new BigDecimal("1.20"));
        assertTrue(quote.toString().contains("▲"));
    }

    @Test
    void падающаяКотировкаПоказываетСтрелкуВниз() {
        Quote quote = new Quote(sber(), new BigDecimal("250.00"), new BigDecimal("-1.20"));
        assertTrue(quote.toString().contains("▼"));
    }

    @Test
    void нулевоеИзменениеПоказываетсяНейтральнымЗнаком() {
        Quote quote = new Quote(sber(), new BigDecimal("250.00"), BigDecimal.ZERO);
        assertTrue(quote.toString().contains("▬"));
    }

    @Test
    void getPriceВозвращаетПереданнуюЦену() {
        Quote quote = new Quote(sber(), new BigDecimal("250.00"), BigDecimal.ZERO);
        assertEquals(new BigDecimal("250.00"), quote.getPrice());
    }
}
