package academy.backend.market_pulse.demo;

import java.io.IOException;

/**
 * Сессия импорта, чей {@code close()} всегда бросает исключение — используется только для
 * демонстрации подавленных исключений (suppressed exceptions), см. «План семинара.md»,
 * семинар 4, этап 4.
 */
public class FailingInstrumentImportSession implements AutoCloseable {

    public void importLine(String line) {
        System.out.println("Импортируем: " + line);
    }

    @Override
    public void close() throws IOException {
        throw new IOException("Не удалось закрыть сессию импорта");
    }
}
