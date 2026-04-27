package controllers.Dashboard;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import utils.Alertas;
import utils.ManagerView;
import utils.Paths;

public class DashboardRectorController {

    @FXML
    private Button btnAyuda;

    @FXML
    private Button btnConfig;

    @FXML
    private Button btnConsulta;

    @FXML
    private Button btnInformes;

    @FXML
    private Button btnInicio;

    @FXML
    private Button btnPrestamos;

    @FXML
    private BorderPane contenedor;

    @FXML
    private AnchorPane contenedorPrincipal;

    @FXML
    void clickAyuda(ActionEvent event) {

    }

    @FXML
    void clickConfig(ActionEvent event) {

    }

    @FXML
    void clickEstadisticas(ActionEvent event) {
        ManagerView.cargarCentro(contenedor, Paths.ESTADISTICAS);
    }

    @FXML
    void clickInformes(ActionEvent event) {

    }

    @FXML
    void clickInicio(ActionEvent event) {

    }

    @FXML
    void clickInventario(ActionEvent event) {

    }

    @FXML
    void clickSalir(ActionEvent event) {
        if(Alertas.mostrarConfirmacion("¿Estás seguro que deseas cerrar sesión?")){
            ManagerView.cargarVista(contenedorPrincipal, Paths.LOGIN);
        }
    }

}
