package spotifyclone.ui.pages;

import java.util.List;

import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import spotifyclone.model.AppState;
import spotifyclone.ui.Actions;

/** Base des pages : un contenu vertical qui défile, reconstruit par {@link #rafraichir()}. */
public abstract class Page extends ScrollPane {

    protected final AppState state;
    protected final Actions actions;
    protected final VBox contenu = new VBox(36);

    protected Page(AppState state, Actions actions) {
        this.state = state;
        this.actions = actions;
        getStyleClass().add("page");
        contenu.getStyleClass().add("page-content");
        setContent(contenu);
        setFitToWidth(true);
        setHbarPolicy(ScrollBarPolicy.NEVER);
        setFocusTraversable(false);
    }

    /** Reconstruit la page à partir de l'état courant. */
    public abstract void rafraichir();

    /** Reconstruit la page sans la faire remonter en haut (ex. après une nouvelle écoute). */
    public void rafraichirSurPlace() {
        double defilement = getVvalue();
        rafraichir();
        applyCss();
        layout();
        setVvalue(defilement);
    }

    protected static VBox entete(String titre, String sousTitre) {
        Label t = new Label(titre);
        t.getStyleClass().add("page-title");
        VBox bloc = new VBox(6, t);
        if (sousTitre != null && !sousTitre.isEmpty()) {
            Label s = new Label(sousTitre);
            s.getStyleClass().add("page-subtitle");
            bloc.getChildren().add(s);
        }
        return bloc;
    }

    protected static VBox section(String titre, Node corps) {
        return section(titre, null, corps);
    }

    protected static VBox section(String titre, Node action, Node corps) {
        Label t = new Label(titre);
        t.getStyleClass().add("section-heading");
        HBox ligne = new HBox(t);
        ligne.setAlignment(Pos.CENTER_LEFT);
        if (action != null) {
            Region espace = new Region();
            HBox.setHgrow(espace, Priority.ALWAYS);
            ligne.getChildren().addAll(espace, action);
        }
        return new VBox(14, ligne, corps);
    }

    protected static FlowPane grille(List<? extends Node> elements) {
        FlowPane grille = new FlowPane(12, 16);
        grille.getChildren().setAll(elements);
        return grille;
    }

    protected static Label etatVide(String message) {
        Label label = new Label(message);
        label.getStyleClass().add("empty-state");
        label.setWrapText(true);
        return label;
    }

    protected Label messageBibliothequeVide() {
        return etatVide("Ta bibliothèque est vide.\nAjoute des fichiers .mp3 ou .wav dans :\n"
                + state.getDossierMusique());
    }
}
