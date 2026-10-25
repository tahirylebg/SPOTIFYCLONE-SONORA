package spotifyclone.model;

import java.nio.file.Path;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.image.Image;
import javafx.util.Duration;

/**
 * Un morceau de la bibliothèque. Les propriétés sont observables car la durée,
 * les tags et la pochette arrivent après coup, une fois le fichier analysé.
 */
public class Track {
    private final StringProperty titre;
    private final StringProperty artiste;
    private final ObjectProperty<Duration> duree;
    private final ObjectProperty<Image> pochette = new SimpleObjectProperty<>();
    private final Path cheminFichier;

    // Constructeur
    public Track(String titre, String artiste, Duration duree, Path cheminFichier) {
        this.titre = new SimpleStringProperty(titre);
        this.artiste = new SimpleStringProperty(artiste);
        this.duree = new SimpleObjectProperty<>(duree);
        this.cheminFichier = cheminFichier;
    }

    // Getters
    public String getTitre() { return titre.get(); }
    public StringProperty titreProperty() { return titre; }

    public String getArtiste() { return artiste.get(); }
    public StringProperty artisteProperty() { return artiste; }

    public Duration getDuree() { return duree.get(); }
    public ObjectProperty<Duration> dureeProperty() { return duree; }

    public Image getPochette() { return pochette.get(); }
    public ObjectProperty<Image> pochetteProperty() { return pochette; }

    public Path getCheminFichier() { return cheminFichier; }

    // toString pour afficher les informations de la piste
    @Override
    public String toString() {
        Duration d = getDuree();
        if (d == null || d.isUnknown() || d.isIndefinite()) {
            return String.format("%s - %s", getTitre(), getArtiste());
        }
        int totalSecondes = (int) d.toSeconds();
        return String.format("%s - %s (%d:%02d)", getTitre(), getArtiste(), totalSecondes / 60, totalSecondes % 60);
    }
}
