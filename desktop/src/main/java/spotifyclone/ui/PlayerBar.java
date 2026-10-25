package spotifyclone.ui;

import java.util.Locale;

import javafx.application.Platform;
import javafx.css.PseudoClass;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.util.Duration;
import spotifyclone.model.Track;

/**
 * Barre de lecture en trois zones : morceau en cours | contrôles + progression | volume.
 */
public class PlayerBar extends BorderPane {

    private static final PseudoClass ACTIF = PseudoClass.getPseudoClass("actif");

    private final Button shuffleButton = boutonBascule(Icons.shuffle(16));
    private final Button repeatButton = boutonBascule(Icons.repeat(16));
    private final Button previousButton = bouton(Icons.previous(18));
    private final Button playPauseButton = new Button();
    private final Button nextButton = bouton(Icons.next(18));
    private final Button volumeButton = bouton(Icons.volume(18));
    private final Slider progressSlider = new Slider(0, 1, 0);
    private final Slider volumeSlider = new Slider(0, 1, 0.5);
    private final Cover cover = new Cover(56, 6);
    private final Label titleLabel = new Label("Aucun morceau");
    private final Label artistLabel = new Label("Double-clique sur un titre pour l'écouter");
    private final Label elapsedLabel = new Label("0:00");
    private final Label totalLabel = new Label("0:00");

    public PlayerBar() {
        getStyleClass().add("player-bar");
        setMinHeight(92);
        setPrefHeight(92);

        playPauseButton.getStyleClass().add("play-btn");
        playPauseButton.setFocusTraversable(false);
        setPlaying(false);

        // Gauche : pochette + titre/artiste
        titleLabel.getStyleClass().add("now-title");
        artistLabel.getStyleClass().add("now-artist");
        titleLabel.setMinWidth(0);
        artistLabel.setMinWidth(0);
        VBox infos = new VBox(3, titleLabel, artistLabel);
        infos.setAlignment(Pos.CENTER_LEFT);
        infos.setMinWidth(0);
        HBox gauche = new HBox(14, cover, infos);
        gauche.setAlignment(Pos.CENTER_LEFT);
        gauche.setMinWidth(0);
        gauche.prefWidthProperty().bind(widthProperty().multiply(0.28));

        // Centre : contrôles au-dessus de la barre de progression
        HBox controles = new HBox(14, shuffleButton, previousButton, playPauseButton, nextButton, repeatButton);
        controles.setAlignment(Pos.CENTER);

        elapsedLabel.getStyleClass().add("time-label");
        totalLabel.getStyleClass().add("time-label");
        elapsedLabel.setAlignment(Pos.CENTER_RIGHT);
        elapsedLabel.setMinWidth(40);
        totalLabel.setMinWidth(40);
        progressSlider.getStyleClass().add("progress-slider");
        progressSlider.setMaxWidth(Double.MAX_VALUE);
        progressSlider.setFocusTraversable(false);
        HBox.setHgrow(progressSlider, Priority.ALWAYS);
        HBox progression = new HBox(10, elapsedLabel, progressSlider, totalLabel);
        progression.setAlignment(Pos.CENTER);

        VBox centre = new VBox(6, controles, progression);
        centre.setAlignment(Pos.CENTER);
        centre.setMaxWidth(640);
        BorderPane.setMargin(centre, new Insets(0, 24, 0, 24));

        // Droite : volume
        volumeSlider.getStyleClass().add("volume-slider");
        volumeSlider.setPrefWidth(110);
        volumeSlider.setFocusTraversable(false);
        volumeSlider.valueProperty().addListener((obs, ancien, valeur) ->
                volumeButton.setGraphic(valeur.doubleValue() <= 0 ? Icons.volumeMute(18) : Icons.volume(18)));
        HBox droite = new HBox(6, volumeButton, volumeSlider);
        droite.setAlignment(Pos.CENTER_RIGHT);
        droite.setMinWidth(160);
        droite.prefWidthProperty().bind(widthProperty().multiply(0.28));

        setLeft(gauche);
        setCenter(centre);
        setRight(droite);
        BorderPane.setAlignment(gauche, Pos.CENTER_LEFT);
        BorderPane.setAlignment(centre, Pos.CENTER);
        BorderPane.setAlignment(droite, Pos.CENTER_RIGHT);

        remplissageProgressif(progressSlider);
        remplissageProgressif(volumeSlider);
    }

