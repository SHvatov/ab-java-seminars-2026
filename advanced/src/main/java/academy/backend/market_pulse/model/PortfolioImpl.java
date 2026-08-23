package academy.backend.market_pulse.model;

import java.util.Arrays;

/**
 * Эталонная реализация {@link Portfolio}: хранит позиции в массиве и
 * защищает их от изменения снаружи в обход {@link #addPosition}.
 */
public class PortfolioImpl implements Portfolio {

    private static final class PositionRecord implements Position {
        private final Instrument instrument;
        private final int quantity;

        private PositionRecord(Instrument instrument, int quantity) {
            this.instrument = instrument;
            this.quantity = quantity;
        }

        @Override
        public Instrument getInstrument() {
            return instrument;
        }

        @Override
        public int getQuantity() {
            return quantity;
        }
    }

    private final String name;
    private Position[] positions = new Position[0];

    public PortfolioImpl(String name) {
        this.name = name;
    }

    @Override
    public void addPosition(Instrument instrument, int quantity) {
        positions = Arrays.copyOf(positions, positions.length + 1);
        positions[positions.length - 1] = new PositionRecord(instrument, quantity);
    }

    @Override
    public Position[] getPositions() {
        // Копия, а не сам массив: иначе вызывающий код смог бы изменить состояние
        // портфеля в обход addPosition.
        return Arrays.copyOf(positions, positions.length);
    }

    @Override
    public String getName() {
        return name;
    }
}
