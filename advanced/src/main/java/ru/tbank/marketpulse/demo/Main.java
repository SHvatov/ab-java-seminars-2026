package ru.tbank.marketpulse.demo;

import java.math.BigDecimal;
import java.util.List;

import ru.tbank.marketpulse.model.Bond;
import ru.tbank.marketpulse.model.Currency;
import ru.tbank.marketpulse.model.Etf;
import ru.tbank.marketpulse.model.Instrument;
import ru.tbank.marketpulse.model.Portfolio;
import ru.tbank.marketpulse.model.Quote;
import ru.tbank.marketpulse.model.Stock;

/**
 * Временная точка входа для проверки доменной модели семинара 1: работает
 * на моках, без обращения к реальному T-Invest API.
 *
 * TODO: заменить на полноценный CLI (см. следующие семинары) — этот класс
 * не предназначен для использования как конечный пользовательский интерфейс.
 */
public class Main {

    public static void main(String[] args) {
        Stock sber = new Stock("SBER", "Сбербанк", Currency.RUB,
                "Financials", new BigDecimal("6.5"));
        Bond ofz = new Bond("SU26238RMFS4", "ОФЗ-26238", Currency.RUB,
                7.1, 2035);
        Etf tmos = new Etf("TMOS", "Тинькофф iMOEX", Currency.RUB, "MOEX");

        // Полиморфизм подтипов: getDescription() вызывается разный для каждого
        // конкретного типа, хотя обращаемся мы к ним через общий тип Instrument.
        List<Instrument> instruments = List.of(sber, ofz, tmos);
        for (Instrument instrument : instruments) {
            System.out.println(instrument);
        }

        // Quote — агрегация: одна и та же акция может быть частью любого числа
        // котировок. getDividends() здесь корректен, т.к. цена уже известна.
        Quote sberQuote = new Quote(sber, new BigDecimal("278.50"), new BigDecimal("1.2"));
        System.out.println(sberQuote);
        System.out.println("Дивиденды по котировке: " + sberQuote.getDividends());

        // Bond и Etf дивидендов не платят — Quote.getDividends() честно
        // возвращает ZERO, не нарушая LSP (метод не объявлен в Instrument).
        Quote ofzQuote = new Quote(ofz, new BigDecimal("980.00"), new BigDecimal("-0.3"));
        System.out.println(ofzQuote);
        System.out.println("Дивиденды по котировке: " + ofzQuote.getDividends());

        // Portfolio — композиция: Position создаётся и живёт только внутри портфеля.
        Portfolio portfolio = new Portfolio("Пенсия Серёжи");
        portfolio.addPosition(sber, 10);
        portfolio.addPosition(ofz, 5);
        portfolio.addPosition(tmos, 3);

        System.out.println("Портфель: " + portfolio.getName());
        for (Portfolio.Position position : portfolio.getPositions()) {
            System.out.println("  " + position.getInstrument().getTicker()
                    + " x " + position.getQuantity());
        }
    }
}
