package academy.backend.market_pulse.model;

import java.util.Arrays;

/**
 * Портфель пользователя. Владеет своими позициями композиционно: {@link Position}
 * не имеет смысла в отрыве от портфеля и не может быть создан снаружи
 * (см. «План семинара.md», этап 4.6 — агрегация vs композиция). Позиции хранятся
 * в массиве, а не в коллекции — Java Collections Framework ещё не пройден.
 */
public class Portfolio {

    public static class Position {
        private final Instrument instrument;
        private final int quantity;

        private Position(Instrument instrument, int quantity) {
            this.instrument = instrument;
            this.quantity = quantity;
        }

        public Instrument getInstrument() {
            return instrument;
        }

        public int getQuantity() {
            return quantity;
        }
    }

    private final String name;
    private Position[] positions = new Position[0];

    public Portfolio(String name) {
        this.name = name;
    }

    public void addPosition(Instrument instrument, int quantity) {
        positions = Arrays.copyOf(positions, positions.length + 1);
        positions[positions.length - 1] = new Position(instrument, quantity);
    }

    public Position[] getPositions() {
        // Копия, а не сам массив: иначе вызывающий код смог бы изменить состояние
        // портфеля в обход addPosition.
        return Arrays.copyOf(positions, positions.length);
    }

    public String getName() {
        return name;
    }
}
