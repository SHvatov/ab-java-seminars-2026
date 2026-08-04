package academy.backend.market_pulse.cli;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.Test;

import academy.backend.market_pulse.model.Bond;
import academy.backend.market_pulse.model.Currency;
import academy.backend.market_pulse.model.Stock;
import academy.backend.market_pulse.repository.InstrumentRepository;
import picocli.CommandLine;

class ListCommandTest {

    @Test
    void listФильтруетПоТипуИнструмента() {
        InstrumentRepository repository = new InstrumentRepository();
        repository.add(new Stock("SBER", "Сбербанк", Currency.RUB, "Banks", new BigDecimal("6.5")));
        repository.add(new Bond("OFZ", "ОФЗ", Currency.RUB, new BigDecimal("8"), 2030));

        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        PrintStream original = System.out;
        System.setOut(new PrintStream(buffer, true, StandardCharsets.UTF_8));
        int code;
        try {
            code = new CommandLine(new ListCommand(repository)).execute("--type", "Stock");
        } finally {
            System.setOut(original);   // System.out глобален — обязательно восстанавливаем
        }

        String output = buffer.toString(StandardCharsets.UTF_8);
        assertEquals(0, code);
        assertTrue(output.contains("Акция"));
        assertFalse(output.contains("Облигация"));
    }
}
