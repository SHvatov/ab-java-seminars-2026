package academy.backend.market_pulse.model;

/**
 * Портфель пользователя: набор позиций (инструмент + количество). Владеет
 * своими позициями композиционно — {@link Position} не имеет смысла в отрыве
 * от портфеля и не может быть создан снаружи (см. «План семинара.md» —
 * агрегация vs композиция). Позиции отдаются массивом, а не коллекцией —
 * Java Collections Framework ещё не пройден.
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
