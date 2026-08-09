package academy.backend.market_pulse.cli;

/**
 * ASCII-прогресс-бар для долгих команд (см. «План семинара.md», семинар 10, этап 3). Отрисовывается
 * в {@code System.err}, чтобы не смешиваться с результатами команды в {@code System.out}. Метод
 * {@link #update(int)} вызывается из нескольких потоков-воркеров; собственного изменяемого состояния
 * у бара нет (только финальный {@code total}), а строка печатается одним вызовом — поэтому вывод не
 * искажается. Число уже выполненных считает вызывающая сторона атомарным счётчиком.
 */
public final class ProgressBar {

    private static final int WIDTH = 20;

    private final int total;

    public ProgressBar(int total) {
        this.total = Math.max(total, 1);
    }

    /** Перерисовывает полоску по числу завершённых задач. */
    public void update(int completed) {
        int filled = (int) ((long) completed * WIDTH / total);
        String bar = "#".repeat(filled) + "-".repeat(WIDTH - filled);
        System.err.print("\r[" + bar + "] " + completed + "/" + total);
        System.err.flush();
    }

    /** Завершает строку прогресса переводом каретки. */
    public void done() {
        System.err.println();
    }
}
