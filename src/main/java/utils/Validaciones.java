package utils;

import javafx.scene.control.Label;
import javafx.scene.control.TextInputControl;
import org.controlsfx.control.PopOver;

import java.util.Map;
import java.util.WeakHashMap;

public class Validaciones {

    // Usamos WeakHashMap para evitar fugas de memoria, los campos se limpian solos cuando se destruyen
    private static final Map<TextInputControl, PopOver> popOvers = new WeakHashMap<>();

    public static boolean campoRequerido(TextInputControl campo) {
        String texto = campo.getText().trim();
        if (texto.isEmpty()) {
            marcarInvalido(campo, "Este campo es obligatorio");
            return false;
        }

        if (texto.length() > 32) {
            marcarInvalido(campo, "No se admiten más de 32 caracteres");
            return false;
        }

        marcarValido(campo);
        return true;
    }

    public static boolean validarUsuario(TextInputControl campo) {
        String texto = campo.getText().trim();
        if (texto.length() < 4 || texto.length() > 32) {
            marcarInvalido(campo, "El usuario debe tener entre 4 y 32 caracteres");
            return false;
        }
        marcarValido(campo);
        return true;
    }

    public static boolean validarPassword(TextInputControl campo) {
        String texto = campo.getText().trim();
        if (texto.length() < 4 || texto.length() > 32) {
            marcarInvalido(campo, "La contraseña debe tener entre 4 y 32 caracteres");
            return false;
        }
        marcarValido(campo);
        return true;
    }

    private static void marcarInvalido(TextInputControl campo, String mensaje) {
        campo.getStyleClass().removeAll("is-valid", "is-invalid");
        campo.getStyleClass().add("is-invalid");
        mostrarPopOver(campo, mensaje);
    }

    private static void marcarValido(TextInputControl campo) {
        campo.getStyleClass().removeAll("is-valid", "is-invalid");
        campo.getStyleClass().add("is-valid");
        ocultarPopOver(campo);
    }

    private static void mostrarPopOver(TextInputControl campo, String texto) {
        PopOver popOver = popOvers.computeIfAbsent(campo, k -> {
            Label label = new Label(texto);
            label.setWrapText(true);
            label.setStyle("-fx-padding: 10; -fx-text-fill: white;");
            
            PopOver po = new PopOver(label);
            po.setArrowLocation(PopOver.ArrowLocation.LEFT_CENTER);
            po.setCornerRadius(10);
            po.setDetachable(false);
            po.setAutoHide(true);
            return po;
        });

        Label label = (Label) popOver.getContentNode();
        label.setText(texto);
        
        if (!popOver.isShowing()) {
            popOver.show(campo);
        }
    }

    public static void ocultarPopOver(TextInputControl campo) {
        PopOver popOver = popOvers.get(campo);
        if (popOver != null && popOver.isShowing()) {
            popOver.hide();
        }
    }

    public static void agregarPopOver(TextInputControl campo, String mensaje) {
        mostrarPopOver(campo, mensaje);
    }

    public static boolean validarCampoNumerico(TextInputControl campo) {
        try {
            Integer.parseInt(campo.getText().trim());
            marcarValido(campo);
            return true;
        } catch (NumberFormatException e) {
            marcarInvalido(campo, "Solo se admiten números");
            return false;
        }
    }
}
