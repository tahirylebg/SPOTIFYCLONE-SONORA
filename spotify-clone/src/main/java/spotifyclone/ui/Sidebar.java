package spotifyclone.ui;

import java.util.List;
import java.util.function.Consumer;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import spotifyclone.model.Track;

public class Sidebar extends VBox {

    private static final int NB_RECENTS = 8;

    private final Button homeButton = navItem(Icons.home(20), "Accueil");
    private final Button searchButton = navItem(Icons.search(20), "Rechercher");
    private final Button libraryButton = navItem(Icons.library(20), "Bibliothèque");
    private final Button discoverButton = navItem(Icons.explore(20), "Découvertes");
    private final List<Button> navItems = List.of(homeButton, searchButton, libraryButton, discoverButton);
    private final VBox recents = new VBox(2);

    public Sidebar() {
        getStyleClass().add("sidebar");
        setPrefWidth(240);
        setMinWidth(220);
        setSpacing(4);

        Region logo = Icons.music(22);
        logo.getStyleClass().add("brand-logo");
        Label name = new Label("Sonora");
        name.getStyleClass().add("brand-name");
        HBox brand = new HBox(10, logo, name);
        brand.setAlignment(Pos.CENTER_LEFT);
        brand.getStyleClass().add("brand");

        VBox nav = new VBox(2, homeButton, searchButton, libraryButton, discoverButton);
        activer(homeButton);

        Region separateur = new Region();
        separateur.getStyleClass().add("nav-divider");
        VBox.setMargin(separateur, new Insets(16, 8, 8, 8));

        Label recentsLabel = new Label("ÉCOUTÉS RÉCEMMENT");
        recentsLabel.getStyleClass().add("section-title");
        VBox.setMargin(recentsLabel, new Insets(10, 0, 8, 12));

        ScrollPane defilement = new ScrollPane(recents);
        defilement.getStyleClass().add("page");
        defilement.setFitToWidth(true);
        defilement.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        defilement.setFocusTraversable(false);
        VBox.setVgrow(defilement, Priority.ALWAYS);

        getChildren().addAll(brand, nav, separateur, recentsLabel, defilement);
    }

    public void activer(Button item) {
        navItems.forEach(b -> b.getStyleClass().remove("nav-item-active"));
        item.getStyleClass().add("nav-item-active");
    }

    /** Affiche les derniers morceaux écoutés ; un double-clic en relance un. */
    public void afficherRecents(List<Track> historique, Consumer<Track> jouer) {
        recents.getChildren().clear();
        if (historique.isEmpty()) {
            Label vide = new Label("Tes écoutes apparaîtront ici.");
            vide.getStyleClass().add("recent-empty");
            vide.setWrapText(true);
            recents.getChildren().add(vide);
            return;
        }
        historique.stream().limit(NB_RECENTS).forEach(track -> recents.getChildren().add(itemRecent(track, jouer)));
    }

    private static HBox itemRecent(Track track, Consumer<Track> jouer) {
        Cover cover = new Cover(40, 4);
        cover.afficher(track);
        Label titre = new Label();
        titre.textProperty().bind(track.titreProperty());
        titre.getStyleClass().add("recent-title");
        titre.setMinWidth(0);
        Label artiste = new Label();
        artiste.textProperty().bind(track.artisteProperty());
        artiste.getStyleClass().add("recent-artist");
        artiste.setMinWidth(0);
        VBox texte = new VBox(2, titre, artiste);
        texte.setMinWidth(0);

        HBox item = new HBox(12, cover, texte);
        item.setAlignment(Pos.CENTER_LEFT);
        item.getStyleClass().add("recent-item");
        item.setOnMouseClicked(e -> {
            if (e.getClickCount() == 2) {
                jouer.accept(track);
            }
        });
        return item;
    }

    public Button getHomeButton() { return homeButton; }
    public Button getSearchButton() { return searchButton; }
    public Button getLibraryButton() { return libraryButton; }
    public Button getDiscoverButton() { return discoverButton; }

    private static Button navItem(Region icone, String text) {
        Button btn = new Button(text, icone);
        btn.getStyleClass().add("nav-item");
        btn.setFocusTraversable(false);
        btn.setMaxWidth(Double.MAX_VALUE);
        btn.setAlignment(Pos.CENTER_LEFT);
        return btn;
    }
}
