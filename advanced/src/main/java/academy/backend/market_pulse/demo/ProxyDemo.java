package academy.backend.market_pulse.demo;

import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.math.BigDecimal;

import academy.backend.market_pulse.model.Currency;
import academy.backend.market_pulse.model.Stock;
import academy.backend.market_pulse.repository.InMemoryInstrumentRepository;
import academy.backend.market_pulse.repository.InstrumentRepository;

/**
 * Демонстрация JDK Dynamic Proxy поверх {@link InstrumentRepository} — один из двух вариантов
 * этапа 5 «План семинара.md», семинар 2 (второй — {@link CglibProxyDemo}). Единственный
 * {@code InvocationHandler} перехватывает вызовы любого метода интерфейса без ручного
 * дублирования сигнатур.
 */
public class ProxyDemo {

    public static void main(String[] args) {
        InstrumentRepository target = new InMemoryInstrumentRepository();

        InstrumentRepository proxy = (InstrumentRepository) Proxy.newProxyInstance(
                InstrumentRepository.class.getClassLoader(),
                new Class<?>[]{InstrumentRepository.class},
                (Object p, Method method, Object[] methodArgs) -> {
                    long start = System.nanoTime();
                    Object result = method.invoke(target, methodArgs);
                    System.out.printf("%s() выполнен за %d нс%n", method.getName(), System.nanoTime() - start);
                    return result;
                });

        proxy.add(new Stock("SBER", "Сбербанк", Currency.RUB, "Financials", new BigDecimal("6.5")));
        proxy.findByTicker("SBER");
    }
}
