package application;

import javafx.application.Application;
import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import org.kordamp.bootstrapfx.BootstrapFX;
import utils.Paths;
import utils.updates.UpdateService;

import java.awt.*;
import java.io.InputStream;

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
        configurarIconoAplicacion(primaryStage);
        primaryStage.show();

        // Verificar actualizaciones disponibles de forma asincrónica
        Thread updateThread = new Thread(UpdateService::notificarActualizacionSiExiste);
        updateThread.setDaemon(true);
        updateThread.start();
    }

    public static void main(String[] args) {
        launch(args);
    }

    private void configurarIconoAplicacion(Stage stage) {
        Image icono = cargarIcono("/images/iconApp.png");
        if (icono == null) {
            icono = cargarIcono("/images/iconLibro.png");
        }
        if (icono == null) {
            icono = cargarIcono("/images/escudo.png");
        }

        if (icono == null) {
            return;
        }

        stage.getIcons().add(icono);

        // En Windows ayuda a que la barra de tareas use el icono de la app y no el de Java.
        try {
            if (Taskbar.isTaskbarSupported()) {
                Taskbar taskbar = Taskbar.getTaskbar();
                if (taskbar.isSupported(Taskbar.Feature.ICON_IMAGE)) {
                    taskbar.setIconImage(SwingFXUtils.fromFXImage(icono, null));
                }
            }
        } catch (UnsupportedOperationException | SecurityException ignored) {
        }
    }

    private Image cargarIcono(String resourcePath) {
        try (InputStream iconStream = getClass().getResourceAsStream(resourcePath)) {
            if (iconStream == null) {
                return null;
            }
            return new Image(iconStream);
        } catch (Exception e) {
            return null;
        }
    }
}
