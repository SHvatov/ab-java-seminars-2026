package academy.backend.market_pulse.demo;

import java.math.BigDecimal;
import java.util.List;

import academy.backend.market_pulse.model.Bond;
import academy.backend.market_pulse.model.Currency;
import academy.backend.market_pulse.model.Etf;
import academy.backend.market_pulse.model.Instrument;
import academy.backend.market_pulse.model.Stock;

public class Main {

    public static void main(String[] args) {
        Stock sber = new Stock("SBER", "Сбербанк", Currency.RUB,
                "Financials", new BigDecimal("6.5"));
        Bond ofz = new Bond("SU26238RMFS4", "ОФЗ-26238", Currency.RUB,
                new BigDecimal("7.1"), 2035);
        Etf tmos = new Etf("TMOS", "Тинькофф iMOEX", Currency.RUB, "MOEX");

        // NOTICE: toString() каждого инструмента вызывает getDescription() —
        // до реализации метода в Stock/Bond/Etf этот вызов бросает исключение.
        List<Instrument> instruments = List.of(sber, ofz, tmos);
        for (Instrument instrument : instruments) {
            System.out.println(instrument);
        }

        // TODO: построить Quote для sber и ofz, вывести котировки и дивиденды по ним.

        // TODO: построить Portfolio, добавить позиции (sber x10, ofz x5, tmos x3)
        // и вывести список позиций.
    }
}
