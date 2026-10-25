package spotifyclone.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import spotifyclone.model.Track;

/**
 * File de lecture. Elle travaille sur sa propre copie du contexte (la liste d'où
 * l'on a lancé la lecture) : le shuffle ne modifie jamais l'ordre affiché.
 */
public class PlaybackQueue {

    public enum RepeatMode {
        NONE, ONE, ALL
    }

    private List<Track> ordreOriginal = new ArrayList<>();
    private List<Track> file = new ArrayList<>();
    private int indexCourant = -1;
    private boolean shuffleActif;
    private RepeatMode repeatMode = RepeatMode.NONE;

    /** Démarre une nouvelle file à partir d'un contexte, en commençant par {@code depart}. */
    public void demarrer(List<Track> contexte, Track depart) {
        ordreOriginal = new ArrayList<>(contexte);
        if (!ordreOriginal.contains(depart)) {
            ordreOriginal.add(0, depart);
        }
        file = new ArrayList<>(ordreOriginal);
        if (shuffleActif) {
            Collections.shuffle(file);
            placerEnTete(depart);
        }
        indexCourant = file.indexOf(depart);
    }

    public Track getCourant() {
        return indexCourant >= 0 && indexCourant < file.size() ? file.get(indexCourant) : null;
    }

    /** Piste suivante demandée par l'utilisateur ; null en fin de file sans repeat. */
    public Track next() {
        if (file.isEmpty()) {
            return null;
        }
        if (indexCourant < file.size() - 1) {
            indexCourant++;
        } else if (repeatMode != RepeatMode.NONE) {
            indexCourant = 0;
        } else {
            return null;
        }
        return getCourant();
    }

    /** Piste à enchaîner quand le morceau courant se termine. */
    public Track nextAuto() {
        if (repeatMode == RepeatMode.ONE) {
            return getCourant();
        }
        return next();
    }

    public Track previous() {
        if (file.isEmpty()) {
            return null;
        }
        if (indexCourant > 0) {
            indexCourant--;
        } else if (repeatMode == RepeatMode.ALL) {
            indexCourant = file.size() - 1;
        } else {
            indexCourant = 0;
        }
        return getCourant();
    }

    public void toggleShuffle() {
        Track courant = getCourant();
        shuffleActif = !shuffleActif;
        file = new ArrayList<>(ordreOriginal);
        if (shuffleActif) {
            Collections.shuffle(file);
            placerEnTete(courant);
        }
        indexCourant = courant == null ? -1 : file.indexOf(courant);
    }

    // Le morceau en cours passe en tête pour ne pas rejouer ce qui précède.
    private void placerEnTete(Track track) {
        if (track != null && file.remove(track)) {
            file.add(0, track);
        }
    }

    public boolean isShuffleActif() {
        return shuffleActif;
    }

    public void toggleRepeat() {
        switch (repeatMode) {
            case NONE -> repeatMode = RepeatMode.ALL;
            case ALL -> repeatMode = RepeatMode.ONE;
            case ONE -> repeatMode = RepeatMode.NONE;
        }
    }

    public RepeatMode getRepeatMode() {
        return repeatMode;
    }
}
