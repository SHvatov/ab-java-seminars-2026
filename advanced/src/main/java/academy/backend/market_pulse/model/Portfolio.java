package academy.backend.market_pulse.model;

/**
 * Портфель пользователя.
 * <p>
 * Функциональные требования:
 * <ul>
 *     <li>у портфеля есть название;</li>
 *     <li>можно добавить позицию — инструмент и его количество;</li>
 *     <li>можно получить все позиции портфеля.</li>
 * </ul>
 * Нефункциональные требования:
 * <ul>
 *     <li>коллекции (List, ArrayList) ещё не пройдены — позиции отдаются массивом.</li>
 * </ul>
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
