package sonora.api.library;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Comparator;
import java.util.HexFormat;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import sonora.api.config.SonoraProperties;

/** Scanne le dossier musique et garde la bibliothèque en mémoire. */
@Service
public class LibraryService {

    private static final Logger log = LoggerFactory.getLogger(LibraryService.class);

    private final Path musicDir;
    private volatile Map<String, Track> tracks = Map.of();
    // Pochettes brutes (tags ID3), traitées à la première demande puis mises en cache.
    private final Map<String, byte[]> rawCovers = new ConcurrentHashMap<>();
    private final Map<String, byte[]> covers = new ConcurrentHashMap<>();

    public LibraryService(SonoraProperties properties) {
        this.musicDir = properties.musicPath();
    }

    @PostConstruct
    public synchronized void rescan() {
        rawCovers.clear();
        covers.clear();
        if (!Files.isDirectory(musicDir)) {
            log.warn("Dossier musique introuvable : {}", musicDir);
            tracks = Map.of();
            return;
        }
        try (Stream<Path> files = Files.walk(musicDir)) {
            Map<String, Track> scanned = new LinkedHashMap<>();
            files.filter(Files::isRegularFile)
                    .filter(LibraryService::isAudio)
                    .sorted(Comparator.comparing(p -> p.getFileName().toString().toLowerCase(Locale.ROOT)))
                    .map(this::load)
                    .forEach(t -> scanned.put(t.id(), t));
            tracks = scanned;
            log.info("{} morceau(x) chargé(s) depuis {}", scanned.size(), musicDir);
        } catch (IOException e) {
            log.error("Impossible de scanner {}", musicDir, e);
            tracks = Map.of();
        }
    }

    public List<Track> all() {
        return List.copyOf(tracks.values());
    }

    public Optional<Track> find(String id) {
        return Optional.ofNullable(tracks.get(id));
    }

    /** Morceaux groupés par artiste, artistes triés par ordre alphabétique. */
    public Map<String, List<Track>> byArtist() {
        return tracks.values().stream().collect(Collectors.groupingBy(
                Track::artist, () -> new TreeMap<>(String.CASE_INSENSITIVE_ORDER), Collectors.toList()));
    }

    public Optional<byte[]> cover(String id) {
        byte[] raw = rawCovers.get(id);
        if (raw == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(covers.computeIfAbsent(id, k -> CoverProcessor.process(raw)));
    }

    public Path musicDir() {
        return musicDir;
    }

    private Track load(Path file) {
        String id = idFor(musicDir.relativize(file));
        TrackNameParser.ParsedName parsed = TrackNameParser.parse(file.getFileName().toString());
        AudioMetadata meta = AudioMetadata.read(file);

        // Le nom de fichier fait foi ; les tags ne servent que s'il ne donne pas l'artiste.
        String artist = parsed.artist();
        String title = parsed.title();
        if (!parsed.artistFound()) {
            if (meta.artist() != null) {
                artist = meta.artist();
            }
            if (meta.title() != null) {
                title = TrackNameParser.cleanTitle(meta.title());
            }
        }
        if (meta.artwork() != null) {
            rawCovers.put(id, meta.artwork());
        }
        return new Track(id, title, artist, meta.durationSeconds(), meta.artwork() != null, file);
    }

    private static boolean isAudio(Path p) {
        String name = p.getFileName().toString().toLowerCase(Locale.ROOT);
        return name.endsWith(".mp3") || name.endsWith(".wav");
    }

    /** Identifiant stable : début du SHA-1 du chemin relatif. */
    static String idFor(Path relative) {
        try {
            byte[] hash = MessageDigest.getInstance("SHA-1")
                    .digest(relative.toString().replace('\\', '/').getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash, 0, 6);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }
}
