package spotifyclone.controller;

import java.nio.file.Path;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.media.MediaPlayer;
import javafx.util.Duration;
import spotifyclone.model.Track;
import spotifyclone.service.AudioPlayerService;
import spotifyclone.service.LibraryService;
import spotifyclone.service.PlaybackQueue;
import spotifyclone.service.PlaybackQueue.RepeatMode;
import spotifyclone.ui.MainView;

public class MainController {
    private MainView mainView;
    private LibraryService libraryService;
    private ObservableList<Track> trackObservableList;
    private final AudioPlayerService audioPlayerService = new AudioPlayerService();
    private PlaybackQueue playbackQueue;
    private boolean userIsSeeking = false;

    public MainController(MainView mainView){
        this.mainView = mainView;
        this.libraryService = new LibraryService();
        this.trackObservableList = FXCollections.observableArrayList();
        mainView.getTrackList().setItems(trackObservableList);
        chargerBibliotheque();
        initialiserInteractions();
    }

    private void chargerBibliotheque(){
        Path dossier = Path.of("src/main/resources/music");
        trackObservableList.setAll(libraryService.chargerBibliotheque(dossier));
        playbackQueue = new PlaybackQueue(trackObservableList);
    }

    private void initialiserInteractions() {
        mainView.getTrackList().getSelectionModel().selectedIndexProperty().addListener((obs, oldIndex, newIndex) -> {
            if (newIndex == null) {
                return;
            }
            int idx = newIndex.intValue();
            if (idx < 0 || idx >= trackObservableList.size()) {
                return;
            }
            playbackQueue.setIndexCourant(idx);
            playTrack(trackObservableList.get(idx));
        });

        mainView.getPlayerBar().getPlayPauseButton().setOnAction(e -> togglePlayPause());
        mainView.getPlayerBar().getNextButton().setOnAction(e -> playNext());
        mainView.getPlayerBar().getPreviousButton().setOnAction(e -> playPrevious());
        mainView.getPlayerBar().getShuffleButton().setOnAction(e -> {
            if (playbackQueue == null) {
                return;
            }
            playbackQueue.toggleShuffle();
            updateShuffleStyle();
        });
        updateShuffleStyle();

        mainView.getPlayerBar().getRepeatButton().setOnAction(e -> {
            if (playbackQueue == null) {
                return;
            }
            playbackQueue.toggleRepeat();
            updateRepeatUi();
        });
        updateRepeatUi();

        mainView.getPlayerBar().getVolumeSlider().valueProperty().addListener((obs, oldV, newV) -> {
            MediaPlayer mp = audioPlayerService.getMediaPlayer();
            if (mp != null && newV != null) {
                mp.setVolume(newV.doubleValue());
            }
        });

        mainView.getPlayerBar().getProgressSlider().setOnMousePressed(e -> userIsSeeking = true);
        mainView.getPlayerBar().getProgressSlider().setOnMouseReleased(e -> {
            userIsSeeking = false;
            MediaPlayer mp = audioPlayerService.getMediaPlayer();
            if (mp == null) {
                return;
            }
            double value = mainView.getPlayerBar().getProgressSlider().getValue();
            Duration total = mp.getTotalDuration();
            if (total == null || total.isUnknown() || total.isIndefinite() || total.toMillis() <= 0) {
                return;
            }
            mp.seek(total.multiply(value));
        });
    }

    private void updateShuffleStyle() {
        boolean actif = playbackQueue != null && playbackQueue.isShuffleActif();
        String base = "-fx-background-color: transparent;";
        String active = "-fx-background-color: #1DB954; -fx-text-fill: #000000;";
        mainView.getPlayerBar().getShuffleButton().setStyle(actif ? active : base);
    }

    private void updateRepeatUi() {
        RepeatMode mode = playbackQueue == null ? RepeatMode.NONE : playbackQueue.getRepeatMode();
        var button = mainView.getPlayerBar().getRepeatButton();

        String baseStyle = "-fx-background-color: transparent;";
        String activeStyle = "-fx-background-color: #1DB954; -fx-text-fill: #000000;";

        if (mode == RepeatMode.ONE) {
            button.setText("🔂");
            button.setStyle(activeStyle);
        } else if (mode == RepeatMode.ALL) {
            button.setText("🔁");
            button.setStyle(activeStyle);
        } else {
            button.setText("🔁");
            button.setStyle(baseStyle);
        }
    }

