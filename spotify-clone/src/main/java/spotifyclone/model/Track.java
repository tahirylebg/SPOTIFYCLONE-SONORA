package spotifyclone.model; 

import java.nio.file.Path;
import javafx.util.Duration;

public class Track {
    private String titre;
    private String artiste;
    private Duration duree;
    private Path cheminFichier;

    // Constructeur
    public Track(String titre, String artiste, Duration duree,Path cheminFichier){
        this.titre = titre;
        this.artiste = artiste;
        this.duree = duree;
        this.cheminFichier = cheminFichier;
    } 

    // Getters
    public String getTitre(){
        return titre;
    }

    public String getArtiste(){
        return artiste;
    }  

    public Duration getDuree(){
        return duree;
    }

    public Path getCheminFichier() {
        return cheminFichier;
    }
    
    // toString pour afficher les informations de la piste
    @Override
    public String toString(){
        int totalSecondes = (int) duree.toSeconds();
        int minutes = totalSecondes / 60;
        int secondes = totalSecondes % 60;
        return String.format("%s - %s (%d:%02d)", titre, artiste, minutes, secondes);
    }

}
