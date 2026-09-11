package academy.backend.market_pulse.demo;

import java.math.BigDecimal;
import java.util.Iterator;

import academy.backend.market_pulse.model.Currency;
import academy.backend.market_pulse.model.Instrument;
import academy.backend.market_pulse.model.Stock;
import academy.backend.market_pulse.repository.InMemoryInstrumentRepository;
import academy.backend.market_pulse.repository.InstrumentRepository;

/**
 * Демонстрация самописного (статического) прокси поверх {@link InstrumentRepository} — третий,
 * наименее автоматизированный вариант: каждый метод интерфейса оборачивается вручную, а
 * дублирование «замерить → залогировать» просто переезжает сюда из целевого класса. Сравнить с
 * {@link ProxyDemo} (JDK Dynamic Proxy) и {@link CglibProxyDemo} (CGLIB) — оба избавляют от
 * необходимости дописывать новый метод-обёртку вручную при каждом новом методе интерфейса.
 */
public class StaticProxyDemo {

    private static final class TimingInstrumentRepository implements InstrumentRepository {

        private final InstrumentRepository target;

        private TimingInstrumentRepository(InstrumentRepository target) {
            this.target = target;
        }

        @Override
        public void add(Instrument instrument) {
            long start = System.nanoTime();
            target.add(instrument);
            System.out.printf("add() выполнен за %d нс%n", System.nanoTime() - start);
        }

        @Override
        public Instrument findByTicker(String ticker) {
            long start = System.nanoTime();
            Instrument result = target.findByTicker(ticker);
            System.out.printf("findByTicker() выполнен за %d нс%n", System.nanoTime() - start);
            return result;
        }

        @Override
        public Iterator<Instrument> iterator() {
            long start = System.nanoTime();
            Iterator<Instrument> result = target.iterator();
            System.out.printf("iterator() выполнен за %d нс%n", System.nanoTime() - start);
            return result;
        }
    }

    public static void main(String[] args) {
        InstrumentRepository proxy = new TimingInstrumentRepository(new InMemoryInstrumentRepository());

        proxy.add(new Stock("SBER", "Сбербанк", Currency.RUB, "Financials", new BigDecimal("6.5")));
        proxy.findByTicker("SBER");
    }
}
