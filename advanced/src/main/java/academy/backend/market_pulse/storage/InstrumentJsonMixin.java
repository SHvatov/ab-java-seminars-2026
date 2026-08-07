package academy.backend.market_pulse.storage;

import java.math.BigDecimal;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import academy.backend.market_pulse.model.Bond;
import academy.backend.market_pulse.model.Currency;
import academy.backend.market_pulse.model.Etf;
import academy.backend.market_pulse.model.Stock;

/**
 * Jackson-mixin'ы для доменной модели (см. «План семинара.md», семинар 9, этап 3 — Clean Architecture).
 * Знание о сериализации (полиморфизм по полю {@code type} и конструкторы для десериализации) вынесено
 * из доменных классов сюда, в слой хранения. Домен ({@code model}) снова свободен от зависимости на
 * Jackson; mixin'ы привязываются к нему снаружи через {@code ObjectMapper.addMixIn} в
 * {@link InstrumentStorage}. Классы package-private — деталь слоя storage.
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
@JsonSubTypes({
        @JsonSubTypes.Type(value = Stock.class, name = "STOCK"),
        @JsonSubTypes.Type(value = Bond.class, name = "BOND"),
        @JsonSubTypes.Type(value = Etf.class, name = "ETF")
})
abstract class InstrumentJsonMixin {
}

abstract class StockJsonMixin {
    @JsonCreator
    StockJsonMixin(@JsonProperty("ticker") String ticker,
                   @JsonProperty("name") String name,
                   @JsonProperty("currency") Currency currency,
                   @JsonProperty("sector") String sector,
                   @JsonProperty("dividendYield") BigDecimal dividendYield) {
    }
}

abstract class BondJsonMixin {
    @JsonCreator
    BondJsonMixin(@JsonProperty("ticker") String ticker,
                  @JsonProperty("name") String name,
                  @JsonProperty("currency") Currency currency,
                  @JsonProperty("couponRate") BigDecimal couponRate,
                  @JsonProperty("maturityYear") int maturityYear) {
    }
}

abstract class EtfJsonMixin {
    @JsonCreator
    EtfJsonMixin(@JsonProperty("ticker") String ticker,
                 @JsonProperty("name") String name,
                 @JsonProperty("currency") Currency currency,
                 @JsonProperty("trackingIndex") String trackingIndex) {
    }
}
