package academy.backend.market_pulse.demo;

/**
 * try-with-resources и подавленные исключения (suppressed exceptions), см. «План семинара.md»,
 * семинар 4, этап 4.
 */
public class TryWithResourcesDemo {

    public static void main(String[] args) {
        try (InstrumentImportSession session = new InstrumentImportSession()) {
            session.importLine("STOCK,SBER,Сбербанк,250");
        } // close() вызывается автоматически — в том числе если importLine бросит исключение

        try (FailingInstrumentImportSession session = new FailingInstrumentImportSession()) {
            throw new IllegalStateException("Ошибка импорта строки");
        } catch (Exception e) {
            // наружу вылетает IllegalStateException из тела блока — оно основное;
            // IOException из close() прикреплён к нему как подавленное, а не потерян
            System.out.println("Основное исключение: " + e);
            for (Throwable suppressed : e.getSuppressed()) {
                System.out.println("Подавленное исключение: " + suppressed);
            }
        }
    }
}
