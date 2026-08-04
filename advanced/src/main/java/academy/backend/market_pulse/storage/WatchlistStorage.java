package academy.backend.market_pulse.storage;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashSet;
import java.util.Set;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Файловое хранилище watchlist: набор тикеров в одном JSON-файле {@code <dataDir>/watchlist.json}.
 * Чтение и запись — через NIO (см. «План семинара.md», семинар 7, этап 2). Watchlist — это
 * {@code Set<String>}, поэтому сериализация проще, чем у полиморфного каталога инструментов.
 */
public class WatchlistStorage {

    private final Path dataDir;
    private final Path watchlistFile;
    private final ObjectMapper mapper = new ObjectMapper();

    public WatchlistStorage(Path dataDir) {
        this.dataDir = dataDir;
        this.watchlistFile = dataDir.resolve("watchlist.json");
    }

    public void save(Set<String> tickers) throws IOException {
        Files.createDirectories(dataDir);
        byte[] json = mapper.writerWithDefaultPrettyPrinter().writeValueAsBytes(tickers);
        Files.write(watchlistFile, json);
    }

    public Set<String> load() throws IOException {
        if (Files.notExists(watchlistFile)) {
            return new LinkedHashSet<>();
        }
        return mapper.readValue(Files.readAllBytes(watchlistFile),
                new TypeReference<LinkedHashSet<String>>() {
                });
    }
}
