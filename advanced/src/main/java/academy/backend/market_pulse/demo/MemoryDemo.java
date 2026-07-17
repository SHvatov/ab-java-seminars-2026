package academy.backend.market_pulse.demo;

import java.util.Scanner;

/**
 * Демонстрация для Этапа 6 семинара 1 (VisualVM): создаёт два двумерных
 * массива с одинаковым числом элементов, но разной формой, и ждёт нажатия
 * Enter между шагами, чтобы рост Heap было видно в мониторинге независимо от
 * скорости машины и настроек профилировщика конкретного слушателя.
 * <p>
 * TODO: заменить на полноценный CLI (см. следующие семинары) — этот класс
 * не предназначен для использования как конечный пользовательский интерфейс.
 */
public class MemoryDemo {

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        System.out.println("Allocating int[10][1000]...");
        int[][] small = new int[10][1000];

        System.out.println("Посмотрите на Heap в VisualVM, затем нажмите Enter...");
        scanner.nextLine();

        System.out.println("Allocating int[1000][10]...");
        int[][] large = new int[1000][10];

        System.out.println("Посмотрите на Heap в VisualVM ещё раз, затем нажмите Enter...");
        scanner.nextLine();

        System.out.println("Done. Press Enter to exit.");
        scanner.nextLine();

        // Держим ссылки, чтобы GC не собрал
        System.out.println(small.length + " " + large.length);
    }
}
