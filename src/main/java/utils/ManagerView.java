package utils;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;

import java.io.IOException;
import java.util.Objects;

public class ManagerView {

    /**
     * Carga una vista FXML dentro de un contenedor Pane.
     * Si el contenedor es un AnchorPane, ajusta la vista a los bordes.
     */
    public static void cargarVista(Pane contenedor, String fxml) {
        try {
            FXMLLoader loader = new FXMLLoader(Objects.requireNonNull(ManagerView.class.getResource(fxml)));
            Parent vista = loader.load();

            contenedor.getChildren().setAll(vista);

            if (contenedor instanceof AnchorPane) {
                AnchorPane.setTopAnchor(vista, 0.0);
                AnchorPane.setBottomAnchor(vista, 0.0);
                AnchorPane.setLeftAnchor(vista, 0.0);
                AnchorPane.setRightAnchor(vista, 0.0);
            }

        } catch (IOException e) {
            Alertas.mostrarError("Error al cargar la vista: " + e.getMessage());
            e.printStackTrace();
        } catch (NullPointerException e) {
            Alertas.mostrarError("No se encontró el archivo FXML: " + fxml);
        }
    }

    /**
     * Carga una vista FXML en la región central de un BorderPane.
     */
    public static void cargarCentro(BorderPane borderPane, String fxml) {
        try {
            FXMLLoader loader = new FXMLLoader(Objects.requireNonNull(ManagerView.class.getResource(fxml)));
            Parent vista = loader.load();

            borderPane.setCenter(vista);

        } catch (IOException e) {
            Alertas.mostrarError("Error al cargar vista central: " + e.getMessage());
            e.printStackTrace();
        } catch (NullPointerException e) {
            Alertas.mostrarError("No se encontró el archivo FXML: " + fxml);
        }
    }
}
