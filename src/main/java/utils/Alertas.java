package utils;

import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;

import java.util.Optional;

public class Alertas {

    public static void mostrarAlerta(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public static void mostrarError(String message) {
        mostrarAlerta(Alert.AlertType.ERROR, "Error", message);
    }

    public static void mostrarInfo(String message) {
        mostrarAlerta(Alert.AlertType.INFORMATION, "Información", message);
    }

    public static void mostrarWarning(String message) {
        mostrarAlerta(Alert.AlertType.WARNING, "Advertencia", message);
    }

    public static void mostrarExito(String message) {
        mostrarAlerta(Alert.AlertType.INFORMATION, "Éxito", message);
    }

    public static boolean mostrarConfirmacion(String mensaje) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmar acción");
        confirm.setHeaderText(null);
        confirm.setContentText(mensaje);

        Optional<ButtonType> resultado = confirm.showAndWait();

        return resultado.isPresent() && resultado.get() == ButtonType.OK;
    }
}
