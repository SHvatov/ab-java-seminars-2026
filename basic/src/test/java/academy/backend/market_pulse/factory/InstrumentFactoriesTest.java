package academy.backend.market_pulse.factory;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

import academy.backend.market_pulse.model.Currency;
import academy.backend.market_pulse.model.Instrument;
import academy.backend.market_pulse.model.Stock;

class InstrumentFactoriesTest {

    @Test
    void неизвестныйТипБросаетIllegalArgumentException() {
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
                () -> InstrumentFactories.create("CRYPTO", "BTC", "Bitcoin", Currency.RUB));
        assertEquals(true, e.getMessage().contains("CRYPTO"));
    }

    @Test
    void stockСоздаётсяФабрикой() {
        Instrument instrument = InstrumentFactories.create("STOCK", "SBER", "Сбербанк", Currency.RUB);
        assertInstanceOf(Stock.class, instrument);
        assertEquals("SBER", instrument.getTicker());
    }
}
