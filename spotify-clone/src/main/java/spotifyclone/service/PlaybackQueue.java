package spotifyclone.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import spotifyclone.model.Track;

public class PlaybackQueue {

    public enum RepeatMode {
        NONE, ONE, ALL
    }

    private List<Track> tracks;
    private List<Track> tracksOriginales;
    private boolean shuffleActif;
    private RepeatMode repeatMode = RepeatMode.NONE;
    private int indexCourant;

    public PlaybackQueue(List<Track> tracks) {
        this.tracks = tracks;
        this.tracksOriginales = null;
        this.shuffleActif = false;
        this.repeatMode = RepeatMode.NONE;
        this.indexCourant = 0;
    }

    public Track getCourant() {
        if (tracks == null || tracks.isEmpty()) {
            return null;
        }
        indexCourant = Math.max(0, Math.min(indexCourant, tracks.size() - 1));
        return tracks.get(indexCourant);
    }

    public Track next() {
        if (tracks == null || tracks.isEmpty()) {
            return null;
        }

        if (repeatMode == RepeatMode.ONE) {
            return getCourant();
        }

        if (repeatMode == RepeatMode.ALL) {
            indexCourant = (indexCourant + 1) % tracks.size();
            return getCourant();
        }

        if (indexCourant < tracks.size() - 1) {
            indexCourant++;
        }
        return getCourant();
    }

    public Track previous() {
        if (tracks == null || tracks.isEmpty()) {
            return null;
        }
        if (indexCourant > 0) {
            indexCourant--;
        }
        return getCourant();
    }

    public void setIndexCourant(int index) {
        if (tracks == null || tracks.isEmpty()) {
            indexCourant = 0;
            return;
        }
        indexCourant = Math.max(0, Math.min(index, tracks.size() - 1));
    }

    public void toggleShuffle() {
        if (tracks == null || tracks.isEmpty()) {
            shuffleActif = !shuffleActif;
            return;
        }

        Track courant = getCourant();
        shuffleActif = !shuffleActif;

        if (shuffleActif) {
            tracksOriginales = new ArrayList<>(tracks);
            Collections.shuffle(tracks);
        } else {
            if (tracksOriginales != null) {
                tracks = new ArrayList<>(tracksOriginales);
                tracksOriginales = null;
            }
        }

        if (courant != null && tracks != null) {
            int idx = tracks.indexOf(courant);
            if (idx >= 0) {
                indexCourant = idx;
            }
        }
    }

    public boolean isShuffleActif() {
        return shuffleActif;
    }

    public void toggleRepeat() {
        switch (repeatMode) {
            case NONE -> repeatMode = RepeatMode.ONE;
            case ONE -> repeatMode = RepeatMode.ALL;
            case ALL -> repeatMode = RepeatMode.NONE;
        }
    }

    public RepeatMode getRepeatMode() {
        return repeatMode;
    }
}
