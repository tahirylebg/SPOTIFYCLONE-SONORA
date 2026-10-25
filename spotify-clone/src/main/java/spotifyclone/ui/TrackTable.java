package spotifyclone.ui;

import java.util.List;

import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import spotifyclone.model.AppState;
import spotifyclone.model.Track;

/**
 * Liste de morceaux (non virtualisée : faite pour vivre dans une page qui défile).
 * Clic = sélection, double-clic = lecture avec la liste comme contexte.
 */
public class TrackTable extends VBox {

    private final AppState state;
    private final Actions actions;
    private final VBox lignes = new VBox(2);
    private TrackRow selection;

    public TrackTable(AppState state, Actions actions, boolean avecEntete) {
        this.state = state;
        this.actions = actions;
        getStyleClass().add("track-table");
        if (avecEntete) {
            getChildren().add(entete());
        }
        getChildren().add(lignes);
    }

    public void setTracks(List<Track> tracks) {
        lignes.getChildren().clear();
        selection = null;
        List<Track> contexte = List.copyOf(tracks);
        for (int i = 0; i < contexte.size(); i++) {
            Track track = contexte.get(i);
            TrackRow ligne = new TrackRow(state.morceauCourantProperty());
            ligne.setTrack(track, i + 1);
            ligne.setOnMouseClicked(e -> {
                if (e.getButton() != MouseButton.PRIMARY) {
                    return;
                }
                selectionner(ligne);
                if (e.getClickCount() == 2) {
                    actions.jouer(track, contexte);
                }
            });
            lignes.getChildren().add(ligne);
        }
    }

    private void selectionner(TrackRow ligne) {
        if (selection != null) {
            selection.setSelected(false);
        }
        selection = ligne;
        ligne.setSelected(true);
    }

    /** En-tête des colonnes, aligné sur les marges de TrackRow. */
    private static HBox entete() {
        Label numero = new Label("#");
        numero.setMinWidth(24);
        numero.setAlignment(Pos.CENTER);
        Region espaceCover = new Region();
        espaceCover.setMinWidth(40);
        Label titre = new Label("TITRE");
        titre.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(titre, Priority.ALWAYS);
        HBox horloge = new HBox(Icons.clock(14));
        horloge.setMinWidth(48);
        horloge.setAlignment(Pos.CENTER_RIGHT);

        HBox entete = new HBox(14, numero, espaceCover, titre, horloge);
        entete.getStyleClass().add("list-header");
        entete.setAlignment(Pos.CENTER_LEFT);
        return entete;
    }
}