    private void togglePlayPause() {
        MediaPlayer mp = audioPlayerService.getMediaPlayer();
        if (mp == null) {
            Track selected = mainView.getTrackList().getSelectionModel().getSelectedItem();
            if (selected == null && !trackObservableList.isEmpty()) {
                mainView.getTrackList().getSelectionModel().select(0);
                selected = trackObservableList.get(0);
            }
            playTrack(selected);
            return;
        }

        MediaPlayer.Status status = mp.getStatus();
        if (status == MediaPlayer.Status.PLAYING) {
            audioPlayerService.pause();
            mainView.getPlayerBar().getPlayPauseButton().setText("▶");
        } else {
            mp.play();
            mainView.getPlayerBar().getPlayPauseButton().setText("❚❚");
        }
    }

    private void playNext() {
        if (playbackQueue == null) {
            return;
        }
        Track next = playbackQueue.next();
        if (next == null) {
            return;
        }
        mainView.getTrackList().getSelectionModel().select(trackObservableList.indexOf(next));
        playTrack(next);
    }

    private void playPrevious() {
        if (playbackQueue == null) {
            return;
        }
        Track previous = playbackQueue.previous();
        if (previous == null) {
            return;
        }
        mainView.getTrackList().getSelectionModel().select(trackObservableList.indexOf(previous));
        playTrack(previous);
    }

    private void playTrack(Track track) {
        if (track == null) {
            return;
        }

        mainView.getPlayerBar().getTrackLabel().setText(track.getTitre());
        mainView.getPlayerBar().getPlayPauseButton().setText("❚❚");
        mainView.getPlayerBar().getProgressSlider().setValue(0);
        mainView.getPlayerBar().getTimeLabel().setText("0:00 / " + formatDuration(track.getDuree()));

        audioPlayerService.play(track);
        MediaPlayer mp = audioPlayerService.getMediaPlayer();
        if (mp == null) {
            return;
        }

        mp.setVolume(mainView.getPlayerBar().getVolumeSlider().getValue());

        mp.setOnReady(() -> Platform.runLater(() -> {
            Duration total = mp.getTotalDuration();
            String totalText = formatDuration(total);
            if (totalText.isBlank()) {
                totalText = formatDuration(track.getDuree());
            }
            mainView.getPlayerBar().getTimeLabel().setText("0:00 / " + (totalText.isBlank() ? "0:00" : totalText));
        }));

        mp.currentTimeProperty().addListener((obs, oldT, newT) -> {
            if (newT == null) {
                return;
            }
            Platform.runLater(() -> updateProgressAndTime(mp, newT));
        });

        mp.setOnEndOfMedia(() -> Platform.runLater(this::playNext));
    }

    private void updateProgressAndTime(MediaPlayer mp, Duration current) {
        Duration total = mp.getTotalDuration();
        if (total == null || total.isUnknown() || total.isIndefinite() || total.toMillis() <= 0) {
            mainView.getPlayerBar().getTimeLabel().setText(formatDuration(current) + " / 0:00");
            return;
        }

        if (!userIsSeeking) {
            double progress = Math.max(0, Math.min(1, current.toMillis() / total.toMillis()));
            mainView.getPlayerBar().getProgressSlider().setValue(progress);
        }

        mainView.getPlayerBar().getTimeLabel().setText(formatDuration(current) + " / " + formatDuration(total));
    }

    private static String formatDuration(Duration duration) {
        if (duration == null || duration.isUnknown() || duration.isIndefinite()) {
            return "";
        }
        int totalSeconds = (int) Math.max(0, Math.floor(duration.toSeconds()));
        int minutes = totalSeconds / 60;
        int seconds = totalSeconds % 60;
        return String.format("%d:%02d", minutes, seconds);
    }
}
