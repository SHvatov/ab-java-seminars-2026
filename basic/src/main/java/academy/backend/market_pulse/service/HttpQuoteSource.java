package academy.backend.market_pulse.service;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import academy.backend.market_pulse.model.Instrument;
import academy.backend.market_pulse.model.Quote;
import academy.backend.market_pulse.repository.InstrumentRepository;

/**
 * Источник котировок поверх реального T-Invest API (см. «План семинара.md», семинар 7, этап 5).
 * Ходит в REST-gateway через {@link HttpClient} с аутентификацией по токену; ответ разбирается
 * Jackson. Инструмент для {@link Quote} берётся из каталога ({@link InstrumentRepository}); последняя
 * цена — из API, изменение считается относительно цены закрытия предыдущего дня.
 *
 * <p><b>Контракт API помечен комментариями «// API:» и подлежит сверке с документацией</b>
 * (developer.tbank.ru/invest): точные пути методов, поля запроса и структура ответа могут отличаться.
 */
public class HttpQuoteSource implements QuoteSource {

    // API: базовый адрес REST-gateway
    private static final String BASE = "https://invest-public-api.tinkoff.ru/rest/";
    // API: поиск инструмента (тикер -> идентификатор)
    private static final String FIND_INSTRUMENT =
            "tinkoff.public.invest.api.contract.v1.InstrumentsService/FindInstrument";
    // API: последние цены по идентификаторам
    private static final String GET_LAST_PRICES =
            "tinkoff.public.invest.api.contract.v1.MarketDataService/GetLastPrices";
    // API: цены закрытия предыдущего дня по идентификаторам
    private static final String GET_CLOSE_PRICES =
            "tinkoff.public.invest.api.contract.v1.MarketDataService/GetClosePrices";

    private final String token;
    private final InstrumentRepository repository;
    private final HttpClient httpClient = HttpClient.newHttpClient();
    private final ObjectMapper mapper = new ObjectMapper();
    private final Map<String, String> figiCache = new HashMap<>();   // тикер -> figi

    public HttpQuoteSource(String token, InstrumentRepository repository) {
        this.token = token;
        this.repository = repository;
    }

    @Override
    public Optional<Quote> fetch(String ticker) {
        if (token == null || token.isBlank()) {
            return Optional.empty();
        }
        Optional<Instrument> instrument = repository.findByTicker(ticker);
        if (instrument.isEmpty()) {
            return Optional.empty();   // котировку строим только для известного инструмента
        }
        try {
            String figi = resolveFigi(ticker);
            if (figi == null) {
                return Optional.empty();
            }
            BigDecimal last = fetchPrice(GET_LAST_PRICES, "lastPrices", figi);
            if (last == null) {
                return Optional.empty();
            }
            BigDecimal close = fetchPrice(GET_CLOSE_PRICES, "closePrices", figi);
            return Optional.of(new Quote(instrument.get(), last, changePercent(last, close)));
        } catch (IOException e) {
            return Optional.empty();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();   // восстанавливаем флаг прерывания
            return Optional.empty();
        }
    }

    private String resolveFigi(String ticker) throws IOException, InterruptedException {
        String cached = figiCache.get(ticker.toUpperCase());
        if (cached != null) {
            return cached;
        }
        // API: тело запроса поиска инструмента
        JsonNode response = post(FIND_INSTRUMENT, "{\"query\":\"" + ticker + "\"}");
        if (response == null) {
            return null;
        }
        // API: ответ — { "instruments": [ { "figi": "...", "ticker": "..." }, ... ] }
        for (JsonNode node : response.path("instruments")) {
            if (ticker.equalsIgnoreCase(node.path("ticker").asText())) {
                String figi = node.path("figi").asText();
                figiCache.put(ticker.toUpperCase(), figi);
                return figi;
            }
        }
        return null;
    }

    /**
     * Запрашивает цену по идентификатору у метода {@code method} и достаёт её из массива
     * {@code arrayField} ответа. {@code GetLastPrices} и {@code GetClosePrices} имеют одинаковую
     * форму ответа, отличаясь только именем массива.
     */
    private BigDecimal fetchPrice(String method, String arrayField, String figi)
            throws IOException, InterruptedException {
        // API: тело запроса цен
        JsonNode response = post(method, "{\"instrumentId\":[\"" + figi + "\"]}");
        if (response == null) {
            return null;
        }
        // API: ответ — { "<arrayField>": [ { "price": { "units": "250", "nano": 500000000 } } ] }
        JsonNode prices = response.path(arrayField);
        if (prices.isEmpty()) {
            return null;
        }
        return quotationToBigDecimal(prices.get(0).path("price"));
    }

    /**
     * Изменение цены в процентах относительно закрытия: {@code (last - close) / close * 100}. Если
     * цена закрытия недоступна или нулевая — возвращает ноль.
     */
    private BigDecimal changePercent(BigDecimal last, BigDecimal close) {
        if (close == null || close.signum() == 0) {
            return BigDecimal.ZERO;
        }
        return last.subtract(close)
                .divide(close, 6, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100));
    }

    /**
     * Отправляет POST-запрос к методу API и возвращает разобранное тело ответа или {@code null},
     * если статус не 200.
     */
    private JsonNode post(String method, String body) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE + method))
                .header("Authorization", "Bearer " + token)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build();
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() != 200) {
            return null;
        }
        return mapper.readTree(response.body());
    }

    /**
     * Преобразует денежную величину T-Invest (Quotation: целая часть {@code units} + дробная в
     * миллиардных долях {@code nano}) в {@link BigDecimal} без потери точности.
     */
    private BigDecimal quotationToBigDecimal(JsonNode quotation) {
        long units = quotation.path("units").asLong();
        int nano = quotation.path("nano").asInt();
        return BigDecimal.valueOf(units).add(BigDecimal.valueOf(nano, 9));
    }
}
