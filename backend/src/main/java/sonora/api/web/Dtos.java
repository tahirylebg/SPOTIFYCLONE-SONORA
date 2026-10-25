package sonora.api.web;

import java.time.Instant;
import java.util.List;

import sonora.api.history.PlayHistoryService.Play;
import sonora.api.library.Track;

/** Objets renvoyés par l'API. Les URLs sont relatives à la racine du serveur. */
public final class Dtos {

    private Dtos() {
    }

    public record TrackDto(String id, String title, String artist, int durationSeconds,
                           String coverUrl, String audioUrl) {

        public static TrackDto of(Track t) {
            return new TrackDto(t.id(), t.title(), t.artist(), t.durationSeconds(),
                    t.hasCover() ? "/api/tracks/" + t.id() + "/cover" : null,
                    "/api/tracks/" + t.id() + "/audio");
        }
    }

    public record ArtistDto(String name, int trackCount, List<String> trackIds, String coverUrl) {

        public static ArtistDto of(String name, List<Track> tracks) {
            String cover = tracks.stream()
                    .filter(Track::hasCover)
                    .findFirst()
                    .map(t -> "/api/tracks/" + t.id() + "/cover")
                    .orElse(null);
            return new ArtistDto(name, tracks.size(), tracks.stream().map(Track::id).toList(), cover);
        }
    }

    public record SearchResultDto(List<TrackDto> tracks, List<ArtistDto> artists) {
    }

    public record PlayDto(String trackId, int count, Instant lastPlayedAt) {

        public static PlayDto of(Play p) {
            return new PlayDto(p.trackId(), p.count(), p.lastPlayedAt());
        }
    }

    public record RescanDto(int trackCount) {
    }
}
