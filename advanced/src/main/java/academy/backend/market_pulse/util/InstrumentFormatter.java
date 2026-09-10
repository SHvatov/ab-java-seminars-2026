package academy.backend.market_pulse.util;

import academy.backend.market_pulse.model.Bond;
import academy.backend.market_pulse.model.Etf;
import academy.backend.market_pulse.model.Instrument;
import academy.backend.market_pulse.model.Stock;

/**
 * Наивный способ описать инструмент по типу — через {@code instanceof}.
 * Работает, но плохо масштабируется: новый тип инструмента требует найти и
 * обновить все такие цепочки по кодовой базе, компилятор об этом не
 * предупредит.
 *
 * @deprecated заменяется полиморфизмом — {@link Instrument#getDescription()}.
 * Удалить после рефакторинга.
 */
@Deprecated
public class InstrumentFormatter {

    private InstrumentFormatter() {
    }

    public static String describe(Instrument instrument) {
        if (instrument instanceof Stock stock) {
            return "Акция, сектор: " + stock.getSector();
        } else if (instrument instanceof Bond bond) {
            return "Облигация, купон: " + bond.getCouponRate() + "%, погашение: " + bond.getMaturityYear();
        } else if (instrument instanceof Etf etf) {
            return "ETF, индекс: " + etf.getTrackingIndex();
        }
        return "Неизвестный инструмент";
    }
}
