package spotifyclone.ui.pages;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import spotifyclone.model.AppState;
import spotifyclone.model.Track;
import spotifyclone.ui.Actions;
import spotifyclone.ui.Card;
import spotifyclone.ui.Cover;
import spotifyclone.ui.Icons;

public class DiscoverPage extends Page {

    private static final int TAILLE_SELECTION = 6;

    // Gardée entre deux rafraîchissements : seul le bouton "Mélanger" la renouvelle.
    private List<Track> selectionAleatoire = List.of();

    public DiscoverPage(AppState state, Actions actions) {
        super(state, actions);
    }

    @Override
    public void rafraichir() {
        contenu.getChildren().setAll(entete("Découvertes", "Laisse-toi surprendre par ta propre bibliothèque."));

        List<Track> tracks = state.getTracks();
        if (tracks.isEmpty()) {
            contenu.getChildren().add(messageBibliothequeVide());
            return;
        }

        contenu.getChildren().add(bandeauMix(tracks));

        List<Track> jamaisEcoutes = tracks.stream()
                .filter(t -> state.getEcoutes(t) == 0)
                .collect(Collectors.toList());
        contenu.getChildren().add(section("Jamais écoutés", jamaisEcoutes.isEmpty()
                ? etatVide("Tu as écouté chaque titre au moins une fois. Bien joué !")
                : cartes(jamaisEcoutes.stream().limit(TAILLE_SELECTION).collect(Collectors.toList()))));

        if (selectionAleatoire.isEmpty() || !tracks.containsAll(selectionAleatoire)) {
            melanger();
        }
        Button melanger = new Button("Mélanger", Icons.shuffle(14));
        melanger.getStyleClass().add("ghost-btn");
        melanger.setFocusTraversable(false);
        melanger.setOnAction(e -> {
            melanger();
            rafraichir();
        });
        contenu.getChildren().add(section("Au hasard", melanger, cartes(selectionAleatoire)));
    }

    private StackPane bandeauMix(List<Track> tracks) {
        Label surtitre = new Label("MIX");
        surtitre.getStyleClass().add("hero-kicker");
        Label titre = new Label("Mix découverte");
        titre.getStyleClass().add("hero-title");
        Label description = new Label("Toute ta bibliothèque, dans un ordre que tu n'as jamais entendu.");
        description.getStyleClass().add("hero-text");
        description.setWrapText(true);

        Button lancer = new Button("Lancer le mix", Icons.play(14));
        lancer.getStyleClass().add("primary-btn");
        lancer.setFocusTraversable(false);
        lancer.setOnAction(e -> actions.jouerAleatoire(tracks));

        VBox texte = new VBox(8, surtitre, titre, description, new HBox(lancer));
        VBox.setMargin(texte.getChildren().get(3), new Insets(12, 0, 0, 0));
        texte.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(texte, Priority.ALWAYS);

        // Petit éventail de pochettes à droite
        HBox pochettes = new HBox(-36);
        pochettes.setAlignment(Pos.CENTER_RIGHT);
        List<Track> apercu = new ArrayList<>(tracks);
        Collections.shuffle(apercu);
        apercu.stream().limit(3).forEach(t -> {
            Cover cover = new Cover(120, 8);
            cover.afficher(t);
            cover.getStyleClass().add("hero-cover");
            pochettes.getChildren().add(cover);
        });

        Region espace = new Region();
        espace.setMinWidth(24);
        HBox ligne = new HBox(texte, espace, pochettes);
        ligne.setAlignment(Pos.CENTER_LEFT);

        StackPane bandeau = new StackPane(ligne);
        bandeau.getStyleClass().add("hero");
        return bandeau;
    }

    private FlowPane cartes(List<Track> liste) {
        return grille(liste.stream()
                .map(t -> Card.morceau(t, () -> actions.jouer(t, liste)))
                .collect(Collectors.toList()));
    }

    private void melanger() {
        List<Track> copie = new ArrayList<>(state.getTracks());
        Collections.shuffle(copie);
        selectionAleatoire = List.copyOf(copie.subList(0, Math.min(TAILLE_SELECTION, copie.size())));
    }
}
