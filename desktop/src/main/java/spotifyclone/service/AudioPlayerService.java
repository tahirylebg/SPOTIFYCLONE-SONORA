package spotifyclone.service;

import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import spotifyclone.model.Track;

public class AudioPlayerService {

    private MediaPlayer mediaPlayer;

    public void play(Track track) {
        if (track == null || track.getCheminFichier() == null) {
            return;
        }

        stop();

        Media media = new Media(track.getCheminFichier().toUri().toString());
        mediaPlayer = new MediaPlayer(media);
        mediaPlayer.play();
    }

    public void pause() {
        if (mediaPlayer != null) {
            mediaPlayer.pause();
        }
    }

    public void stop() {
        if (mediaPlayer == null) {
            return;
        }
        mediaPlayer.stop();
        mediaPlayer.dispose();
        mediaPlayer = null;
    }

    public MediaPlayer getMediaPlayer() {
        return mediaPlayer;
    }
}
