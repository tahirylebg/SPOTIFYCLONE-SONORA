package spotifyclone.ui;

import java.util.List;

import spotifyclone.model.Track;

/** Ce que les pages peuvent demander au reste de l'application. */
public interface Actions {

    /** Joue {@code track} ; la suite de la lecture enchaîne sur {@code contexte}. */
    void jouer(Track track, List<Track> contexte);

    /** Joue la liste dans un ordre aléatoire. */
    void jouerAleatoire(List<Track> tracks);

    /** Ouvre la recherche filtrée sur un artiste. */
    void ouvrirArtiste(String artiste);
}
