package controllers.Rectoria;

import database.EstudianteDAO;
import database.FaltaDAO;
import javafx.fxml.FXML;
import javafx.scene.control.Label;

import java.util.Map;

public class InicioRectorController {

    @FXML private Label lblTotalFaltas;
    @FXML private Label lblCasoMasComun;
    @FXML private Label lblLugarMasFaltas;
    @FXML private Label lblTotalEstudiantes;
    @FXML private Label lblEstudianteMasfaltas;
    @FXML private Label lblTipoFaltaMasFrecuente;

    private final FaltaDAO faltaDAO = new FaltaDAO();
    private final EstudianteDAO estudianteDAO = new EstudianteDAO();

    @FXML
    void initialize() {
        cargarEstadisticas();
    }

    private void cargarEstadisticas() {
        // Tarjeta 1: Total de faltas registradas
        int totalFaltas = faltaDAO.obtenerTotalFaltas();
        lblTotalFaltas.setText(String.valueOf(totalFaltas));

        // Tarjeta 2: Caso más común
        String casoMasComun = faltaDAO.obtenerCasoMasComun();
        lblCasoMasComun.setText(casoMasComun != null ? casoMasComun : "N/A");

        // Tarjeta 3: Lugar con más faltas
        String lugarMasFaltas = faltaDAO.obtenerLugarMasFaltas();
        lblLugarMasFaltas.setText(lugarMasFaltas != null ? lugarMasFaltas : "N/A");

        // Tarjeta 4: Total de estudiantes registrados
        int totalEstudiantes = estudianteDAO.obtenerTodos().size();
        lblTotalEstudiantes.setText(String.valueOf(totalEstudiantes));

        // Tarjeta 5: Estudiante con más faltas (top 1 del top10, sin filtro de fechas)
        Map<String, Integer> top = faltaDAO.obtenerTop10Estudiantes(null, null);
        if (top != null && !top.isEmpty()) {
            String primerEstudiante = top.entrySet().iterator().next().getKey();
            int cantidadFaltas = top.entrySet().iterator().next().getValue();
            lblEstudianteMasfaltas.setText(primerEstudiante + " (" + cantidadFaltas + ")");
        } else {
            lblEstudianteMasfaltas.setText("N/A");
        }

        // Tarjeta 6: Tipo de falta más frecuente (1=Leve, 2=Grave, 3=Gravísima)
        Integer tipoMasComun = faltaDAO.obtenerTipoFaltaMasComun();
        if (tipoMasComun != null) {
            String nombreTipo;
            switch (tipoMasComun) {
                case 1: nombreTipo = "Tipo 1"; break;
                case 2: nombreTipo = "Tipo 2"; break;
                case 3: nombreTipo = "Tipo 3"; break;
                default: nombreTipo = "Tipo " + tipoMasComun;
            }
            lblTipoFaltaMasFrecuente.setText(nombreTipo);
        } else {
            lblTipoFaltaMasFrecuente.setText("N/A");
        }
    }
}