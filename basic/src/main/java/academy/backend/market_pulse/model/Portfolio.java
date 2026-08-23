package academy.backend.market_pulse.model;

/**
 * Портфель пользователя: набор позиций (инструмент + количество). Владеет
 * своими позициями композиционно — {@link Position} не имеет смысла в отрыве
 * от портфеля и не может быть создан снаружи (см. «План семинара.md»,
 * этап 4.6 — агрегация vs композиция). Позиции отдаются массивом, а не
 * коллекцией — Java Collections Framework ещё не пройден.
 *
 * TODO (шаг 4.6, самостоятельно): реализовать этот интерфейс классом,
 * который хранит позиции и защищает их от изменения снаружи в обход
 * addPosition().
 */
public interface Portfolio {

    String getName();

    void addPosition(Instrument instrument, int quantity);

    Position[] getPositions();

    interface Position {
        Instrument getInstrument();

        int getQuantity();
    }
}
