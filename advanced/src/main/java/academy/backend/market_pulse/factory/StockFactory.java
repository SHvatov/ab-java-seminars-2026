package academy.backend.market_pulse.factory;

import java.math.BigDecimal;

import academy.backend.market_pulse.model.Currency;
import academy.backend.market_pulse.model.Instrument;
import academy.backend.market_pulse.model.Stock;

public class StockFactory implements InstrumentFactory {

    @Override
    public String getSupportedType() {
        return "STOCK";
    }

    @Override
    public Instrument create(String ticker, String name, Currency currency) {
        // sector и dividendYield не собираются через CLI на этом этапе — значения по умолчанию,
        // уточняются последующим редактированием инструмента в будущих семинарах.
        return new Stock(ticker, name, currency, "Unspecified", BigDecimal.ZERO);
    }
}
