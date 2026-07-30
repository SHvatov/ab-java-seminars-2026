package academy.backend.market_pulse.demo;

import it.unimi.dsi.fastutil.doubles.DoubleArrayList;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;

import academy.backend.market_pulse.factory.InstrumentFactories;
import academy.backend.market_pulse.model.Currency;
import academy.backend.market_pulse.model.Instrument;

/**
 * Примитивные коллекции fastutil (см. «План семинара.md», семинар 5, этап 6). Стандартные
 * {@code List<Integer>}/{@code Map<Integer, V>} хранят обёртки {@code Integer} и на каждый
 * примитив делают boxing (отдельный объект в куче + разыменование при доступе). Коллекции fastutil
 * хранят примитивы напрямую в массиве {@code int[]}/{@code double[]}: меньше памяти, нет мусора от
 * boxing, дружелюбнее к кешу процессора — то же соображение о раскладке объектов, что показывал JOL
 * на семинаре 1. Не внедряется в проект — только демонстрация.
 */
public class FastUtilCollectionsDemo {

    public static void main(String[] args) {
        // История цен как double[] под капотом — без обёрток Double и boxing на каждый элемент.
        DoubleArrayList priceHistory = new DoubleArrayList();
        priceHistory.add(250.00);
        priceHistory.add(251.20);
        priceHistory.add(249.80);
        double last = priceHistory.getDouble(priceHistory.size() - 1); // примитив, без unboxing
        System.out.println("Последняя цена: " + last);

        // Индекс инструментов по числовому id: ключ хранится как int[], а не как Integer[].
        Int2ObjectMap<Instrument> byId = new Int2ObjectOpenHashMap<>();
        byId.put(1, InstrumentFactories.create("STOCK", "SBER", "Сбербанк", Currency.RUB));
        byId.put(2, InstrumentFactories.create("STOCK", "GAZP", "Газпром", Currency.RUB));
        Instrument found = byId.get(1); // ключ-примитив, без boxing на поиске
        System.out.println("Инструмент с id=1: " + found.getDescription());
    }
}
