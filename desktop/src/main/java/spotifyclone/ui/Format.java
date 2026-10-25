package spotifyclone.ui;

import java.text.Normalizer;
import java.util.List;
import java.util.Locale;

import javafx.util.Duration;
import spotifyclone.model.Track;

public final class Format {

    private Format() {
    }

    public static boolean estConnue(Duration duree) {
        return duree != null && !duree.isUnknown() && !duree.isIndefinite();
    }

    /** Formate une durée en "m:ss", ou renvoie une chaîne vide si elle est inconnue. */
    public static String duree(Duration duree) {
        if (!estConnue(duree)) {
            return "";
        }
        int totalSecondes = (int) Math.max(0, Math.floor(duree.toSeconds()));
        return String.format("%d:%02d", totalSecondes / 60, totalSecondes % 60);
    }

    /** "3 titres · 8 min" */
    public static String resume(List<Track> tracks) {
        int n = tracks.size();
        double secondes = tracks.stream()
                .map(Track::getDuree)
                .filter(Format::estConnue)
                .mapToDouble(Duration::toSeconds)
                .sum();
        String texte = pluriel(n, "titre");
        if (secondes > 0) {
            texte += " · " + Math.max(1, Math.round(secondes / 60)) + " min";
        }
        return texte;
    }

    public static String pluriel(int n, String mot) {
        return n + " " + mot + (n > 1 ? "s" : "");
    }

    /** Minuscules sans accents, pour que "bebe" trouve "Bébé". */
    public static String normaliser(String texte) {
        if (texte == null) {
            return "";
        }
        return Normalizer.normalize(texte, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .toLowerCase(Locale.ROOT)
                .trim();
    }
}
