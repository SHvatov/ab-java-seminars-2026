package academy.backend.market_pulse.demo;

import java.math.BigDecimal;

import academy.backend.market_pulse.model.Currency;
import academy.backend.market_pulse.model.Stock;
import academy.backend.market_pulse.repository.InstrumentRepository;

/**
 * Демонстрация CGLIB поверх {@link InstrumentRepository} — второй из двух автоматизированных
 * вариантов проксирования (первый — {@link ProxyDemo}). Генерирует подкласс целевого класса во
 * время выполнения — в отличие от JDK Dynamic Proxy, интерфейс не обязателен. Само создание
 * прокси скрыто за {@link ProxyFactory}.
 */
public class CglibProxyDemo {

    public static void main(String[] args) {
        InstrumentRepository repository = ProxyFactory.timingRepository(ProxyFactory.Kind.CGLIB);

        repository.add(new Stock("SBER", "Сбербанк", Currency.RUB, "Financials", new BigDecimal("6.5")));
        repository.findByTicker("SBER");
    }
}
