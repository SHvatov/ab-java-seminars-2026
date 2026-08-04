package academy.backend.market_pulse.cli;

import java.io.IOException;
import java.util.concurrent.Callable;

import academy.backend.market_pulse.model.Watchlist;
import academy.backend.market_pulse.storage.WatchlistStorage;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

@Command(name = "watch", description = "Отслеживать инструмент: добавить/убрать тикер в watchlist или показать список")
public class WatchCommand implements Callable<Integer> {

    @Parameters(index = "0", arity = "0..1", description = "Тикер инструмента (без аргумента — показать watchlist)")
    private String ticker;

    @Option(names = "--remove", description = "Убрать тикер из watchlist вместо добавления")
    private boolean remove;

    private final Watchlist watchlist;
    private final WatchlistStorage storage;

    public WatchCommand(Watchlist watchlist, WatchlistStorage storage) {
        this.watchlist = watchlist;
        this.storage = storage;
    }

    @Override
    public Integer call() {
        if (ticker != null) {
            if (remove) {
                boolean removed = watchlist.remove(ticker);
                System.out.println(removed
                        ? "Убрано из watchlist: " + ticker.toUpperCase()
                        : "Не отслеживается: " + ticker.toUpperCase());
            } else {
                boolean added = watchlist.add(ticker);
                System.out.println(added
                        ? "Добавлено в watchlist: " + ticker.toUpperCase()
                        : "Уже отслеживается: " + ticker.toUpperCase());
            }
            try {
                storage.save(watchlist.tickers());   // сохраняем watchlist на диск (семинар 7)
            } catch (IOException e) {
                System.out.println("Не удалось сохранить watchlist: " + e.getMessage());
            }
        }
        System.out.println("Watchlist: " + watchlist.tickers());
        return 0;
    }
}
