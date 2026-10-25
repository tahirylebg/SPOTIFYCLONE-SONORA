package spotifyclone.controller;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javafx.scene.Scene;
import javafx.scene.control.Slider;
import javafx.scene.control.TextInputControl;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.media.MediaPlayer;
import javafx.util.Duration;
import spotifyclone.model.AppState;
import spotifyclone.model.Track;
import spotifyclone.service.AudioPlayerService;
import spotifyclone.service.PlaybackQueue;
import spotifyclone.service.PlaybackQueue.RepeatMode;
import spotifyclone.ui.Format;
import spotifyclone.ui.PlayerBar;

/** Pilote la lecture : file, MediaPlayer et barre du lecteur. */
public class PlayerController {

    // Au-delà de ce temps, "précédent" revient au début du morceau au lieu de changer de piste.
    private static final Duration SEUIL_PRECEDENT = Duration.seconds(3);

    private final PlayerBar playerBar;
    private final AppState state;
    private final AudioPlayerService audioPlayerService = new AudioPlayerService();
    private final PlaybackQueue playbackQueue = new PlaybackQueue();
    private boolean majDepuisLecteur = false;
    private double volumeAvantMute = 0.5;

    public PlayerController(PlayerBar playerBar, AppState state) {
        this.playerBar = playerBar;
        this.state = state;
        initialiserLecteur();
    }

    /** Joue {@code track} ; la suite enchaîne sur {@code contexte} (toute la bibliothèque par défaut). */
    public void jouer(Track track, List<Track> contexte) {
        if (track == null) {
            return;
        }
        playbackQueue.demarrer(contexte == null || contexte.isEmpty() ? state.getTracks() : contexte, track);
        lancer(track);
    }

    public void jouerAleatoire(List<Track> tracks) {
        if (tracks.isEmpty()) {
            return;
        }
        List<Track> melange = new ArrayList<>(tracks);
        Collections.shuffle(melange);
        jouer(melange.get(0), melange);
    }

    /** Raccourcis de lecture : Espace, Ctrl+←/→. */
    public void installerRaccourcis(Scene scene) {
        scene.addEventFilter(KeyEvent.KEY_PRESSED, e -> {
            boolean saisie = scene.getFocusOwner() instanceof TextInputControl;
            if (e.getCode() == KeyCode.SPACE && !saisie) {
                togglePlayPause();
                e.consume();
            } else if (e.isShortcutDown() && e.getCode() == KeyCode.RIGHT) {
                playNext();
                e.consume();
            } else if (e.isShortcutDown() && e.getCode() == KeyCode.LEFT) {
                playPrevious();
                e.consume();
            }
        });
    }

    private void initialiserLecteur() {
        playerBar.getPlayPauseButton().setOnAction(e -> togglePlayPause());
        playerBar.getNextButton().setOnAction(e -> playNext());
        playerBar.getPreviousButton().setOnAction(e -> playPrevious());

        playerBar.getShuffleButton().setOnAction(e -> {
            playbackQueue.toggleShuffle();
            playerBar.setShuffle(playbackQueue.isShuffleActif());
        });
        playerBar.getRepeatButton().setOnAction(e -> {
            playbackQueue.toggleRepeat();
            majRepeat();
        });
        majRepeat();

        Slider volume = playerBar.getVolumeSlider();
        volume.valueProperty().addListener((obs, ancien, valeur) -> {
            MediaPlayer mp = audioPlayerService.getMediaPlayer();
            if (mp != null) {
                mp.setVolume(valeur.doubleValue());
            }
        });
        playerBar.getVolumeButton().setOnAction(e -> {
            if (volume.getValue() > 0) {
                volumeAvantMute = volume.getValue();
                volume.setValue(0);
            } else {
                volume.setValue(volumeAvantMute > 0 ? volumeAvantMute : 0.5);
            }
        });

        // Seek : au relâchement d'un glisser, ou immédiatement sur un clic dans la piste.
        Slider progression = playerBar.getProgressSlider();
        progression.valueChangingProperty().addListener((obs, avant, enCours) -> {
            if (!enCours) {
                seek(progression.getValue());
            }
        });
        progression.valueProperty().addListener((obs, ancien, valeur) -> {
            if (majDepuisLecteur) {
                return;
            }
            if (progression.isValueChanging()) {
                MediaPlayer mp = audioPlayerService.getMediaPlayer();
                if (mp != null && Format.estConnue(mp.getTotalDuration())) {
                    playerBar.setTempsEcoule(mp.getTotalDuration().multiply(valeur.doubleValue()));
                }
            } else {
                seek(valeur.doubleValue());
            }
        });
    }

