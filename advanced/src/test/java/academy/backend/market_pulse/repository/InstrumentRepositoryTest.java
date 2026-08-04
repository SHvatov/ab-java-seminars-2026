package academy.backend.market_pulse.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import academy.backend.market_pulse.exception.DuplicateTickerException;
import academy.backend.market_pulse.model.Currency;
import academy.backend.market_pulse.model.Stock;

class InstrumentRepositoryTest {

    private InstrumentRepository repository;

    @BeforeEach
    void setUp() {
        repository = new InMemoryInstrumentRepository();   // тестируем через интерфейс
    }

    private Stock sber() {
        return new Stock("SBER", "Сбербанк", Currency.RUB, "Banks", new BigDecimal("6.5"));
    }

    @Test
    void findByTickerРегистронезависим() {
        repository.add(sber());
        assertTrue(repository.findByTicker("sber").isPresent());
    }

    @Test
    void findByTickerОтсутствующегоВозвращаетEmpty() {
        assertTrue(repository.findByTicker("XXX").isEmpty());
    }

    @Test
    void повторныйТикерБросаетDuplicateTickerException() {
        repository.add(sber());
        assertThrows(DuplicateTickerException.class, () -> repository.add(sber()));
    }

    @Test
    void streamВозвращаетДобавленныеИнструменты() {
        repository.add(sber());
        assertEquals(1, repository.stream().count());
    }
}
