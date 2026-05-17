package controllers.Rectoria;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import utils.Alertas;
import utils.AppVersion;
import utils.updates.UpdateService;

public class AyudaController {

    @FXML
    private Label lblVersionActual;

    @FXML
    void initialize() {
        if (lblVersionActual != null) {
            lblVersionActual.setText("Versión actual: " + AppVersion.obtenerVersionActual());
        }
    }

    @FXML
    void clickBuscarActualizaciones() {
        UpdateService.buscarActualizacionManual();
    }

    @FXML
    void clickAcercaDe() {
        String version = AppVersion.obtenerVersionActual();
        String mensaje = "Disciplina v" + version + "\n\n" +
                "Sistema de gestión disciplinaria para instituciones educativas.\n\n" +
                "© 2026 - Todos los derechos reservados.";
        Alertas.mostrarInfo(mensaje);
    }
}

