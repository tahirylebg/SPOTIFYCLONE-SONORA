package sonora.api.library;

import java.util.Locale;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Déduit artiste et titre d'un nom de fichier "Artiste - Titre.mp3",
 * en retirant le bruit typique des fichiers YouTube.
 */
public final class TrackNameParser {

    public static final String UNKNOWN_ARTIST = "Artiste inconnu";

    private static final Locale FR = Locale.FRENCH;

    public record ParsedName(String artist, String title, boolean artistFound) {
    }

    private TrackNameParser() {
    }

    public static ParsedName parse(String fileName) {
        int point = fileName.lastIndexOf('.');
        String name = point > 0 ? fileName.substring(0, point) : fileName;

        int separator = name.indexOf(" - ");
        if (separator > 0) {
            return new ParsedName(name.substring(0, separator).trim(), cleanTitle(name.substring(separator + 3)), true);
        }
        return new ParsedName(UNKNOWN_ARTIST, cleanTitle(name), false);
    }

    /** Retire "(Visualizer)", "CLIP OFFICIEL", les années… et remet en casse normale un titre tout en majuscules. */
    public static String cleanTitle(String title) {
        String clean = title
                .replaceAll("[(\\[][^)\\]]*[)\\]]", " ")
                .replaceAll("(?i)\\b(clip officiel|clip|officiel|official (music )?video|official audio|lyrics?|a colors show|colors show|visualizer|audio)\\b", " ")
                .replaceAll("\\b(19|20)\\d{2}\\b", " ")
                .replaceAll("\\s+", " ")
                .trim();
        if (clean.isEmpty()) {
            clean = title.trim();
        }
        return clean.equals(clean.toUpperCase(FR)) ? capitalize(clean) : clean;
    }

    private static String capitalize(String text) {
        return Stream.of(text.toLowerCase(FR).split(" "))
                .map(word -> word.isEmpty() ? word : word.substring(0, 1).toUpperCase(FR) + word.substring(1))
                .collect(Collectors.joining(" "));
    }
}
