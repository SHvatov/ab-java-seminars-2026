package academy.backend.market_pulse.exception;

/**
 * Инструмент с таким тикером уже есть в репозитории (см. «План семинара.md», семинар 4, этап 3).
 */
public class DuplicateTickerException extends RuntimeException {

    private final String ticker;

    public DuplicateTickerException(String ticker) {
        super("Инструмент с тикером уже существует: " + ticker);
        this.ticker = ticker;
    }

    public String getTicker() {
        return ticker;
    }
}
