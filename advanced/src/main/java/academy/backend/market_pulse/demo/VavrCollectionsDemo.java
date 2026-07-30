package academy.backend.market_pulse.demo;

import io.vavr.collection.List;
import io.vavr.collection.Map;
import io.vavr.collection.HashMap;

/**
 * Персистентные (иммутабельные) коллекции Vavr (см. «План семинара.md», семинар 5, этап 6). В
 * отличие от {@code Collections.unmodifiableList} — обёртки над изменяемым оригиналом — коллекции
 * {@code io.vavr.collection.*} неизменяемы по-настоящему: любая «модификация» ({@code append},
 * {@code put}) возвращает новый экземпляр, а исходный остаётся прежним (structural sharing — новая
 * версия переиспользует внутренние узлы старой, поэтому копирование дешёвое). Не внедряется в
 * проект — только демонстрация.
 */
public class VavrCollectionsDemo {

    public static void main(String[] args) {
        // Персистентный список: append не меняет исходный, а возвращает новую версию.
        List<String> watchlist = List.of("SBER", "GAZP");
        List<String> extended = watchlist.append("LKOH");
        System.out.println("Исходный watchlist: " + watchlist);   // List(SBER, GAZP) — не изменился
        System.out.println("Расширенный:        " + extended);    // List(SBER, GAZP, LKOH)

        // Персистентное отображение: put возвращает новый Map, старый неизменен.
        Map<String, Integer> weights = HashMap.of("SBER", 40);
        Map<String, Integer> updated = weights.put("GAZP", 60);
        System.out.println("Исходный map: " + weights);           // HashMap((SBER, 40))
        System.out.println("Обновлённый:  " + updated);           // HashMap((SBER, 40), (GAZP, 60))

        // Операции возвращают новую коллекцию, а не мутируют текущую — стиль без побочных эффектов.
        List<String> upper = watchlist.map(String::toLowerCase);
        System.out.println("Преобразованный: " + upper);
    }
}
