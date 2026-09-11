package academy.backend.market_pulse.demo;

import java.math.BigDecimal;

import academy.backend.market_pulse.model.Currency;
import academy.backend.market_pulse.model.Stock;
import academy.backend.market_pulse.repository.InstrumentRepository;

/**
 * Демонстрация JDK Dynamic Proxy поверх {@link InstrumentRepository} — один из двух
 * автоматизированных вариантов проксирования (второй — {@link CglibProxyDemo}). Единственный
 * {@code InvocationHandler} перехватывает вызовы любого метода интерфейса без ручного
 * дублирования сигнатур. Само создание прокси скрыто за {@link ProxyFactory}.
 */
public class ProxyDemo {

    public static void main(String[] args) {
        InstrumentRepository proxy = ProxyFactory.timingRepository(ProxyFactory.Kind.JDK_DYNAMIC);

        proxy.add(new Stock("SBER", "Сбербанк", Currency.RUB, "Financials", new BigDecimal("6.5")));
        proxy.findByTicker("SBER");
    }
}
