package spotifyclone.ui;

import java.util.List;
import java.util.Locale;
import java.util.Objects;

import javafx.beans.binding.Bindings;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Rectangle2D;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.PixelReader;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.Rectangle;
import spotifyclone.model.Track;

/**
 * Pochette carrée (ou ronde pour un artiste) : l'image ID3 si elle existe,
 * recadrée au centre, sinon un dégradé avec une initiale.
 */
public class Cover extends StackPane {

    private static final double SEUIL_SOMBRE = 0.08;

    private final double taille;
    private final ImageView image = new ImageView();
    private final Label initiale = new Label();
    private final Region note;

    public Cover(double taille, double rayon) {
        this.taille = taille;
        getStyleClass().add("cover");
        setMinSize(taille, taille);
        setPrefSize(taille, taille);
        setMaxSize(taille, taille);

        note = Icons.music(taille * 0.42);
        note.getStyleClass().add("cover-glyph");

        initiale.getStyleClass().add("cover-initial");
        initiale.setStyle(String.format(Locale.ROOT, "-fx-font-size: %.1fpx;", taille * 0.38));

        image.setFitWidth(taille);
        image.setFitHeight(taille);
        image.setSmooth(true);
        Rectangle clip = new Rectangle(taille, taille);
        clip.setArcWidth(rayon * 2);
        clip.setArcHeight(rayon * 2);
        image.setClip(clip);
        image.imageProperty().addListener((obs, ancienne, nouvelle) -> recadrer(nouvelle));

        getChildren().addAll(note, initiale, image);
        afficher(null);
    }

    /** Pochette ronde, pour les artistes. */
    public static Cover ronde(double taille) {
        Cover cover = new Cover(taille, taille / 2);
        cover.getStyleClass().add("cover-round");
        return cover;
    }

    public void afficher(Track track) {
        if (track == null) {
            afficher(null, null);
            return;
        }
        afficher(track.pochetteProperty(),
                Bindings.createStringBinding(() -> premiereLettre(track.getTitre()), track.titreProperty()));
    }

    /** Un artiste prend la première pochette disponible parmi ses morceaux. */
    public void afficherArtiste(String artiste, List<Track> titres) {
        ObservableValue<Image> pochette = Bindings.createObjectBinding(
                () -> titres.stream().map(Track::getPochette).filter(Objects::nonNull).findFirst().orElse(null),
                titres.stream().map(Track::pochetteProperty).toArray(ObservableValue[]::new));
        afficher(pochette, Bindings.createStringBinding(() -> premiereLettre(artiste)));
    }

    private void afficher(ObservableValue<Image> pochette, ObservableValue<String> lettre) {
        image.imageProperty().unbind();
        initiale.textProperty().unbind();
        initiale.visibleProperty().unbind();

        if (pochette == null) {
            image.setImage(null);
            initiale.setText("");
            note.setVisible(true);
            return;
        }

        note.setVisible(false);
        image.imageProperty().bind(pochette);
        initiale.textProperty().bind(lettre);
        initiale.visibleProperty().bind(Bindings.createBooleanBinding(() -> pochette.getValue() == null, pochette));
    }

    private void recadrer(Image img) {
        if (img == null || img.getWidth() <= 0 || img.getHeight() <= 0) {
            image.setViewport(null);
            return;
        }
        Rectangle2D zone = zoneUtile(img);
        double cote = Math.min(zone.getWidth(), zone.getHeight());
        image.setViewport(new Rectangle2D(
                zone.getMinX() + (zone.getWidth() - cote) / 2,
                zone.getMinY() + (zone.getHeight() - cote) / 2,
                cote, cote));
        image.setFitWidth(taille);
        image.setFitHeight(taille);
    }

    /**
     * Zone de l'image sans les bandes noires (fréquentes sur les miniatures YouTube
     * enregistrées comme pochettes). Renvoie l'image entière si elle est sombre partout.
     */
    private static Rectangle2D zoneUtile(Image img) {
        int w = (int) img.getWidth();
        int h = (int) img.getHeight();
        Rectangle2D entiere = new Rectangle2D(0, 0, w, h);
        PixelReader pixels = img.getPixelReader();
        if (pixels == null) {
            return entiere;
        }

        int haut = 0, bas = h - 1, gauche = 0, droite = w - 1;
        while (haut < bas && ligneSombre(pixels, haut, w)) haut++;
        while (bas > haut && ligneSombre(pixels, bas, w)) bas--;
        while (gauche < droite && colonneSombre(pixels, gauche, haut, bas)) gauche++;
        while (droite > gauche && colonneSombre(pixels, droite, haut, bas)) droite--;

        int largeur = droite - gauche + 1;
        int hauteur = bas - haut + 1;
        if (largeur < w / 2 || hauteur < h / 2) {
            return entiere;
        }
        return new Rectangle2D(gauche, haut, largeur, hauteur);
    }

    private static boolean ligneSombre(PixelReader pixels, int y, int largeur) {
        int pas = Math.max(1, largeur / 32);
        for (int x = 0; x < largeur; x += pas) {
            if (pixels.getColor(x, y).getBrightness() > SEUIL_SOMBRE) {
                return false;
            }
        }
        return true;
    }

    private static boolean colonneSombre(PixelReader pixels, int x, int haut, int bas) {
        int pas = Math.max(1, (bas - haut) / 32);
        for (int y = haut; y <= bas; y += pas) {
            if (pixels.getColor(x, y).getBrightness() > SEUIL_SOMBRE) {
                return false;
            }
        }
        return true;
    }

    private static String premiereLettre(String texte) {
        return texte == null || texte.isBlank() ? "" : texte.substring(0, 1).toUpperCase();
    }
}
