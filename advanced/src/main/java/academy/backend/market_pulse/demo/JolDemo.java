package academy.backend.market_pulse.demo;

import java.math.BigDecimal;

import org.openjdk.jol.info.ClassLayout;
import org.openjdk.jol.info.GraphLayout;

import academy.backend.market_pulse.model.Currency;
import academy.backend.market_pulse.model.Stock;

/**
 * Демонстрация для практики с JOL (семинар 1): точные размеры объектов и
 * сравнение shallow/deep size двух двумерных массивов разной формы.
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

        // GraphLayout — полный граф ссылок, deep size с учётом внутренних массивов
        System.out.println(GraphLayout.parseInstance(small).toFootprint());
        System.out.println(GraphLayout.parseInstance(large).toFootprint());
    }
}
