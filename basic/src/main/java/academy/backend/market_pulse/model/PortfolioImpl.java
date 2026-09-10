package academy.backend.market_pulse.model;

import java.util.Arrays;

/**
 * Эталонная реализация {@link Portfolio}: хранит позиции в массиве.
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
        return positions;
    }

    @Override
    public String getName() {
        return name;
    }
}
