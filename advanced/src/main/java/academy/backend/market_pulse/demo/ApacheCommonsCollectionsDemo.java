package academy.backend.market_pulse.demo;

import java.util.List;
import java.util.Collection;

import org.apache.commons.collections4.Bag;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.bag.HashBag;

import academy.backend.market_pulse.model.Currency;

/**
 * Утилиты Apache Commons Collections, дополняющие {@code java.util.Collections} (см. «План
 * семинара.md», семинар 5, этап 6): теоретико-множественные операции {@link CollectionUtils} и
 * мультимножество {@link Bag}, считающее вхождения элементов. Не внедряется в проект — только
 * демонстрация.
 */
public class ApacheCommonsCollectionsDemo {

    public static void main(String[] args) {
        // Теоретико-множественная операция над двумя watchlist'ами без ручных циклов.
        List<String> watchlistA = List.of("SBER", "GAZP", "LKOH");
        List<String> watchlistB = List.of("GAZP", "LKOH", "YNDX");
        Collection<String> both = CollectionUtils.intersection(watchlistA, watchlistB);
        System.out.println("В обоих watchlist: " + both); // [GAZP, LKOH]

        // Bag: мультимножество, считает количество вхождений каждого элемента.
        Bag<Currency> currencyCounts = new HashBag<>();
        currencyCounts.add(Currency.RUB);
        currencyCounts.add(Currency.RUB);
        currencyCounts.add(Currency.USD);
        System.out.println("Инструментов в RUB: " + currencyCounts.getCount(Currency.RUB)); // 2
    }
}
