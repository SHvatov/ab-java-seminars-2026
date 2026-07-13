package ru.tbank.marketpulse.model;

/**
 * Базовая абстракция финансового инструмента. Инкапсулирует общие для всех
 * инструментов данные (тикер, название, валюта) и защищает их инварианты
 * прямо в конструкторе.
 */
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
