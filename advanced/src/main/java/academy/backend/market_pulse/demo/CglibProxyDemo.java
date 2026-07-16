package academy.backend.market_pulse.demo;

import java.math.BigDecimal;

import academy.backend.market_pulse.model.Currency;
import academy.backend.market_pulse.model.Stock;
import academy.backend.market_pulse.repository.InMemoryInstrumentRepository;
import academy.backend.market_pulse.repository.InstrumentRepository;
import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.MethodInterceptor;

/**
 * Демонстрация CGLIB поверх {@link InstrumentRepository} — второй из двух вариантов этапа 5
 * «План семинара.md», семинар 2 (первый — {@link ProxyDemo}). Генерирует подкласс целевого
 * класса во время выполнения — в отличие от JDK Dynamic Proxy, интерфейс не обязателен.
 */
public class CglibProxyDemo {

    public static void main(String[] args) {
        Enhancer enhancer = new Enhancer();
        enhancer.setSuperclass(InMemoryInstrumentRepository.class);
        enhancer.setCallback((MethodInterceptor) (obj, method, methodArgs, methodProxy) -> {
            long start = System.nanoTime();
            Object result = methodProxy.invokeSuper(obj, methodArgs);
            System.out.printf("%s() выполнен за %d нс%n", method.getName(), System.nanoTime() - start);
            return result;
        });

        InstrumentRepository repository = (InstrumentRepository) enhancer.create();
        repository.add(new Stock("SBER", "Сбербанк", Currency.RUB, "Financials", new BigDecimal("6.5")));
        repository.findByTicker("SBER");
    }
}
