package spotifyclone.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayDeque;
import java.util.Collections;
import java.util.Comparator;
import java.util.Deque;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javafx.scene.image.Image;
import javafx.scene.media.Media;
import javafx.scene.media.MediaException;
import javafx.scene.media.MediaPlayer;
import javafx.util.Duration;
import spotifyclone.model.Track;

public class LibraryService {

    public static final String ARTISTE_INCONNU = "Artiste inconnu";

    // Selon le dossier de lancement (spotify-clone/ via Maven, ou la racine du dépôt).
    private static final List<Path> DOSSIERS_CANDIDATS = List.of(
            Path.of("src/main/resources/music"),
            Path.of("spotify-clone/src/main/resources/music"));

    private static final Locale FR = Locale.FRENCH;

    // Garde une référence forte sur le lecteur d'analyse en cours pour éviter qu'il soit collecté.
    private MediaPlayer sondeEnCours;

    public Path trouverDossierMusique() {
        return DOSSIERS_CANDIDATS.stream()
                .filter(Files::isDirectory)
                .findFirst()
                .orElse(DOSSIERS_CANDIDATS.get(0))
                .toAbsolutePath()
                .normalize();
    }

    public List<Track> chargerBibliotheque(Path dossier) {
        if (!Files.isDirectory(dossier)) {
            return Collections.emptyList();
        }
        try (Stream<Path> stream = Files.walk(dossier)) {
            return stream
                    .filter(Files::isRegularFile)
                    .filter(p -> {
                        String nom = p.getFileName().toString().toLowerCase(Locale.ROOT);
                        return nom.endsWith(".mp3") || nom.endsWith(".wav");
                    })
                    .sorted(Comparator.comparing(p -> p.getFileName().toString().toLowerCase(Locale.ROOT)))
                    .map(this::creerTrackDepuisFichier)
                    .collect(Collectors.toList());
        } catch (IOException e) {
            e.printStackTrace();
            return Collections.emptyList();
        }
    }

    /**
     * Analyse les fichiers un par un en arrière-plan pour récupérer la vraie durée,
     * la pochette ID3 et, si le nom de fichier ne suit pas "Artiste - Titre", les tags.
     */
    public void chargerMetadonnees(List<Track> tracks, Runnable quandTermine) {
        chargerSuivant(new ArrayDeque<>(tracks), quandTermine);
    }

    private void chargerSuivant(Deque<Track> file, Runnable quandTermine) {
        Track track = file.poll();
        if (track == null) {
            sondeEnCours = null;
            quandTermine.run();
            return;
        }
        try {
            Media media = new Media(track.getCheminFichier().toUri().toString());
            MediaPlayer sonde = new MediaPlayer(media);
            sondeEnCours = sonde;
            Runnable terminer = () -> {
                sonde.dispose();
                chargerSuivant(file, quandTermine);
            };
            sonde.setOnReady(() -> {
                appliquerMetadonnees(track, media);
                terminer.run();
            });
            sonde.setOnError(terminer);
        } catch (MediaException e) {
            chargerSuivant(file, quandTermine);
        }
    }

    private void appliquerMetadonnees(Track track, Media media) {
        Duration duree = media.getDuration();
        if (duree != null && !duree.isUnknown() && !duree.isIndefinite()) {
            track.dureeProperty().set(duree);
        }

        Map<String, Object> tags = media.getMetadata();
        if (tags.get("image") instanceof Image image) {
            track.pochetteProperty().set(image);
        }

        // Le nom de fichier fait foi ; les tags ne servent que s'il ne donne pas l'artiste.
        if (ARTISTE_INCONNU.equals(track.getArtiste())) {
            if (tags.get("artist") instanceof String artiste && !artiste.isBlank()) {
                track.artisteProperty().set(artiste.trim());
            }
            if (tags.get("title") instanceof String titre && !titre.isBlank()) {
                track.titreProperty().set(nettoyerTitre(titre.trim()));
            }
        }
    }

    private Track creerTrackDepuisFichier(Path cheminFichier) {
        String nomFichier = cheminFichier.getFileName().toString();
        String nom = nomFichier.substring(0, nomFichier.lastIndexOf('.'));

        String artiste = ARTISTE_INCONNU;
        String titre = nom;
        int separateur = nom.indexOf(" - ");
        if (separateur > 0) {
            artiste = nom.substring(0, separateur).trim();
            titre = nom.substring(separateur + 3).trim();
        }
        return new Track(nettoyerTitre(titre), artiste, Duration.UNKNOWN, cheminFichier);
    }

    /** Retire le bruit typique des noms de fichiers YouTube : "(Visualizer)", "CLIP OFFICIEL", années… */
    static String nettoyerTitre(String titre) {
        String propre = titre
                .replaceAll("[(\\[][^)\\]]*[)\\]]", " ")
                .replaceAll("(?i)\\b(clip officiel|clip|officiel|official (music )?video|official audio|lyrics?|a colors show|colors show|visualizer|audio)\\b", " ")
                .replaceAll("\\b(19|20)\\d{2}\\b", " ")
                .replaceAll("\\s+", " ")
                .trim();
        if (propre.isEmpty()) {
            propre = titre;
        }
        return propre.equals(propre.toUpperCase(FR)) ? capitaliser(propre) : propre;
    }

    private static String capitaliser(String texte) {
        return Stream.of(texte.toLowerCase(FR).split(" "))
                .map(mot -> mot.isEmpty() ? mot : mot.substring(0, 1).toUpperCase(FR) + mot.substring(1))
                .collect(Collectors.joining(" "));
    }
}
