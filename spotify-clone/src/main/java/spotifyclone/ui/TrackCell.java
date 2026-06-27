package spotifyclone.ui;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.util.Duration;
import spotifyclone.model.Track;

public class TrackCell extends ListCell<Track> {

    private final StackPane cover = new StackPane();
    private final Label coverGlyph = new Label();
    private final Label titleLabel = new Label();
    private final Label metaLabel = new Label();
    private final VBox textBlock = new VBox(titleLabel, metaLabel);
    private final Label durationLabel = new Label();
    private final HBox container = new HBox(14);

    public TrackCell() {
        cover.getStyleClass().add("track-cover");
        cover.getChildren().add(coverGlyph);
        cover.setMinSize(40, 40);
        cover.setMaxSize(40, 40);

        titleLabel.getStyleClass().add("track-title");
        metaLabel.getStyleClass().add("track-meta");
        textBlock.setSpacing(2);
        HBox.setHgrow(textBlock, Priority.ALWAYS);

        durationLabel.getStyleClass().add("track-duration");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        container.setAlignment(Pos.CENTER_LEFT);
        container.setPadding(new Insets(8, 16, 8, 16));
        container.getChildren().addAll(cover, textBlock, spacer, durationLabel);
        container.getStyleClass().add("track-row");
    }

    @Override
    protected void updateItem(Track track, boolean empty) {
        super.updateItem(track, empty);

        if (empty || track == null) {
            setGraphic(null);
            setText(null);
            return;
        }

        String titre = nullToEmpty(track.getTitre());
        titleLabel.setText(titre);
        metaLabel.setText(nullToEmpty(track.getArtiste()));
        coverGlyph.setText(titre.isBlank() ? "♪" : titre.substring(0, 1).toUpperCase());
        durationLabel.setText(formatDuration(track.getDuree()));
        setText(null);
        setGraphic(container);
    }

    private static String formatDuration(Duration duration) {
        if (duration == null || duration.isUnknown() || duration.isIndefinite()) {
            return "";
        }
        int totalSeconds = (int) Math.max(0, Math.floor(duration.toSeconds()));
        int minutes = totalSeconds / 60;
        int seconds = totalSeconds % 60;
        return String.format("%d:%02d", minutes, seconds);
    }

    private static String nullToEmpty(String value) {
        return value == null ? "" : value;
    }
}
