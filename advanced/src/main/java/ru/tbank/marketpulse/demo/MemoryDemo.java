package ru.tbank.marketpulse.demo;

import java.io.IOException;

/**
 * Демонстрация для Этапа 6 семинара 1 (VisualVM): создаёт два двумерных
 * массива с одинаковым числом элементов, но разной формой, и держит паузы,
 * чтобы рост Heap было видно в мониторинге.
 *
 * TODO: заменить на полноценный CLI (см. следующие семинары) — этот класс
 * не предназначен для использования как конечный пользовательский интерфейс.
 */
public class MemoryDemo {

    public static void main(String[] args) throws InterruptedException, IOException {
        System.out.println("Allocating int[10][1000]...");
        int[][] small = new int[10][1000];

        Thread.sleep(10_000);  // пауза — смотрим в VisualVM

        System.out.println("Allocating int[1000][10]...");
        int[][] large = new int[1000][10];

        Thread.sleep(10_000);  // пауза — смотрим снова

        System.out.println("Done. Press Enter to exit.");
        System.in.read();

        // Держим ссылки, чтобы GC не собрал
        System.out.println(small.length + " " + large.length);
    }
}
