package spotifyclone.ui;

import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;

public class PlayerBar extends HBox {

    private Button shuffleButton;
    private Button repeatButton;
    private Button previousButton;
    private Button playPauseButton;
    private Button nextButton;
    private Slider progressSlider;
    private Slider volumeSlider;
    private Label trackLabel;
    private Label timeLabel;

    public PlayerBar() {
        getStyleClass().add("player-bar");

        StackPane nowPlayingCover = new StackPane(new Label("♪"));
        nowPlayingCover.getStyleClass().add("now-playing-cover");
        nowPlayingCover.setMinSize(48, 48);
        nowPlayingCover.setMaxSize(48, 48);

        shuffleButton = new Button("🔀");
        shuffleButton.getStyleClass().add("player-btn");
        shuffleButton.setFocusTraversable(false);
        shuffleButton.setMinWidth(Region.USE_PREF_SIZE);

        repeatButton = new Button("🔁");
        repeatButton.getStyleClass().add("player-btn");
        repeatButton.setFocusTraversable(false);
        repeatButton.setMinWidth(Region.USE_PREF_SIZE);

        previousButton = new Button("⏮");
        previousButton.getStyleClass().add("player-btn");
        previousButton.setFocusTraversable(false);
        previousButton.setMinWidth(Region.USE_PREF_SIZE);

        playPauseButton = new Button("▶");
        playPauseButton.getStyleClass().addAll("player-btn", "player-btn-primary");
        playPauseButton.setFocusTraversable(false);
        playPauseButton.setMinWidth(Region.USE_PREF_SIZE);

        nextButton = new Button("⏭");
        nextButton.getStyleClass().add("player-btn");
        nextButton.setFocusTraversable(false);
        nextButton.setMinWidth(Region.USE_PREF_SIZE);

        progressSlider = new Slider(0, 1, 0);
        progressSlider.getStyleClass().add("progress-slider");
        volumeSlider = new Slider(0, 1, 0.5);
        volumeSlider.getStyleClass().add("volume-slider");
        trackLabel = new Label("Aucun morceau");
        trackLabel.getStyleClass().add("track-label");
        trackLabel.setMinWidth(0);
        trackLabel.setMaxWidth(180);
        trackLabel.setWrapText(false);
        timeLabel = new Label("0:00 / 0:00");
        timeLabel.getStyleClass().add("time-label");

        setSpacing(18);
        setAlignment(Pos.CENTER);
        setMinHeight(88);

        HBox leftControls = new HBox(10, shuffleButton, repeatButton, previousButton, playPauseButton, nextButton);
        leftControls.setAlignment(Pos.CENTER_LEFT);
        leftControls.getStyleClass().add("player-left");

        HBox nowPlaying = new HBox(12, nowPlayingCover, trackLabel);
        nowPlaying.setAlignment(Pos.CENTER_LEFT);
        nowPlaying.getStyleClass().add("now-playing");
        nowPlaying.setMinWidth(240);
        nowPlaying.setPrefWidth(240);
        nowPlaying.setMaxWidth(240);
        HBox.setHgrow(nowPlaying, Priority.NEVER);

        progressSlider.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(progressSlider, Priority.ALWAYS);

        HBox center = new HBox(14, progressSlider, timeLabel);
        center.setAlignment(Pos.CENTER);
        center.getStyleClass().add("player-center");
        HBox.setHgrow(center, Priority.ALWAYS);

        HBox right = new HBox(10, new Label("🔊"), volumeSlider);
        right.setAlignment(Pos.CENTER_RIGHT);
        right.getStyleClass().add("player-right");

        HBox middle = new HBox(18, leftControls, center);
        middle.setAlignment(Pos.CENTER);
        HBox.setHgrow(middle, Priority.ALWAYS);

        getChildren().addAll(nowPlaying, middle, right);
    }

    public Button getShuffleButton() { return shuffleButton; }
    public Button getRepeatButton() { return repeatButton; }
    public Button getPreviousButton() { return previousButton; }
    public Button getPlayPauseButton() { return playPauseButton; }
    public Button getNextButton() { return nextButton; }
    public Slider getProgressSlider() { return progressSlider; }
    public Slider getVolumeSlider() { return volumeSlider; }
    public Label getTrackLabel() { return trackLabel; }
    public Label getTimeLabel() { return timeLabel; }
}
