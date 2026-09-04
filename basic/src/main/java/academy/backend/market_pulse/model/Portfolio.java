package academy.backend.market_pulse.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Портфель пользователя. Владеет своими позициями композиционно: {@link Position}
 * не имеет смысла в отрыве от портфеля и не может быть создан снаружи
 * (см. «План семинара.md», этап 4.7, шаг 3 — агрегация vs композиция).
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
    private final List<Position> positions = new ArrayList<>();

    public Portfolio(String name) {
        this.name = name;
    }

    public void addPosition(Instrument instrument, int quantity) {
        positions.add(new Position(instrument, quantity));
    }

    public List<Position> getPositions() {
        return Collections.unmodifiableList(positions);
    }

    public String getName() {
        return name;
    }
}