    public void afficherMorceau(Track track) {
        titleLabel.textProperty().unbind();
        artistLabel.textProperty().unbind();
        cover.afficher(track);
        if (track == null) {
            titleLabel.setText("Aucun morceau");
            artistLabel.setText("");
            return;
        }
        titleLabel.textProperty().bind(track.titreProperty());
        artistLabel.textProperty().bind(track.artisteProperty());
    }

    public void setPlaying(boolean enLecture) {
        playPauseButton.setGraphic(enLecture ? Icons.pause(18) : Icons.play(18));
    }

    public void setTemps(Duration courant, Duration total) {
        String ecoule = Format.duree(courant);
        String duree = Format.duree(total);
        elapsedLabel.setText(ecoule.isEmpty() ? "0:00" : ecoule);
        totalLabel.setText(duree.isEmpty() ? "0:00" : duree);
    }

    public void setTempsEcoule(Duration courant) {
        String ecoule = Format.duree(courant);
        elapsedLabel.setText(ecoule.isEmpty() ? "0:00" : ecoule);
    }

    public void setShuffle(boolean actif) {
        shuffleButton.pseudoClassStateChanged(ACTIF, actif);
    }

    public void setRepeat(boolean actif, boolean unSeul) {
        repeatButton.setGraphic(graphiqueBascule(unSeul ? Icons.repeatOne(16) : Icons.repeat(16)));
        repeatButton.pseudoClassStateChanged(ACTIF, actif);
    }

    public Button getShuffleButton() { return shuffleButton; }
    public Button getRepeatButton() { return repeatButton; }
    public Button getPreviousButton() { return previousButton; }
    public Button getPlayPauseButton() { return playPauseButton; }
    public Button getNextButton() { return nextButton; }
    public Button getVolumeButton() { return volumeButton; }
    public Slider getProgressSlider() { return progressSlider; }
    public Slider getVolumeSlider() { return volumeSlider; }

    private static Button bouton(Node icone) {
        Button btn = new Button();
        btn.setGraphic(icone);
        btn.getStyleClass().add("player-btn");
        btn.setFocusTraversable(false);
        btn.setMinWidth(Region.USE_PREF_SIZE);
        return btn;
    }

    /** Bouton avec un point vert sous l'icône quand il est actif (shuffle, repeat). */
    private static Button boutonBascule(Region icone) {
        return bouton(graphiqueBascule(icone));
    }

    private static Node graphiqueBascule(Region icone) {
        Region point = new Region();
        point.getStyleClass().add("dot");
        point.setMinSize(4, 4);
        point.setMaxSize(4, 4);
        point.setTranslateY(12);
        return new StackPane(icone, point);
    }

    /** Colore la piste du slider jusqu'au curseur (vert au survol, comme Spotify). */
    private static void remplissageProgressif(Slider slider) {
        Runnable maj = () -> {
            Node piste = slider.lookup(".track");
            if (piste == null) {
                return;
            }
            double etendue = slider.getMax() - slider.getMin();
            double pct = etendue <= 0 ? 0 : (slider.getValue() - slider.getMin()) / etendue * 100;
            piste.setStyle(String.format(Locale.ROOT,
                    "-fx-background-color: linear-gradient(to right, -slider-fill %.2f%%, -slider-rest %.2f%%);", pct, pct));
        };
        slider.valueProperty().addListener((obs, ancien, valeur) -> maj.run());
        slider.skinProperty().addListener((obs, ancien, skin) -> Platform.runLater(maj));
    }
}
