package controllers.Login;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import models.Usuario;
import database.UsuarioDAO;
import utils.Alertas;
import utils.ManagerView;
import utils.Paths;
import utils.Validaciones;

public class LoginController {

    @FXML
    private Button btnIngresar;

    @FXML
    private AnchorPane contenedor;

    @FXML
    private PasswordField txtPassword;

    @FXML
    private TextField txtUsuario;

    private final UsuarioDAO usuarioDAO = new UsuarioDAO();

    @FXML
    void clickIngresar(ActionEvent event) {
        ingresar();
    }

    private void ingresar() {
        if (!Validaciones.campoRequerido(txtUsuario)) {
            return;
        }

        if (!Validaciones.validarUsuario(txtUsuario)) {
            return;
        }

        if (!Validaciones.campoRequerido(txtPassword)) {
            return;
        }

        if (!Validaciones.validarPassword(txtPassword)) {
            return;
        }

        String userStr = txtUsuario.getText().trim();
        String passStr = txtPassword.getText().trim();

        Usuario usuario = usuarioDAO.validarUsuario(userStr, passStr);

        if (usuario == null) {
            Alertas.mostrarError("ERROR: Usuario no encontrado o credenciales incorrectas");
            return;
        }

        Alertas.mostrarExito("Bienvenido " + usuario.getRol() + " " + usuario.getUsername());

        if (usuario.getRol().equals("RECTOR")) {
            ManagerView.cargarVista(contenedor, Paths.DASHBOARD_RECTOR);
        } else if (usuario.getRol().equals("ORIENTADOR")) {
            ManagerView.cargarVista(contenedor, Paths.DASHBOARD_ORIENTADOR);
        }
    }

    @FXML
    void initialize() {
    }
}
