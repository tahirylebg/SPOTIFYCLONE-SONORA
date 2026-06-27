package spotifyclone;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;
import spotifyclone.controller.MainController;
import spotifyclone.ui.MainView;

public class Main extends Application {

    @Override
    public void start(Stage stage) {
        MainView mainView = new MainView();
        MainController controller = new MainController(mainView);
        Scene scene = new Scene(mainView, 800, 600);
        var css = getClass().getResource("/styles/app.css");
        if (css != null) {
            scene.getStylesheets().add(css.toExternalForm());
        }
        stage.setTitle("Spotify Clone");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
