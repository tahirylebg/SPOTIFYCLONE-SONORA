package sonora.api.web;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import sonora.api.history.PlayHistoryService;
import sonora.api.library.LibraryService;
import sonora.api.web.Dtos.ArtistDto;
import sonora.api.web.Dtos.PlayDto;
import sonora.api.web.Dtos.RescanDto;
import sonora.api.web.Dtos.SearchResultDto;
import sonora.api.web.Dtos.TrackDto;

/** Artistes, recherche, historique d'écoute et rescan de la bibliothèque. */
@RestController
@RequestMapping("/api")
public class LibraryController {

    private final LibraryService library;
    private final PlayHistoryService history;

    public LibraryController(LibraryService library, PlayHistoryService history) {
        this.library = library;
        this.history = history;
    }

    @GetMapping("/artists")
    public List<ArtistDto> artists() {
        return library.byArtist().entrySet().stream()
                .map(e -> ArtistDto.of(e.getKey(), e.getValue()))
                .toList();
    }

    /** Recherche insensible à la casse et aux accents dans les titres et les artistes. */
    @GetMapping("/search")
    public SearchResultDto search(@RequestParam(defaultValue = "") String q) {
        String query = Text.normalize(q);
        if (query.isEmpty()) {
            return new SearchResultDto(List.of(), List.of());
        }
        List<TrackDto> tracks = library.all().stream()
                .filter(t -> Text.normalize(t.title()).contains(query) || Text.normalize(t.artist()).contains(query))
                .map(TrackDto::of)
                .toList();
        List<ArtistDto> artists = library.byArtist().entrySet().stream()
                .filter(e -> Text.normalize(e.getKey()).contains(query))
                .map(e -> ArtistDto.of(e.getKey(), e.getValue()))
                .toList();
        return new SearchResultDto(tracks, artists);
    }

    /** Historique, du plus récemment écouté au plus ancien (morceaux encore présents uniquement). */
    @GetMapping("/plays")
    public List<PlayDto> plays() {
        return history.all().stream()
                .filter(p -> library.find(p.trackId()).isPresent())
                .map(PlayDto::of)
                .toList();
    }

    @PostMapping("/plays/{trackId}")
    @ResponseStatus(HttpStatus.CREATED)
    public PlayDto recordPlay(@PathVariable String trackId) {
        if (library.find(trackId).isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Morceau inconnu : " + trackId);
        }
        return PlayDto.of(history.record(trackId));
    }

    @DeleteMapping("/plays")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void clearPlays() {
        history.clear();
    }

    @PostMapping("/library/rescan")
    public RescanDto rescan() {
        library.rescan();
        return new RescanDto(library.all().size());
    }
}
