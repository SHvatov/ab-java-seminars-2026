package academy.backend.market_pulse.demo;

/**
 * Иллюстрация {@link AutoCloseable} для try-with-resources (см. «План семинара.md», семинар 4,
 * этап 4). В проекте пока нет настоящих ресурсов — файлы и сеть появятся на семинаре 7.
 */
public class InstrumentImportSession implements AutoCloseable {

    public InstrumentImportSession() {
        System.out.println("Сессия импорта открыта");
    }

    public void importLine(String line) {
        System.out.println("Импортируем: " + line);
    }

    @Override
    public void close() {
        System.out.println("Сессия импорта закрыта");
    }
}
