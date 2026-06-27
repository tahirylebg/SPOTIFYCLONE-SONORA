package spotifyclone.ui;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import spotifyclone.model.Track;

public class MainView extends BorderPane {
    private ListView<Track> trackList;
    private PlayerBar playerBar;
    private Sidebar sidebar;
    private TextField searchField;

    public MainView() {
        getStyleClass().add("app-root");

        sidebar = new Sidebar();
        setLeft(sidebar);

        VBox content = new VBox();
        content.getStyleClass().add("content-pane");

        HBox header = buildHeader();

        trackList = new ListView<>();
        trackList.getStyleClass().add("track-list");
        trackList.setCellFactory(lv -> new TrackCell());
        VBox.setVgrow(trackList, Priority.ALWAYS);

        content.getChildren().addAll(header, trackList);
        setCenter(content);

        playerBar = new PlayerBar();
        setBottom(playerBar);
    }

    private HBox buildHeader() {
        HBox header = new HBox(16);
        header.getStyleClass().add("header-bar");
        header.setAlignment(Pos.CENTER_LEFT);
        header.setPadding(new Insets(20, 28, 16, 28));

        VBox titleBlock = new VBox(2);
        Label title = new Label("Tous les titres");
        title.getStyleClass().add("page-title");
        Label subtitle = new Label("Ta bibliothèque musicale locale");
        subtitle.getStyleClass().add("page-subtitle");
        titleBlock.getChildren().addAll(title, subtitle);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        searchField = new TextField();
        searchField.getStyleClass().add("search-field");
        searchField.setPromptText("🔍  Rechercher un titre, un artiste…");
        searchField.setPrefWidth(280);

        header.getChildren().addAll(titleBlock, spacer, searchField);
        return header;
    }

    public ListView<Track> getTrackList() {
        return trackList;
    }

    public PlayerBar getPlayerBar() {
        return playerBar;
    }

    public Sidebar getSidebar() {
        return sidebar;
    }

    public TextField getSearchField() {
        return searchField;
    }
}
