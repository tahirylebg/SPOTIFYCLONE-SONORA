package spotifyclone.controller;

import java.nio.file.Path;
import java.util.List;

import javafx.collections.ListChangeListener;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextInputControl;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import spotifyclone.model.AppState;
import spotifyclone.model.Track;
import spotifyclone.service.HistoryService;
import spotifyclone.service.LibraryService;
import spotifyclone.ui.Actions;
import spotifyclone.ui.MainView;
import spotifyclone.ui.Sidebar;
import spotifyclone.ui.pages.DiscoverPage;
import spotifyclone.ui.pages.HomePage;
import spotifyclone.ui.pages.LibraryPage;
import spotifyclone.ui.pages.Page;
import spotifyclone.ui.pages.SearchPage;

/** Charge la bibliothèque, relie les pages entre elles et gère la navigation. */
public class MainController {

    private final MainView mainView;
    private final Sidebar sidebar;
    private final AppState state = new AppState();
    private final LibraryService libraryService = new LibraryService();
    private final HistoryService historyService = new HistoryService();
    private final PlayerController player;
    private final HomePage homePage;
    private final SearchPage searchPage;
    private final LibraryPage libraryPage;
    private final DiscoverPage discoverPage;
    private Page pageCourante;

    public MainController(MainView mainView) {
        this.mainView = mainView;
        this.sidebar = mainView.getSidebar();
        this.player = new PlayerController(mainView.getPlayerBar(), state);

        Actions actions = new Actions() {
            @Override
            public void jouer(Track track, List<Track> contexte) {
                player.jouer(track, contexte);
            }

            @Override
            public void jouerAleatoire(List<Track> tracks) {
                player.jouerAleatoire(tracks);
            }

            @Override
            public void ouvrirArtiste(String artiste) {
                ouvrirRecherche(artiste);
            }
        };
        homePage = new HomePage(state, actions);
        searchPage = new SearchPage(state, actions);
        libraryPage = new LibraryPage(state, actions);
        discoverPage = new DiscoverPage(state, actions);

        chargerBibliotheque();
        initialiserNavigation();
        naviguer(homePage, sidebar.getHomeButton());
    }

    private void chargerBibliotheque() {
        Path dossier = libraryService.trouverDossierMusique();
        state.setDossierMusique(dossier);
        state.getTracks().setAll(libraryService.chargerBibliotheque(dossier));
        state.chargerEcoutes(historyService.charger());

        // Chaque écoute : on sauvegarde, et l'accueil / la sidebar se mettent à jour.
        state.getHistorique().addListener((ListChangeListener<Track>) c -> {
            historyService.sauvegarder(state.getToutesEcoutes());
            majRecents();
            if (pageCourante != null && pageCourante != searchPage) {
                pageCourante.rafraichirSurPlace();
            }
        });
        majRecents();

        // Les tags ID3 peuvent changer les artistes : on reconstruit la page une fois l'analyse finie.
        libraryService.chargerMetadonnees(state.getTracks(), () -> {
            if (pageCourante != null) {
                pageCourante.rafraichirSurPlace();
            }
        });
    }

    private void majRecents() {
        sidebar.afficherRecents(state.getHistorique(), track -> player.jouer(track, state.getHistorique()));
    }

    private void initialiserNavigation() {
        sidebar.getHomeButton().setOnAction(e -> naviguer(homePage, sidebar.getHomeButton()));
        sidebar.getSearchButton().setOnAction(e -> {
            naviguer(searchPage, sidebar.getSearchButton());
            searchPage.focusChamp();
        });
        sidebar.getLibraryButton().setOnAction(e -> naviguer(libraryPage, sidebar.getLibraryButton()));
        sidebar.getDiscoverButton().setOnAction(e -> naviguer(discoverPage, sidebar.getDiscoverButton()));
    }

    private void naviguer(Page page, Button itemSidebar) {
        sidebar.activer(itemSidebar);
        pageCourante = page;
        page.rafraichir();
        page.setVvalue(0);
        mainView.afficherPage(page);
    }

    private void ouvrirRecherche(String requete) {
        if (pageCourante != searchPage) {
            naviguer(searchPage, sidebar.getSearchButton());
        }
        searchPage.rechercher(requete);
    }

    /** Raccourcis : ceux du lecteur, plus Ctrl+F (recherche) et Échap (vider la recherche). */
    public void installerRaccourcis(Scene scene) {
        player.installerRaccourcis(scene);
        scene.addEventFilter(KeyEvent.KEY_PRESSED, e -> {
            if (e.isShortcutDown() && e.getCode() == KeyCode.F) {
                if (pageCourante != searchPage) {
                    naviguer(searchPage, sidebar.getSearchButton());
                }
                searchPage.focusChamp();
                e.consume();
            } else if (e.getCode() == KeyCode.ESCAPE && scene.getFocusOwner() instanceof TextInputControl) {
                searchPage.vider();
                mainView.requestFocus();
                e.consume();
            }
        });
    }
}
