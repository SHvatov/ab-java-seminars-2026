package academy.backend.market_pulse.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class WatchlistTest {

    private Watchlist watchlist;

    @BeforeEach
    void setUp() {
        watchlist = new Watchlist();
    }

    @Test
    void добавлениеНовогоТикераВозвращаетTrue() {
        assertTrue(watchlist.add("SBER"));
    }

    @Test
    void дубликатНезависимоОтРегистраНеДобавляется() {
        watchlist.add("SBER");
        assertFalse(watchlist.add("sber"));
        assertEquals(1, watchlist.tickers().size());
    }

    @Test
    void containsРегистронезависим() {
        watchlist.add("SBER");
        assertTrue(watchlist.contains("sber"));
    }

    @Test
    void removeУбираетТикерНезависимоОтРегистра() {
        watchlist.add("SBER");
        assertTrue(watchlist.remove("sber"));
        assertFalse(watchlist.contains("SBER"));
    }

    @Test
    void removeНеотслеживаемогоТикераВозвращаетFalse() {
        assertFalse(watchlist.remove("XXX"));
    }

    @Test
    void tickersОтдаётсяНеизменяемым() {
        watchlist.add("SBER");
        assertThrows(UnsupportedOperationException.class, () -> watchlist.tickers().add("GAZP"));
    }
}
