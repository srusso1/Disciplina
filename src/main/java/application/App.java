package application;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.kordamp.bootstrapfx.BootstrapFX;
import utils.Paths;

public class App extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        Parent root = FXMLLoader.load(getClass().getResource(Paths.LOGIN));
        Scene scene = new Scene(root);

        // Cargar BootstrapFX
        scene.getStylesheets().add(BootstrapFX.bootstrapFXStylesheet());

        // Cargar CSS global
        String style = getClass().getResource(Paths.STYLE).toExternalForm();
        if (style != null) {
            scene.getStylesheets().add(style);
        }

        primaryStage.setTitle("Disciplina+");
        primaryStage.setScene(scene);
        primaryStage.setMaximized(true);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
