
package spotifyclone;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;
import spotifyclone.ui.MainView;

public class SpotifyApp extends Application {
    @Override
    public void start(Stage stage) {
        MainView mainView = new MainView();

        Scene scene = new Scene(mainView, 1000, 700);
        var css = getClass().getResource("/styles/app.css");
        if (css != null) {
            scene.getStylesheets().add(css.toExternalForm());
        }
        stage.setTitle("Spotify Clone");
        stage.setScene(scene);
        stage.show();
    }
}
