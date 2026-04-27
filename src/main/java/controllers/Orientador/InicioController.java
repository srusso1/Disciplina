package controllers.Orientador;

import database.FaltaDAO;
import javafx.fxml.FXML;
import javafx.scene.control.Label;

public class InicioController {

    @FXML
    private Label lblTotalFaltas;

    @FXML
    private Label lblCasoMasComun;

    @FXML
    private Label lblLugarMasFaltas;

    private final FaltaDAO faltaDAO = new FaltaDAO();

    @FXML
    void initialize() {
        cargarEstadisticas();
    }

    private void cargarEstadisticas() {
        int totalFaltas = faltaDAO.obtenerTotalFaltas();
        lblTotalFaltas.setText(String.valueOf(totalFaltas));

        String casoMasComun = faltaDAO.obtenerCasoMasComun();
        lblCasoMasComun.setText(casoMasComun);

        String lugarMasFaltas = faltaDAO.obtenerLugarMasFaltas();
        lblLugarMasFaltas.setText(lugarMasFaltas);
    }
}


