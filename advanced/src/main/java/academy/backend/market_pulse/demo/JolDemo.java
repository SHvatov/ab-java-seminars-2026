package academy.backend.market_pulse.demo;

import java.math.BigDecimal;

import org.openjdk.jol.info.ClassLayout;

import academy.backend.market_pulse.model.Currency;
import academy.backend.market_pulse.model.Stock;

/**
 * Демонстрация для Этапа 6 семинара 1 (JOL): точные размеры объектов и
 * сравнение shallow size двух двумерных массивов разной формы.
 *
 * TODO: заменить на полноценный CLI (см. следующие семинары) — этот класс
 * не предназначен для использования как конечный пользовательский интерфейс.
 */
public class JolDemo {

    public static void main(String[] args) {
        // Размер заголовка и layout пустого объекта
        System.out.println(ClassLayout.parseClass(Object.class).toPrintable());

        // Layout нашего Stock
        Stock stock = new Stock("SBER", "Сбербанк", Currency.RUB,
                "Financials", new BigDecimal("6.5"));
        System.out.println(ClassLayout.parseInstance(stock).toPrintable());

        // Сравниваем размеры двух массивов
        int[][] small = new int[10][1000];
        int[][] large = new int[1000][10];

        System.out.println("int[10][1000] shallow size:  "
                + ClassLayout.parseInstance(small).instanceSize());
        System.out.println("int[1000][10] shallow size:  "
                + ClassLayout.parseInstance(large).instanceSize());

        // GraphLayout покажет полный граф — попробуйте сами!
        // TODO: студентам предлагается самостоятельно раскомментировать и
        // сравнить deep size обоих массивов через GraphLayout.parseInstance(...).toFootprint()
        // System.out.println(GraphLayout.parseInstance(small).toFootprint());
        // System.out.println(GraphLayout.parseInstance(large).toFootprint());
    }
}
