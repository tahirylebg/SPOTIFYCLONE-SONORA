package spotifyclone.model;

public class PlayerState {
    private Track currentTrack;
    private boolean playing;
    private double volume = 0.5;

    public Track getCurrentTrack() { return currentTrack; }

    public void setCurrentTrack(Track currentTrack) {
        this.currentTrack = currentTrack;
    }

    public boolean isPlaying() { return playing; }

    public void setPlaying(boolean playing) {
        this.playing = playing;
    }

    public double getVolume() { return volume; }

    public void setVolume(double volume) {
        this.volume = Math.max(0, Math.min(1, volume));
    }
}

