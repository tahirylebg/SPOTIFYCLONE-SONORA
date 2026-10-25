package spotifyclone.ui;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import spotifyclone.model.Track;

/** Tuile horizontale d'accès rapide de l'accueil (pochette + titre, bouton lecture au survol). */
public class QuickTile extends HBox {

    public QuickTile(Track track, Runnable jouer) {
        super(14);
        getStyleClass().add("quick-tile");
        setAlignment(Pos.CENTER_LEFT);
        setMaxWidth(Double.MAX_VALUE);
        setPadding(new Insets(0, 12, 0, 0));

        Cover cover = new Cover(60, 4);
        cover.afficher(track);

        Label titre = new Label();
        titre.textProperty().bind(track.titreProperty());
        titre.getStyleClass().add("quick-tile-title");
        titre.setMinWidth(0);
        titre.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(titre, Priority.ALWAYS);

        Button play = Card.boutonPlay(36, jouer);

        getChildren().addAll(cover, titre, play);
        setOnMouseClicked(e -> {
            if (e.getClickCount() == 2) {
                jouer.run();
            }
        });
    }
}
