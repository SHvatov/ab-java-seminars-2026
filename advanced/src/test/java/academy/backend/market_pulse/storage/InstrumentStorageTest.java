package academy.backend.market_pulse.storage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;

import academy.backend.market_pulse.model.Bond;
import academy.backend.market_pulse.model.Currency;
import academy.backend.market_pulse.model.Etf;
import academy.backend.market_pulse.model.Instrument;
import academy.backend.market_pulse.model.Stock;

/**
 * Характеризационный тест: фиксирует поведение сериализации каталога (сохранить → загрузить) до
 * рефакторинга домена (вынос аннотаций Jackson в mixin, см. «План семинара.md», семинар 9, этап 3).
 * После рефакторинга остаётся зелёным — поведение не меняется, меняется только место знания о JSON.
 */
class InstrumentStorageTest {

    private Instrument byTicker(List<Instrument> instruments, String ticker) {
        Optional<Instrument> found = instruments.stream()
                .filter(i -> i.getTicker().equals(ticker))
                .findFirst();
        return found.orElseThrow();
    }

    @Test
    void полиморфныйКаталогПереживаетRoundTrip() throws IOException {
        Path dir = Files.createTempDirectory("mp-storage-test");
        InstrumentStorage storage = new InstrumentStorage(dir);

        storage.save(new Stock("SBER", "Сбербанк", Currency.RUB, "Banks", new BigDecimal("6.5")));
        storage.save(new Bond("OFZ", "ОФЗ", Currency.RUB, new BigDecimal("8"), 2030));
        storage.save(new Etf("TMOS", "Т iMOEX", Currency.RUB, "IMOEX"));

        List<Instrument> loaded = storage.loadAll();

        assertEquals(3, loaded.size());
        // Полиморфизм: каждый тип восстановлен своим классом.
        Instrument sber = byTicker(loaded, "SBER");
        assertInstanceOf(Stock.class, sber);
        assertEquals("Banks", ((Stock) sber).getSector());   // поле подкласса тоже пережило
        assertInstanceOf(Bond.class, byTicker(loaded, "OFZ"));
        assertInstanceOf(Etf.class, byTicker(loaded, "TMOS"));
    }
}
