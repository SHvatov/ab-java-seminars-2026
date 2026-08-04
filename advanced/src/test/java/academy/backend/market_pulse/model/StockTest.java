package academy.backend.market_pulse.model;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.math.BigDecimal;

import org.junit.jupiter.api.Test;

class StockTest {

    @Test
    void дивидендыЭтоЦенаУмноженнаяНаДоходность() {
        Stock stock = new Stock("SBER", "Сбербанк", Currency.RUB, "Banks", new BigDecimal("10"));
        // 250.00 * 10 / 100 = 25.00
        assertEquals(new BigDecimal("25.00"), stock.getDividends(new BigDecimal("250.00")));
    }

    @Test
    void дивидендыОкругляютсяHalfUp() {
        Stock stock = new Stock("SBER", "Сбербанк", Currency.RUB, "Banks", new BigDecimal("3"));
        // 33.33 * 3 / 100 = 0.9999 -> округление HALF_UP до масштаба 2 = 1.00
        assertEquals(new BigDecimal("1.00"), stock.getDividends(new BigDecimal("33.33")));
    }
}
