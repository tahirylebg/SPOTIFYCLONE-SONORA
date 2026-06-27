package spotifyclone.service;

import spotifyclone.model.Track;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;
import java.util.stream.Collectors;
import java.util.Collections;

public class LibraryService {

    public List<Track> chargerBibliotheque(Path dossier){
        try (Stream<Path> stream = Files.walk(dossier)){
            return stream
                    .filter(Files::isRegularFile)
                    .filter(p -> p.toString().endsWith(".mp3") || p.toString().endsWith(".wav"))
                    .map(this::creerTrackDepuisFichier)
                    .collect(Collectors.toList());
        } catch (IOException e) {
            e.printStackTrace();
            return Collections.emptyList();
        }
    }

    private Track creerTrackDepuisFichier(Path cheminFichier){
        // Ici, vous pouvez utiliser une bibliothèque comme jaudiotagger pour extraire les métadonnées
        // Pour l'exemple, nous allons créer un Track fictif
        String nomFichier = cheminFichier.getFileName().toString();
        String titre = nomFichier.substring(0, nomFichier.lastIndexOf('.'));
        String artiste = "Artiste Inconnu";
        javafx.util.Duration duree = javafx.util.Duration.seconds(180); // Durée fictive de 3 minutes
        return new Track(titre, artiste, duree, cheminFichier);
    }

    public static void main(String[] args) {
        LibraryService service = new LibraryService();
        Path dossier = Path.of("src/main/resources/music");
        List<Track> tracks = service.chargerBibliotheque(dossier);
        tracks.forEach(System.out::println);
    }

}
