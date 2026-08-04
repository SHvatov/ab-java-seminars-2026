package academy.backend.market_pulse.model;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

/**
 * Базовая абстракция финансового инструмента. Инкапсулирует общие для всех
 * инструментов данные (тикер, название, валюта) и защищает их инварианты
 * прямо в конструкторе.
 *
 * <p>Аннотации Jackson задают полиморфную сериализацию в JSON (см. «План семинара.md», семинар 7,
 * этап 2): в файл пишется поле {@code type}, по которому при чтении выбирается конкретный подкласс.
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
@JsonSubTypes({
        @JsonSubTypes.Type(value = Stock.class, name = "STOCK"),
        @JsonSubTypes.Type(value = Bond.class, name = "BOND"),
        @JsonSubTypes.Type(value = Etf.class, name = "ETF")
})
public abstract class Instrument {

    private final String ticker;
    private final String name;
    private final Currency currency;

    public Instrument(String ticker, String name, Currency currency) {
        if (ticker == null || ticker.isBlank()) {
            throw new IllegalArgumentException("Ticker cannot be blank");
        }
        this.ticker = ticker;
        this.name = name;
        this.currency = currency;
    }

    public String getTicker() {
        return ticker;
    }

    public String getName() {
        return name;
    }

    public Currency getCurrency() {
        return currency;
    }

    public abstract String getDescription();

    @Override
    public String toString() {
        return ticker + " — " + name + " (" + getDescription() + ")";
    }
}
