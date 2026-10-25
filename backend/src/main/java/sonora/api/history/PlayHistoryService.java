package sonora.api.history;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import sonora.api.config.SonoraProperties;

/**
 * Historique d'écoute, sauvegardé dans {data-dir}/plays.json.
 * Une seule personne pour l'instant : il n'y a pas encore de comptes utilisateurs.
 */
@Service
public class PlayHistoryService {

    private static final Logger log = LoggerFactory.getLogger(PlayHistoryService.class);

    public record Play(String trackId, int count, Instant lastPlayedAt) {
    }

    private final Path file;
    private final ObjectMapper mapper;
    // Du plus récemment écouté au plus ancien.
    private final LinkedHashMap<String, Play> plays = new LinkedHashMap<>();

    public PlayHistoryService(SonoraProperties properties, ObjectMapper mapper) {
        this.file = properties.dataPath().resolve("plays.json");
        this.mapper = mapper;
        load();
    }

    public synchronized List<Play> all() {
        return new ArrayList<>(plays.values());
    }

    public synchronized Play record(String trackId) {
        Play previous = plays.remove(trackId);
        Play play = new Play(trackId, previous == null ? 1 : previous.count() + 1, Instant.now());

        LinkedHashMap<String, Play> reordered = new LinkedHashMap<>();
        reordered.put(trackId, play);
        reordered.putAll(plays);
        plays.clear();
        plays.putAll(reordered);
        save();
        return play;
    }

    public synchronized void clear() {
        plays.clear();
        save();
    }

    private void load() {
        if (!Files.isRegularFile(file)) {
            return;
        }
        try {
            List<Play> saved = mapper.readValue(file.toFile(), new TypeReference<List<Play>>() { });
            saved.forEach(p -> plays.put(p.trackId(), p));
        } catch (IOException e) {
            log.warn("Historique illisible, on repart de zéro : {}", file, e);
        }
    }

    private void save() {
        try {
            Files.createDirectories(file.getParent());
            mapper.writerWithDefaultPrettyPrinter().writeValue(file.toFile(), new ArrayList<>(plays.values()));
        } catch (IOException e) {
            log.error("Impossible de sauvegarder l'historique dans {}", file, e);
        }
    }
}
