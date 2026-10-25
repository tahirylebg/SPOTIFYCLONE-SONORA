package spotifyclone.ui.pages;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.util.Duration;
import spotifyclone.model.AppState;
import spotifyclone.model.Track;
import spotifyclone.ui.Actions;
import spotifyclone.ui.Card;
import spotifyclone.ui.Format;
import spotifyclone.ui.TrackTable;

public class LibraryPage extends Page {

    private enum Vue { TITRES, ARTISTES }

    private enum Tri {
        DEFAUT("Par défaut", null),
        TITRE("Titre", Comparator.comparing(Track::getTitre, String.CASE_INSENSITIVE_ORDER)),
        ARTISTE("Artiste", Comparator.comparing(Track::getArtiste, String.CASE_INSENSITIVE_ORDER)
                .thenComparing(Track::getTitre, String.CASE_INSENSITIVE_ORDER)),
        DUREE("Durée", Comparator.comparingDouble((Track t) -> secondes(t.getDuree())).reversed());

        final String libelle;
        final Comparator<Track> comparateur;

        Tri(String libelle, Comparator<Track> comparateur) {
            this.libelle = libelle;
            this.comparateur = comparateur;
        }
    }

    private Vue vue = Vue.TITRES;
    private Tri tri = Tri.DEFAUT;

    public LibraryPage(AppState state, Actions actions) {
        super(state, actions);
    }

    @Override
    public void rafraichir() {
        List<Track> tracks = state.getTracks();
        Map<String, List<Track>> artistes = state.parArtiste();
        contenu.getChildren().setAll(entete("Bibliothèque",
                Format.resume(tracks) + " · " + Format.pluriel(artistes.size(), "artiste")));

        if (tracks.isEmpty()) {
            contenu.getChildren().add(messageBibliothequeVide());
            return;
        }

        HBox barre = new HBox(8);
        barre.setAlignment(Pos.CENTER_LEFT);
        barre.getChildren().addAll(chips(Vue.values(), vue, v -> v == Vue.TITRES ? "Titres" : "Artistes", v -> vue = v));
        if (vue == Vue.TITRES) {
            Region espace = new Region();
            HBox.setHgrow(espace, Priority.ALWAYS);
            Label trier = new Label("Trier par");
            trier.getStyleClass().add("chip-label");
            barre.getChildren().addAll(espace, trier);
            barre.getChildren().addAll(chips(Tri.values(), tri, t -> t.libelle, t -> tri = t));
        }
        contenu.getChildren().add(barre);

        if (vue == Vue.TITRES) {
            List<Track> tries = new ArrayList<>(tracks);
            if (tri.comparateur != null) {
                tries.sort(tri.comparateur);
            }
            TrackTable table = new TrackTable(state, actions, true);
            table.setTracks(tries);
            contenu.getChildren().add(table);
        } else {
            contenu.getChildren().add(grille(artistes.entrySet().stream()
                    .map(e -> Card.artiste(e.getKey(), e.getValue(),
                            () -> actions.ouvrirArtiste(e.getKey()),
                            () -> actions.jouer(e.getValue().get(0), e.getValue())))
                    .collect(Collectors.toList())));
        }
    }

    /** Groupe de puces exclusives ; un clic change l'option et reconstruit la page. */
    private <T> List<ToggleButton> chips(T[] options, T actuel, Function<T, String> libelle, Consumer<T> choisir) {
        ToggleGroup groupe = new ToggleGroup();
        List<ToggleButton> boutons = new ArrayList<>();
        for (T option : options) {
            ToggleButton chip = new ToggleButton(libelle.apply(option));
            chip.getStyleClass().add("chip");
            chip.setToggleGroup(groupe);
            chip.setSelected(option == actuel);
            chip.setFocusTraversable(false);
            chip.setOnAction(e -> {
                choisir.accept(option);
                rafraichir();
            });
            boutons.add(chip);
        }
        return boutons;
    }

    private static double secondes(Duration d) {
        return Format.estConnue(d) ? d.toSeconds() : 0;
    }
}
