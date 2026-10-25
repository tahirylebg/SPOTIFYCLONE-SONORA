package spotifyclone.model;

import java.nio.file.Path;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.function.Function;
import java.util.stream.Collectors;

import javafx.beans.Observable;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

/**
 * État partagé entre les pages : la bibliothèque, le morceau en cours et l'historique d'écoute.
 */
public class AppState {

    private static final int TAILLE_HISTORIQUE = 20;

    private final ObservableList<Track> tracks = FXCollections.observableArrayList(
            t -> new Observable[] { t.titreProperty(), t.artisteProperty(), t.dureeProperty() });
    private final ObjectProperty<Track> morceauCourant = new SimpleObjectProperty<>();
    private final ObservableList<Track> historique = FXCollections.observableArrayList();
    // Nombre d'écoutes par fichier, du plus récemment écouté au plus ancien.
    private LinkedHashMap<String, Integer> ecoutes = new LinkedHashMap<>();
    private Path dossierMusique;

    public ObservableList<Track> getTracks() { return tracks; }

    public Track getMorceauCourant() { return morceauCourant.get(); }
    public ObjectProperty<Track> morceauCourantProperty() { return morceauCourant; }

    /** Morceaux récemment écoutés, le plus récent en tête. */
    public ObservableList<Track> getHistorique() { return historique; }

    public Path getDossierMusique() { return dossierMusique; }
    public void setDossierMusique(Path dossierMusique) { this.dossierMusique = dossierMusique; }

    public void chargerEcoutes(Map<String, Integer> sauvegarde) {
        ecoutes = new LinkedHashMap<>(sauvegarde);
        majHistorique();
    }

    public void enregistrerEcoute(Track track) {
        String cle = cle(track);
        LinkedHashMap<String, Integer> maj = new LinkedHashMap<>();
        maj.put(cle, ecoutes.getOrDefault(cle, 0) + 1);
        ecoutes.forEach((k, n) -> maj.putIfAbsent(k, n));
        ecoutes = maj;
        majHistorique();
    }

    public int getEcoutes(Track track) {
        return ecoutes.getOrDefault(cle(track), 0);
    }

    public Map<String, Integer> getToutesEcoutes() {
        return Collections.unmodifiableMap(ecoutes);
    }

    /** Morceaux groupés par artiste, artistes triés par ordre alphabétique. */
    public Map<String, List<Track>> parArtiste() {
        return tracks.stream().collect(Collectors.groupingBy(
                Track::getArtiste,
                () -> new TreeMap<>(String.CASE_INSENSITIVE_ORDER),
                Collectors.toList()));
    }

    public List<Track> plusEcoutes(int limite) {
        return tracks.stream()
                .filter(t -> getEcoutes(t) > 0)
                .sorted(Comparator.comparingInt((Track t) -> getEcoutes(t)).reversed())
                .limit(limite)
                .collect(Collectors.toList());
    }

    private void majHistorique() {
        Map<String, Track> parCle = tracks.stream()
                .collect(Collectors.toMap(AppState::cle, Function.identity(), (a, b) -> a));
        historique.setAll(ecoutes.keySet().stream()
                .map(parCle::get)
                .filter(t -> t != null)
                .limit(TAILLE_HISTORIQUE)
                .collect(Collectors.toList()));
    }

    public static String cle(Track track) {
        return track.getCheminFichier().toAbsolutePath().normalize().toString();
    }
}
