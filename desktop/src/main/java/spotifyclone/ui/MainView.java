package spotifyclone.ui;

import javafx.scene.Node;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;

/** Squelette de la fenêtre : sidebar à gauche, page courante au centre, lecteur en bas. */
public class MainView extends BorderPane {

    private final PlayerBar playerBar = new PlayerBar();
    private final Sidebar sidebar = new Sidebar();
    private final StackPane pageHost = new StackPane();

    public MainView() {
        getStyleClass().add("app-root");
        pageHost.getStyleClass().add("content-pane");

        setLeft(sidebar);
        setCenter(pageHost);
        setBottom(playerBar);
    }

    public void afficherPage(Node page) {
        pageHost.getChildren().setAll(page);
    }

    public PlayerBar getPlayerBar() { return playerBar; }
    public Sidebar getSidebar() { return sidebar; }
}
