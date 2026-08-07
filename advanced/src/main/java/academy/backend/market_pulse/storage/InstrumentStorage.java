package academy.backend.market_pulse.storage;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.databind.ObjectMapper;

import academy.backend.market_pulse.model.Bond;
import academy.backend.market_pulse.model.Etf;
import academy.backend.market_pulse.model.Instrument;
import academy.backend.market_pulse.model.Stock;

/**
 * Файловое хранилище каталога инструментов: каждый инструмент — отдельный JSON-файл
 * {@code <dataDir>/instruments/<TICKER>.json}. Чтение и запись — через NIO ({@link Files}/{@link Path});
 * сериализация — Jackson (см. «План семинара.md», семинар 7, этап 2).
 *
 * <p>{@link ObjectMapper} настроен на работу по полям (а не по геттерам): доменная модель хранит
 * данные в приватных финальных полях без сеттеров, а часть геттеров вычисляемые
 * ({@code getDescription}, {@code getDividends}) — их в JSON писать не нужно.
 */
public class InstrumentStorage {

    private final Path instrumentsDir;
    private final ObjectMapper mapper;

    public InstrumentStorage(Path dataDir) {
        this.instrumentsDir = dataDir.resolve("instruments");
        this.mapper = new ObjectMapper();
        mapper.setVisibility(mapper.getVisibilityChecker()
                .withFieldVisibility(JsonAutoDetect.Visibility.ANY)
                .withGetterVisibility(JsonAutoDetect.Visibility.NONE)
                .withIsGetterVisibility(JsonAutoDetect.Visibility.NONE)
                .withSetterVisibility(JsonAutoDetect.Visibility.NONE));
        // Знание о JSON-сериализации домена вынесено в mixin'ы (Clean Architecture, семинар 9):
        // домен свободен от аннотаций Jackson, привязка — снаружи, здесь.
        mapper.addMixIn(Instrument.class, InstrumentJsonMixin.class);
        mapper.addMixIn(Stock.class, StockJsonMixin.class);
        mapper.addMixIn(Bond.class, BondJsonMixin.class);
        mapper.addMixIn(Etf.class, EtfJsonMixin.class);
    }

    public void save(Instrument instrument) throws IOException {
        Files.createDirectories(instrumentsDir);
        Path file = instrumentsDir.resolve(instrument.getTicker().toUpperCase() + ".json");
        byte[] json = mapper.writerWithDefaultPrettyPrinter().writeValueAsBytes(instrument);
        Files.write(file, json);   // NIO: запись массива байтов одной операцией
    }

    public List<Instrument> loadAll() throws IOException {
        if (Files.notExists(instrumentsDir)) {
            return List.of();
        }
        try (Stream<Path> files = Files.list(instrumentsDir)) {   // стрим держит дескриптор — закрываем
            return files
                    .filter(path -> path.toString().endsWith(".json"))
                    .map(this::read)
                    .filter(Objects::nonNull)
                    .toList();
        }
    }

    /**
     * Читает один файл инструмента. Повреждённый или чужой JSON не роняет загрузку каталога — файл
     * пропускается с предупреждением (см. «План семинара.md», семинар 4 — падать красиво).
     */
    private Instrument read(Path file) {
        try {
            return mapper.readValue(Files.readAllBytes(file), Instrument.class);
        } catch (IOException e) {
            System.err.println("Пропущен повреждённый файл инструмента " + file + ": " + e.getMessage());
            return null;
        }
    }
}
