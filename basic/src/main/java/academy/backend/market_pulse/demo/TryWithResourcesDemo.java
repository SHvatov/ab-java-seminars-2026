package academy.backend.market_pulse.demo;

/**
 * try-with-resources и его ручной эквивалент через try/finally (см. «План семинара.md»,
 * семинар 4, этап 4).
 */
public class TryWithResourcesDemo {

    public static void main(String[] args) {
        try (InstrumentImportSession session = new InstrumentImportSession()) {
            session.importLine("STOCK,SBER,Сбербанк,250");
        } // close() вызывается автоматически — в том числе если importLine бросит исключение

        InstrumentImportSession session = new InstrumentImportSession();
        try {
            session.importLine("STOCK,SBER,Сбербанк,250");
        } finally {
            session.close();
        }
    }
}
