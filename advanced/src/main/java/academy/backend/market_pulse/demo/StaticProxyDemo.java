package academy.backend.market_pulse.demo;

import java.math.BigDecimal;

import academy.backend.market_pulse.model.Currency;
import academy.backend.market_pulse.model.Stock;
import academy.backend.market_pulse.repository.InstrumentRepository;

/**
 * Демонстрация самописного (статического) прокси поверх {@link InstrumentRepository} — третий,
 * наименее автоматизированный вариант: каждый метод интерфейса оборачивается вручную, а
 * дублирование «замерить → залогировать» просто переезжает в новый класс. Сравнить с
 * {@link ProxyDemo} (JDK Dynamic Proxy) и {@link CglibProxyDemo} (CGLIB) — оба избавляют от
 * необходимости дописывать новый метод-обёртку вручную при каждом новом методе интерфейса.
 * Само создание прокси скрыто за {@link ProxyFactory} — этому классу (клиенту) не нужно знать,
 * как обёртка устроена изнутри.
 */
public class StaticProxyDemo {

    public static void main(String[] args) {
        InstrumentRepository proxy = ProxyFactory.timingRepository(ProxyFactory.Kind.STATIC);

        proxy.add(new Stock("SBER", "Сбербанк", Currency.RUB, "Financials", new BigDecimal("6.5")));
        proxy.findByTicker("SBER");
    }
}
