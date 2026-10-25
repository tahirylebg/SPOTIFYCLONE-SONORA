package spotifyclone.ui.pages;

import java.time.LocalTime;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import spotifyclone.model.AppState;
import spotifyclone.model.Track;
import spotifyclone.ui.Actions;
import spotifyclone.ui.Card;
import spotifyclone.ui.QuickTile;
import spotifyclone.ui.TrackTable;

public class HomePage extends Page {

    private static final int COLONNES_RAPIDES = 3;

    public HomePage(AppState state, Actions actions) {
        super(state, actions);
    }

    @Override
    public void rafraichir() {
        contenu.getChildren().setAll(entete(salutation(), null));

        List<Track> tracks = state.getTracks();
        if (tracks.isEmpty()) {
            contenu.getChildren().add(messageBibliothequeVide());
            return;
        }

        // Accès rapide : d'abord l'historique, complété par la bibliothèque.
        Set<Track> rapides = new LinkedHashSet<>(state.getHistorique());
        rapides.addAll(tracks);
        List<Track> tuiles = rapides.stream().limit(6).collect(Collectors.toList());
        contenu.getChildren().add(grilleRapide(tuiles));

        List<Track> historique = state.getHistorique();
        if (!historique.isEmpty()) {
            List<Track> recents = historique.stream().limit(6).collect(Collectors.toList());
            contenu.getChildren().add(section("Écoutés récemment", grille(recents.stream()
                    .map(t -> Card.morceau(t, () -> actions.jouer(t, recents)))
                    .collect(Collectors.toList()))));
        }

        Map<String, List<Track>> artistes = state.parArtiste();
        contenu.getChildren().add(section("Tes artistes", grille(artistes.entrySet().stream()
                .limit(8)
                .map(e -> Card.artiste(e.getKey(), e.getValue(),
                        () -> actions.ouvrirArtiste(e.getKey()),
                        () -> actions.jouer(e.getValue().get(0), e.getValue())))
                .collect(Collectors.toList()))));

        List<Track> plusEcoutes = state.plusEcoutes(5);
        if (!plusEcoutes.isEmpty()) {
            TrackTable table = new TrackTable(state, actions, false);
            table.setTracks(plusEcoutes);
            contenu.getChildren().add(section("Tes plus écoutés", table));
        }
    }

    private GridPane grilleRapide(List<Track> tuiles) {
        GridPane grille = new GridPane();
        grille.setHgap(12);
        grille.setVgap(12);
        for (int c = 0; c < COLONNES_RAPIDES; c++) {
            ColumnConstraints colonne = new ColumnConstraints();
            colonne.setPercentWidth(100.0 / COLONNES_RAPIDES);
            colonne.setHgrow(Priority.ALWAYS);
            grille.getColumnConstraints().add(colonne);
        }
        for (int i = 0; i < tuiles.size(); i++) {
            Track track = tuiles.get(i);
            grille.add(new QuickTile(track, () -> actions.jouer(track, tuiles)), i % COLONNES_RAPIDES, i / COLONNES_RAPIDES);
        }
        return grille;
    }

    private static String salutation() {
        int heure = LocalTime.now().getHour();
        if (heure < 5) {
            return "Bonne nuit";
        }
        if (heure < 12) {
            return "Bonjour";
        }
        if (heure < 18) {
            return "Bon après-midi";
        }
        return "Bonsoir";
    }
}
