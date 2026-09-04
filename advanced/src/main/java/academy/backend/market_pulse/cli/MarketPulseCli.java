package academy.backend.market_pulse.cli;

import picocli.CommandLine.Command;

@Command(name = "market-pulse")
public class MarketPulseCli implements Runnable {

    @Override
    public void run() {
        System.out.println("Используйте --help для списка команд");
    }
}
