package academy.backend.market_pulse.demo;

import java.math.BigDecimal;
import java.util.List;

import academy.backend.market_pulse.model.Bond;
import academy.backend.market_pulse.model.Currency;
import academy.backend.market_pulse.model.Etf;
import academy.backend.market_pulse.model.Instrument;
import academy.backend.market_pulse.model.Stock;

/**
 * Временная точка входа для проверки доменной модели семинара 1: работает
 * на моках, без обращения к реальному T-Invest API.
 * <p>
 * TODO: заменить на полноценный CLI (см. следующие семинары) — этот класс
 * не предназначен для использования как конечный пользовательский интерфейс.
 */
public class Main {

    public static void main(String[] args) {
        Stock sber = new Stock("SBER", "Сбербанк", Currency.RUB,
                "Financials", new BigDecimal("6.5"));
        Bond ofz = new Bond("SU26238RMFS4", "ОФЗ-26238", Currency.RUB,
                new BigDecimal("7.1"), 2035);
        Etf tmos = new Etf("TMOS", "Тинькофф iMOEX", Currency.RUB, "MOEX");

        // Полиморфизм подтипов: как только появится getDescription(), toString()
        // каждого инструмента будет печатать своё специфичное описание, хотя
        // обращаемся мы к ним через общий тип Instrument.
        List<Instrument> instruments = List.of(sber, ofz, tmos);
        for (Instrument instrument : instruments) {
            System.out.println(instrument);
        }

        // TODO (шаг 4.5): построить Quote для sber и ofz, вывести котировки
        // и дивиденды по ним.

        // TODO (шаг 4.6): построить Portfolio, добавить позиции (sber x10,
        // ofz x5, tmos x3) и вывести список позиций.
    }
}
