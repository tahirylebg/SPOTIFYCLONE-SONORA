package sonora.api.web;

import java.time.Duration;
import java.util.List;
import java.util.Locale;

import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import sonora.api.library.LibraryService;
import sonora.api.library.Track;
import sonora.api.web.Dtos.TrackDto;

@RestController
@RequestMapping("/api/tracks")
public class TrackController {

    private final LibraryService library;

    public TrackController(LibraryService library) {
        this.library = library;
    }

    @GetMapping
    public List<TrackDto> list() {
        return library.all().stream().map(TrackDto::of).toList();
    }

    @GetMapping("/{id}")
    public TrackDto get(@PathVariable String id) {
        return TrackDto.of(track(id));
    }

    /**
     * Fichier audio. Spring gère l'en-tête Range (réponses 206) pour une Resource,
     * ce qui permet aux lecteurs web et mobiles d'avancer dans le morceau.
     */
    @GetMapping("/{id}/audio")
    public ResponseEntity<Resource> audio(@PathVariable String id) {
        Track track = track(id);
        String name = track.file().getFileName().toString().toLowerCase(Locale.ROOT);
        MediaType type = name.endsWith(".wav") ? MediaType.parseMediaType("audio/wav") : MediaType.parseMediaType("audio/mpeg");
        return ResponseEntity.ok()
                .contentType(type)
                .header("Accept-Ranges", "bytes")
                .body(new FileSystemResource(track.file()));
    }

    @GetMapping("/{id}/cover")
    public ResponseEntity<byte[]> cover(@PathVariable String id) {
        track(id);
        return library.cover(id)
                .map(bytes -> ResponseEntity.ok()
                        .contentType(MediaType.IMAGE_JPEG)
                        .cacheControl(CacheControl.maxAge(Duration.ofDays(1)))
                        .body(bytes))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Pas de pochette"));
    }

    private Track track(String id) {
        return library.find(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Morceau inconnu : " + id));
    }
}
