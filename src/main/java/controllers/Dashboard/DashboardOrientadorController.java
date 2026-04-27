package controllers.Dashboard;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import utils.Alertas;
import utils.ManagerView;
import utils.Paths;

public class DashboardOrientadorController {

    @FXML
    private Button btnBibliotecaVirtual;

    @FXML
    private Button btnConsulta;

    @FXML
    private Button btnInicio;

    @FXML
    private Button btnPrestamos;

    @FXML
    private BorderPane contenedor;

    @FXML
    private AnchorPane contenedorPrincipal;

    @FXML
    void clickConsultas(ActionEvent event) {
        ManagerView.cargarCentro(contenedor, Paths.CONSULTAS);
    }

    @FXML
    void clickInicio(ActionEvent event) {
        ManagerView.cargarCentro(contenedor, Paths.INICIO);
    }

    @FXML
    void clickRegistrarFalta(ActionEvent event) {
        ManagerView.cargarCentro(contenedor, Paths.REGISTRAR_FALTA);
    }

    @FXML
    void clickSalir(ActionEvent event) {
        if(Alertas.mostrarConfirmacion("¿Estás seguro que deseas cerrar sesión?")){
            ManagerView.cargarVista(contenedorPrincipal, Paths.LOGIN);
        }
    }

    @FXML
    void initialize() {
        ManagerView.cargarCentro(contenedor, Paths.INICIO);
    }

}
