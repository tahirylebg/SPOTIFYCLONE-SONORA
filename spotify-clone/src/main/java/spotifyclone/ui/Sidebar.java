package spotifyclone.ui;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

public class Sidebar extends VBox {

    public Sidebar() {
        getStyleClass().add("sidebar");
        setPrefWidth(260);
        setMinWidth(220);
        setSpacing(4);

        HBox brand = new HBox(10);
        brand.setAlignment(Pos.CENTER_LEFT);
        brand.getStyleClass().add("brand");
        Label logo = new Label("♪");
        logo.getStyleClass().add("brand-logo");
        Label name = new Label("Sonora");
        name.getStyleClass().add("brand-name");
        brand.getChildren().addAll(logo, name);

        VBox nav = new VBox(2);
        nav.getStyleClass().add("nav-group");
        nav.getChildren().addAll(
                navItem("🏠", "Accueil", true),
                navItem("🔍", "Rechercher", false),
                navItem("📖", "Bibliothèque", false)
        );

        Separator sep = new Separator();
        sep.getStyleClass().add("nav-separator");

        Label playlistsLabel = new Label("PLAYLISTS");
        playlistsLabel.getStyleClass().add("section-title");
        VBox.setMargin(playlistsLabel, new Insets(18, 0, 4, 14));

        VBox playlists = new VBox(2);
        playlists.getChildren().addAll(
                playlistItem("Titres likés"),
                playlistItem("Ma bibliothèque"),
                playlistItem("Découvertes")
        );

        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);

        getChildren().addAll(brand, nav, sep, playlistsLabel, playlists, spacer);
    }

    private Button navItem(String icon, String text, boolean active) {
        Button btn = new Button(icon + "   " + text);
        btn.getStyleClass().add("nav-item");
        if (active) {
            btn.getStyleClass().add("nav-item-active");
        }
        btn.setFocusTraversable(false);
        btn.setMaxWidth(Double.MAX_VALUE);
        btn.setAlignment(Pos.CENTER_LEFT);
        return btn;
    }

    private Button playlistItem(String text) {
        Button btn = new Button(text);
        btn.getStyleClass().add("playlist-item");
        btn.setFocusTraversable(false);
        btn.setMaxWidth(Double.MAX_VALUE);
        btn.setAlignment(Pos.CENTER_LEFT);
        return btn;
    }
}