    private void majRepeat() {
        RepeatMode mode = playbackQueue.getRepeatMode();
        playerBar.setRepeat(mode != RepeatMode.NONE, mode == RepeatMode.ONE);
    }

    private void togglePlayPause() {
        MediaPlayer mp = audioPlayerService.getMediaPlayer();
        if (mp == null) {
            if (!state.getTracks().isEmpty()) {
                jouer(state.getTracks().get(0), state.getTracks());
            }
            return;
        }

        if (mp.getStatus() == MediaPlayer.Status.PLAYING) {
            audioPlayerService.pause();
        } else {
            mp.play();
        }
    }

    private void playNext() {
        Track suivant = playbackQueue.next();
        if (suivant != null) {
            lancer(suivant);
        }
    }

    private void playPrevious() {
        MediaPlayer mp = audioPlayerService.getMediaPlayer();
        if (mp != null && mp.getCurrentTime().greaterThan(SEUIL_PRECEDENT)) {
            mp.seek(Duration.ZERO);
            return;
        }
        Track precedent = playbackQueue.previous();
        if (precedent != null) {
            lancer(precedent);
        }
    }

    private void finDuMorceau() {
        Track suivant = playbackQueue.nextAuto();
        if (suivant != null) {
            lancer(suivant);
            return;
        }
        // Fin de la file sans repeat : on s'arrête au début du dernier morceau.
        MediaPlayer mp = audioPlayerService.getMediaPlayer();
        if (mp != null) {
            mp.stop();
        }
    }

    private void lancer(Track track) {
        state.morceauCourantProperty().set(track);
        state.enregistrerEcoute(track);
        playerBar.afficherMorceau(track);
        playerBar.setTemps(Duration.ZERO, track.getDuree());
        definirProgression(0);

        audioPlayerService.play(track);
        MediaPlayer mp = audioPlayerService.getMediaPlayer();
        if (mp == null) {
            return;
        }

        mp.setVolume(playerBar.getVolumeSlider().getValue());
        playerBar.setPlaying(true);

        // Les gardes "mp == courant" ignorent les événements tardifs d'un lecteur déjà remplacé.
        mp.statusProperty().addListener((obs, ancien, statut) -> {
            if (mp == audioPlayerService.getMediaPlayer()) {
                playerBar.setPlaying(statut == MediaPlayer.Status.PLAYING);
            }
        });
        mp.currentTimeProperty().addListener((obs, ancien, temps) -> {
            if (mp == audioPlayerService.getMediaPlayer()) {
                majProgression(mp, temps);
            }
        });
        mp.setOnReady(() -> majProgression(mp, Duration.ZERO));
        mp.setOnEndOfMedia(this::finDuMorceau);
    }

    private void majProgression(MediaPlayer mp, Duration courant) {
        Duration total = mp.getTotalDuration();
        if (playerBar.getProgressSlider().isValueChanging()) {
            return;
        }
        playerBar.setTemps(courant, total);
        if (Format.estConnue(total) && total.toMillis() > 0 && courant != null) {
            definirProgression(Math.max(0, Math.min(1, courant.toMillis() / total.toMillis())));
        }
    }

    private void definirProgression(double valeur) {
        majDepuisLecteur = true;
        playerBar.getProgressSlider().setValue(valeur);
        majDepuisLecteur = false;
    }

    private void seek(double fraction) {
        MediaPlayer mp = audioPlayerService.getMediaPlayer();
        if (mp == null) {
            return;
        }
        Duration total = mp.getTotalDuration();
        if (Format.estConnue(total) && total.toMillis() > 0) {
            mp.seek(total.multiply(fraction));
        }
    }
}
