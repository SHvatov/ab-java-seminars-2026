package academy.backend.market_pulse.service;

import java.util.Optional;

import academy.backend.market_pulse.model.Quote;

/**
 * Источник рыночных котировок. Заглушка семинаров 5–6 заменена реализацией поверх реального
 * T-Invest API (см. «План семинара.md», семинар 7, этап 5) — {@link HttpQuoteSource}. Вынесен в
 * интерфейс, чтобы вызывающий код ({@link QuoteService}) не зависел от способа получения котировки.
 */
public interface QuoteSource {

    /**
     * Возвращает котировку по тикеру или {@link Optional#empty()}, если инструмент не найден либо
     * запрос не удался.
     */
    Optional<Quote> fetch(String ticker);
}
