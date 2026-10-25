package spotifyclone.ui;

import java.util.List;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import spotifyclone.model.Track;

/** Carte verticale (pochette + titre + sous-titre) avec un bouton lecture qui apparaît au survol. */
public class Card extends VBox {

    private static final double TAILLE_COVER = 156;

    private Card(Cover cover, ObservableValue<String> titre, ObservableValue<String> sousTitre,
                 Runnable ouvrir, Runnable jouer) {
        super(10);
        getStyleClass().add("card");
        setPrefWidth(TAILLE_COVER + 24);
        setMaxWidth(TAILLE_COVER + 24);

        Button play = boutonPlay(44, jouer);
        StackPane.setAlignment(play, Pos.BOTTOM_RIGHT);
        StackPane.setMargin(play, new Insets(0, 8, 8, 0));
        StackPane visuel = new StackPane(cover, play);
        visuel.setMaxSize(TAILLE_COVER, TAILLE_COVER);

        Label titreLabel = new Label();
        titreLabel.textProperty().bind(titre);
        titreLabel.getStyleClass().add("card-title");
        titreLabel.setMaxWidth(TAILLE_COVER);
        Label sousTitreLabel = new Label();
        sousTitreLabel.textProperty().bind(sousTitre);
        sousTitreLabel.getStyleClass().add("card-subtitle");
        sousTitreLabel.setMaxWidth(TAILLE_COVER);

        getChildren().addAll(visuel, new VBox(3, titreLabel, sousTitreLabel));
        setOnMouseClicked(e -> ouvrir.run());
    }

    public static Card morceau(Track track, Runnable jouer) {
        Cover cover = new Cover(TAILLE_COVER, 6);
        cover.afficher(track);
        return new Card(cover, track.titreProperty(), track.artisteProperty(), jouer, jouer);
    }

    public static Card artiste(String nom, List<Track> titres, Runnable ouvrir, Runnable jouer) {
        Cover cover = Cover.ronde(TAILLE_COVER);
        cover.afficherArtiste(nom, titres);
        Card card = new Card(cover, new SimpleStringProperty(nom),
                new SimpleStringProperty("Artiste · " + Format.pluriel(titres.size(), "titre")), ouvrir, jouer);
        card.getStyleClass().add("card-artist");
        return card;
    }

    /** Bouton rond vert "lecture", réutilisé par les tuiles et les bandeaux. */
    public static Button boutonPlay(double taille, Runnable jouer) {
        Button play = new Button();
        play.setGraphic(Icons.play(taille * 0.45));
        play.getStyleClass().add("fab");
        play.setFocusTraversable(false);
        play.setMinSize(taille, taille);
        play.setPrefSize(taille, taille);
        play.setMaxSize(taille, taille);
        play.setOnAction(e -> jouer.run());
        // Le clic sur le bouton ne doit pas aussi "ouvrir" la carte qui le contient.
        play.setOnMouseClicked(e -> e.consume());
        return play;
    }
}
