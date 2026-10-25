package spotifyclone.ui;

import javafx.beans.InvalidationListener;
import javafx.beans.WeakInvalidationListener;
import javafx.beans.binding.Bindings;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.css.PseudoClass;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import spotifyclone.model.Track;

/** Une ligne de morceau : numéro (ou égaliseur s'il joue), pochette, titre/artiste, durée. */
public class TrackRow extends HBox {

    private static final PseudoClass PLAYING = PseudoClass.getPseudoClass("playing");
    private static final PseudoClass SELECTED = PseudoClass.getPseudoClass("selected");

    private final ReadOnlyObjectProperty<Track> morceauCourant;
    private final Label numeroLabel = new Label();
    private final Region egaliseur = Icons.equalizer(14);
    private final Cover cover = new Cover(40, 4);
    private final Label titleLabel = new Label();
    private final Label metaLabel = new Label();
    private final Label durationLabel = new Label();
    // Référence forte nécessaire : la propriété partagée ne garde qu'une référence faible.
    private final InvalidationListener majLecture = obs -> majEtatLecture();
    private Track track;

    public TrackRow(ReadOnlyObjectProperty<Track> morceauCourant) {
        super(14);
        this.morceauCourant = morceauCourant;
        getStyleClass().add("track-row");
        setAlignment(Pos.CENTER_LEFT);

        numeroLabel.getStyleClass().add("track-number");
        egaliseur.getStyleClass().add("playing-icon");
        StackPane colonneNumero = new StackPane(numeroLabel, egaliseur);
        colonneNumero.setMinWidth(24);
        colonneNumero.setPrefWidth(24);

        titleLabel.getStyleClass().add("track-title");
        metaLabel.getStyleClass().add("track-meta");
        titleLabel.setMinWidth(0);
        metaLabel.setMinWidth(0);
        VBox textBlock = new VBox(2, titleLabel, metaLabel);
        textBlock.setAlignment(Pos.CENTER_LEFT);
        textBlock.setMinWidth(0);
        textBlock.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(textBlock, Priority.ALWAYS);

        durationLabel.getStyleClass().add("track-duration");
        durationLabel.setMinWidth(48);
        durationLabel.setAlignment(Pos.CENTER_RIGHT);

        getChildren().addAll(colonneNumero, cover, textBlock, durationLabel);
        morceauCourant.addListener(new WeakInvalidationListener(majLecture));
    }

    public void setTrack(Track track, int numero) {
        this.track = track;
        numeroLabel.setText(String.valueOf(numero));
        titleLabel.textProperty().bind(track.titreProperty());
        metaLabel.textProperty().bind(track.artisteProperty());
        durationLabel.textProperty().bind(Bindings.createStringBinding(
                () -> Format.duree(track.getDuree()), track.dureeProperty()));
        cover.afficher(track);
        majEtatLecture();
    }

    public Track getTrack() {
        return track;
    }

    public void setSelected(boolean selectionne) {
        pseudoClassStateChanged(SELECTED, selectionne);
    }

    private void majEtatLecture() {
        boolean enCours = track != null && track == morceauCourant.get();
        pseudoClassStateChanged(PLAYING, enCours);
        numeroLabel.setVisible(!enCours);
        egaliseur.setVisible(enCours);
    }
}
