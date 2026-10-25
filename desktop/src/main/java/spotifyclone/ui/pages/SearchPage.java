package spotifyclone.ui.pages;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

import javafx.css.PseudoClass;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Rectangle;
import spotifyclone.model.AppState;
import spotifyclone.model.Track;
import spotifyclone.ui.Actions;
import spotifyclone.ui.Card;
import spotifyclone.ui.Cover;
import spotifyclone.ui.Format;
import spotifyclone.ui.Icons;
import spotifyclone.ui.TrackTable;

public class SearchPage extends Page {

    private static final PseudoClass FOCUSED = PseudoClass.getPseudoClass("focused");

    // Couleurs des tuiles "Parcourir", attribuées de façon stable selon le nom de l'artiste.
    private static final String[] COULEURS = {
            "#e13300", "#1e3264", "#8d67ab", "#e8115b", "#148a08", "#bc5900", "#509bf5", "#af2896", "#27856a", "#477d95"
    };

    private final TextField champ = new TextField();
    private final HBox barreRecherche;
    private final VBox resultats = new VBox(36);

    public SearchPage(AppState state, Actions actions) {
        super(state, actions);

        champ.getStyleClass().add("search-field");
        champ.setPromptText("Que veux-tu écouter ?");
        HBox.setHgrow(champ, Priority.ALWAYS);
        barreRecherche = new HBox(12, Icons.search(20), champ);
        barreRecherche.getStyleClass().addAll("search-box", "search-box-large");
        barreRecherche.setAlignment(Pos.CENTER_LEFT);
        barreRecherche.setMaxWidth(560);
        champ.focusedProperty().addListener((obs, avant, focus) -> barreRecherche.pseudoClassStateChanged(FOCUSED, focus));
        champ.textProperty().addListener((obs, ancien, texte) -> majResultats());
    }

    public void rechercher(String requete) {
        champ.setText(requete);
        champ.positionCaret(requete.length());
    }

    public void focusChamp() {
        champ.requestFocus();
        champ.selectAll();
    }

    public void vider() {
        champ.clear();
    }

    @Override
    public void rafraichir() {
        contenu.getChildren().setAll(entete("Rechercher", null), barreRecherche, resultats);
        majResultats();
    }

    private void majResultats() {
        String requete = Format.normaliser(champ.getText());
        resultats.getChildren().clear();

        if (state.getTracks().isEmpty()) {
            resultats.getChildren().add(messageBibliothequeVide());
            return;
        }
        if (requete.isEmpty()) {
            resultats.getChildren().add(section("Parcourir tes artistes", grille(state.parArtiste().entrySet().stream()
                    .map(e -> tuileArtiste(e.getKey(), e.getValue()))
                    .collect(Collectors.toList()))));
            return;
        }

        List<Track> titres = state.getTracks().stream()
                .filter(t -> Format.normaliser(t.getTitre()).contains(requete)
                        || Format.normaliser(t.getArtiste()).contains(requete))
                .collect(Collectors.toList());
        Map<String, List<Track>> artistes = state.parArtiste().entrySet().stream()
                .filter(e -> Format.normaliser(e.getKey()).contains(requete))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (a, b) -> a, TreeMap::new));

        if (titres.isEmpty() && artistes.isEmpty()) {
            resultats.getChildren().add(etatVide("Aucun résultat pour « " + champ.getText().trim()
                    + " ».\nVérifie l'orthographe ou essaie un autre mot-clé."));
            return;
        }

        if (!titres.isEmpty()) {
            VBox meilleur = section("Meilleur résultat", meilleurResultat(titres.get(0), titres));
            meilleur.setMinWidth(320);
            meilleur.setPrefWidth(380);

            TrackTable table = new TrackTable(state, actions, false);
            table.setTracks(titres.stream().limit(5).collect(Collectors.toList()));
            VBox listeTitres = section("Titres", table);
            HBox.setHgrow(listeTitres, Priority.ALWAYS);

            HBox haut = new HBox(24, meilleur, listeTitres);
            resultats.getChildren().add(haut);
        }

        if (!artistes.isEmpty()) {
            resultats.getChildren().add(section("Artistes", grille(artistes.entrySet().stream()
                    .map(e -> Card.artiste(e.getKey(), e.getValue(),
                            () -> rechercher(e.getKey()),
                            () -> actions.jouer(e.getValue().get(0), e.getValue())))
                    .collect(Collectors.toList()))));
        }
    }

    private Node meilleurResultat(Track track, List<Track> contexte) {
        Cover cover = new Cover(92, 6);
        cover.afficher(track);

        Label titre = new Label();
        titre.textProperty().bind(track.titreProperty());
        titre.getStyleClass().add("best-title");
        Label artiste = new Label();
        artiste.textProperty().bind(track.artisteProperty());
        artiste.getStyleClass().add("best-artist");
        Label type = new Label("Titre");
        type.getStyleClass().add("tag");
        HBox meta = new HBox(10, artiste, type);
        meta.setAlignment(Pos.CENTER_LEFT);

        VBox carte = new VBox(18, cover, new VBox(6, titre, meta));
        carte.getStyleClass().add("best-result");

        Button play = Card.boutonPlay(48, () -> actions.jouer(track, contexte));
        StackPane.setAlignment(play, Pos.BOTTOM_RIGHT);
        StackPane.setMargin(play, new Insets(0, 20, 20, 0));
        StackPane conteneur = new StackPane(carte, play);
        conteneur.getStyleClass().add("best-result-wrap");
        conteneur.setOnMouseClicked(e -> {
            if (e.getClickCount() == 2) {
                actions.jouer(track, contexte);
            }
        });
        return conteneur;
    }

    /** Tuile colorée façon "Parcourir" : nom en haut à gauche, pochette inclinée en bas à droite. */
    private Node tuileArtiste(String nom, List<Track> titres) {
        Label label = new Label(nom);
        label.getStyleClass().add("browse-title");
        label.setWrapText(true);
        label.setMaxWidth(120);
        StackPane.setAlignment(label, Pos.TOP_LEFT);
        StackPane.setMargin(label, new Insets(14));

        Cover cover = new Cover(84, 4);
        cover.afficherArtiste(nom, titres);
        cover.setRotate(25);
        cover.getStyleClass().add("browse-cover");
        StackPane.setAlignment(cover, Pos.BOTTOM_RIGHT);
        cover.setTranslateX(18);
        cover.setTranslateY(6);

        StackPane tuile = new StackPane(label, cover);
        tuile.getStyleClass().add("browse-tile");
        tuile.setStyle("-fx-background-color: " + COULEURS[Math.floorMod(nom.hashCode(), COULEURS.length)] + ";");
        tuile.setMinSize(200, 120);
        tuile.setPrefSize(200, 120);
        tuile.setMaxSize(200, 120);
        Rectangle clip = new Rectangle(200, 120);
        clip.setArcWidth(16);
        clip.setArcHeight(16);
        tuile.setClip(clip);
        tuile.setOnMouseClicked(e -> rechercher(nom));
        return tuile;
    }
}
