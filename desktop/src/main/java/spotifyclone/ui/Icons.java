package spotifyclone.ui;

import javafx.scene.layout.Region;
import javafx.scene.shape.SVGPath;

/**
 * Icônes vectorielles (paths Material 24x24) rendues comme des Region dont la
 * couleur se règle en CSS via -fx-background-color sur la classe "icon".
 */
public final class Icons {

    // Deux points quasi invisibles aux coins forcent une boîte 24x24 :
    // sans eux, -fx-scale-shape étirerait les icônes non carrées.
    private static final String CADRE = "M0 0h.01v.01H0z M23.99 23.99h.01v.01h-.01z ";

    private static final String PLAY = "M8 5v14l11-7z";
    private static final String PAUSE = "M6 19h4V5H6v14zm8-14v14h4V5h-4z";
    private static final String NEXT = "M6 18l8.5-6L6 6v12zM16 6v12h2V6h-2z";
    private static final String PREVIOUS = "M6 6h2v12H6zm3.5 6l8.5 6V6z";
    private static final String SHUFFLE = "M10.59 9.17L5.41 4 4 5.41l5.17 5.17 1.42-1.41zM14.5 4l2.04 2.04L4 18.59 5.41 20 17.96 7.46 20 9.5V4h-5.5zm.33 9.41l-1.41 1.41 3.13 3.13L14.5 20H20v-5.5l-2.04 2.04-3.13-3.13z";
    private static final String REPEAT = "M7 7h10v3l4-4-4-4v3H5v6h2V7zm10 10H7v-3l-4 4 4 4v-3h12v-6h-2v4z";
    private static final String REPEAT_ONE = "M7 7h10v3l4-4-4-4v3H5v6h2V7zm10 10H7v-3l-4 4 4 4v-3h12v-6h-2v4zm-4-2V9h-1l-2 1v1h1.5v4H13z";
    private static final String VOLUME = "M3 9v6h4l5 5V4L7 9H3zm13.5 3c0-1.77-1.02-3.29-2.5-4.03v8.05c1.48-.73 2.5-2.25 2.5-4.02zM14 3.23v2.06c2.89.86 5 3.54 5 6.71s-2.11 5.85-5 6.71v2.06c4.01-.91 7-4.49 7-8.77s-2.99-7.86-7-8.77z";
    private static final String VOLUME_MUTE = "M16.5 12c0-1.77-1.02-3.29-2.5-4.03v2.21l2.45 2.45c.03-.2.05-.41.05-.63zm2.5 0c0 .94-.2 1.82-.54 2.64l1.51 1.51C20.63 14.91 21 13.5 21 12c0-4.28-2.99-7.86-7-8.77v2.06c2.89.86 5 3.54 5 6.71zM4.27 3L3 4.27 7.73 9H3v6h4l5 5v-6.73l4.25 4.25c-.67.52-1.42.93-2.25 1.18v2.06c1.38-.31 2.63-.95 3.69-1.81L19.73 21 21 19.73l-9-9L4.27 3zM12 4L9.91 6.09 12 8.18V4z";
    private static final String HOME = "M10 20v-6h4v6h5v-8h3L12 3 2 12h3v8z";
    private static final String SEARCH = "M15.5 14h-.79l-.28-.27C15.41 12.59 16 11.11 16 9.5 16 5.91 13.09 3 9.5 3S3 5.91 3 9.5 5.91 16 9.5 16c1.61 0 3.09-.59 4.23-1.57l.27.28v.79l5 4.99L20.49 19l-4.99-5zm-6 0C7.01 14 5 11.99 5 9.5S7.01 5 9.5 5 14 7.01 14 9.5 11.99 14 9.5 14z";
    private static final String LIBRARY = "M4 6H2v14c0 1.1.9 2 2 2h14v-2H4V6zm16-4H8c-1.1 0-2 .9-2 2v12c0 1.1.9 2 2 2h12c1.1 0 2-.9 2-2V4c0-1.1-.9-2-2-2zm-1 9H9V9h10v2zm-4 4H9v-2h6v2zm4-8H9V5h10v2z";
    private static final String MUSIC = "M12 3v10.55c-.59-.34-1.27-.55-2-.55-2.21 0-4 1.79-4 4s1.79 4 4 4 4-1.79 4-4V7h4V3h-6z";
    private static final String EQUALIZER = "M10 20h4V4h-4v16zm-6 0h4v-8H4v8zM16 9v11h4V9h-4z";
    private static final String EXPLORE = "M12 10.9c-.61 0-1.1.49-1.1 1.1s.49 1.1 1.1 1.1c.61 0 1.1-.49 1.1-1.1s-.49-1.1-1.1-1.1zM12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm2.19 12.19L6 18l3.81-8.19L18 6l-3.81 8.19z";
    private static final String CLOCK = "M11.99 2C6.47 2 2 6.48 2 12s4.47 10 9.99 10C17.52 22 22 17.52 22 12S17.52 2 11.99 2zM12 20c-4.42 0-8-3.58-8-8s3.58-8 8-8 8 3.58 8 8-3.58 8-8 8zm.5-13H11v6l5.25 3.15.75-1.23-4.5-2.67z";

    private Icons() {
    }

    public static Region play(double taille) { return creer(PLAY, taille); }
    public static Region pause(double taille) { return creer(PAUSE, taille); }
    public static Region next(double taille) { return creer(NEXT, taille); }
    public static Region previous(double taille) { return creer(PREVIOUS, taille); }
    public static Region shuffle(double taille) { return creer(SHUFFLE, taille); }
    public static Region repeat(double taille) { return creer(REPEAT, taille); }
    public static Region repeatOne(double taille) { return creer(REPEAT_ONE, taille); }
    public static Region volume(double taille) { return creer(VOLUME, taille); }
    public static Region volumeMute(double taille) { return creer(VOLUME_MUTE, taille); }
    public static Region home(double taille) { return creer(HOME, taille); }
    public static Region search(double taille) { return creer(SEARCH, taille); }
    public static Region library(double taille) { return creer(LIBRARY, taille); }
    public static Region music(double taille) { return creer(MUSIC, taille); }
    public static Region equalizer(double taille) { return creer(EQUALIZER, taille); }
    public static Region clock(double taille) { return creer(CLOCK, taille); }
    public static Region explore(double taille) { return creer(EXPLORE, taille); }

    private static Region creer(String path, double taille) {
        SVGPath forme = new SVGPath();
        forme.setContent(CADRE + path);

        Region icone = new Region();
        icone.setShape(forme);
        icone.setScaleShape(true);
        icone.setCenterShape(true);
        icone.setMinSize(taille, taille);
        icone.setPrefSize(taille, taille);
        icone.setMaxSize(taille, taille);
        icone.getStyleClass().add("icon");
        return icone;
    }
}
