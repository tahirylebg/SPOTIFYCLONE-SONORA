package sonora.api.library;

import java.nio.file.Path;

/**
 * Un morceau de la bibliothèque.
 *
 * @param id              identifiant stable, dérivé du chemin relatif du fichier
 * @param durationSeconds 0 si la durée n'a pas pu être lue
 */
public record Track(String id, String title, String artist, int durationSeconds, boolean hasCover, Path file) {
}
