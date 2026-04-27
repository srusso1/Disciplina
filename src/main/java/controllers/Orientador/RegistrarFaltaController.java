package controllers.Orientador;

import database.CasoDAO;
import database.DocenteDAO;
import database.EstudianteDAO;
import database.FaltaDAO;
import database.LugarDAO;
import database.models.Caso;
import database.models.Falta;
import database.models.Lugar;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import models.Docente;
import models.Estudiante;
import utils.Alertas;
import utils.BusquedaSugerencias;

import java.util.ArrayList;
import java.util.List;

public class RegistrarFaltaController {

    private static final int ID_LUGAR_AULA = 1;
    private static final int ID_DOCENTE_NO_APLICA = 65;

    @FXML
    private ComboBox<Caso> comboCaso;

    @FXML
    private ComboBox<Integer> comboFaltaTipo;

    @FXML
    private ComboBox<Lugar> comboLugar;

    @FXML
    private VBox hboxDocente;

    @FXML
    private TextField txtDocente;

    @FXML
    private Label lbFaltasPrevias;

    @FXML
    private Label lbGrado;

    @FXML
    private Label lbIdentificacion;

    @FXML
    private TextField txtAccionRestaurativa;

    @FXML
    private TextField txtDescargo;

    @FXML
    private TextField txtEstudiante;

    private final EstudianteDAO estudianteDAO = new EstudianteDAO();
    private final DocenteDAO docenteDAO = new DocenteDAO();
    private final FaltaDAO faltaDAO = new FaltaDAO();
    private final CasoDAO casoDAO = new CasoDAO();
    private final LugarDAO lugarDAO = new LugarDAO();

    private final List<Estudiante> cacheEstudiantes = new ArrayList<>();
    private final List<Docente> cacheDocentes = new ArrayList<>();
    private final List<Caso> cacheCasos = new ArrayList<>();
    private final List<Lugar> cacheLugares = new ArrayList<>();

    private final ContextMenu menuSugerenciasEstudiante = new ContextMenu();
    private final ContextMenu menuSugerenciasDocente = new ContextMenu();
    private Estudiante estudianteSeleccionado;
    private Docente docenteSeleccionado;

    @FXML
    void initialize() {
        inicializarVista();
        cargarDatosBase();
        configurarAutocompletadoEstudiante();
        configurarAutocompletadoDocente();
        configurarVisibilidadDocente();
    }

    private void inicializarVista() {
        txtEstudiante.setContextMenu(menuSugerenciasEstudiante);
        txtDocente.setContextMenu(menuSugerenciasDocente);
        limpiarDatosEstudiante();
        ocultarDocente();
    }

    private void cargarDatosBase() {
        cacheEstudiantes.clear();
        cacheEstudiantes.addAll(estudianteDAO.obtenerTodos());

        cacheDocentes.clear();
        cacheDocentes.addAll(docenteDAO.obtenerTodos());

        cacheCasos.clear();
        cacheCasos.addAll(casoDAO.obtenerTodos());
        comboCaso.setItems(FXCollections.observableArrayList(cacheCasos));

        cacheLugares.clear();
        cacheLugares.addAll(lugarDAO.obtenerTodos());
        comboLugar.setItems(FXCollections.observableArrayList(cacheLugares));

        comboFaltaTipo.setItems(FXCollections.observableArrayList(1, 2, 3));
    }

    private void configurarVisibilidadDocente() {
        comboLugar.valueProperty().addListener((obs, oldLugar, nuevoLugar) -> actualizarVisibilidadDocente(nuevoLugar));
        actualizarVisibilidadDocente(comboLugar.getValue());
    }

    private void actualizarVisibilidadDocente(Lugar lugar) {
        boolean mostrar = lugar != null && lugar.getId() == ID_LUGAR_AULA;
        hboxDocente.setVisible(mostrar);
        hboxDocente.setManaged(mostrar);

        if (!mostrar) {
            limpiarDatosDocente();
            menuSugerenciasDocente.hide();
        }
    }

    private void configurarAutocompletadoEstudiante() {
        BusquedaSugerencias.configurar(
                txtEstudiante,
                menuSugerenciasEstudiante,
                cacheEstudiantes,
                2,
                8,
                this::textoBusquedaEstudiante,
                this::textoMenuEstudiante,
                this::textoSeleccionEstudiante,
                this::seleccionarEstudiante,
                this::limpiarDatosEstudiante
        );

        txtEstudiante.textProperty().addListener((obs, oldText, newText) -> {
            if (estudianteSeleccionado == null) {
                return;
            }

            String textoActual = newText == null ? "" : newText.trim();
            if (!textoActual.equalsIgnoreCase(textoSeleccionEstudiante(estudianteSeleccionado))) {
                limpiarDatosEstudiante();
            }
        });
    }

    private void configurarAutocompletadoDocente() {
        BusquedaSugerencias.configurar(
                txtDocente,
                menuSugerenciasDocente,
                cacheDocentes,
                2,
                8,
                this::textoBusquedaDocente,
                this::textoMenuDocente,
                this::textoSeleccionDocente,
                this::seleccionarDocente,
                this::limpiarSeleccionDocente
        );

        txtDocente.textProperty().addListener((obs, oldText, newText) -> {
            if (docenteSeleccionado == null) {
                return;
            }

            String textoActual = newText == null ? "" : newText.trim();
            if (!textoActual.equalsIgnoreCase(textoSeleccionDocente(docenteSeleccionado))) {
                limpiarSeleccionDocente();
            }
        });
    }

    private String textoBusquedaEstudiante(Estudiante estudiante) {
        return textoSeleccionEstudiante(estudiante) + " " + estudiante.getIdentificacion();
    }

    private String textoMenuEstudiante(Estudiante estudiante) {
        return textoSeleccionEstudiante(estudiante)
                + " | ID: " + estudiante.getIdentificacion()
                + " | Grado: " + estudiante.getGrado();
    }

    private String textoSeleccionEstudiante(Estudiante estudiante) {
        return unirNombre(estudiante.getNombre1(), estudiante.getNombre2(), estudiante.getApellido1(), estudiante.getApellido2());
    }

    private String textoBusquedaDocente(Docente docente) {
        return textoSeleccionDocente(docente);
    }

    private String textoMenuDocente(Docente docente) {
        return textoSeleccionDocente(docente);
    }

    private String textoSeleccionDocente(Docente docente) {
        return unirNombre(docente.getNombre1(), docente.getNombre2(), docente.getApellido1(), docente.getApellido2());
    }

    private void seleccionarEstudiante(Estudiante estudiante) {
        estudianteSeleccionado = estudiante;
        lbIdentificacion.setText(String.valueOf(estudiante.getIdentificacion()));
        lbGrado.setText(String.valueOf(estudiante.getGrado()));
        lbFaltasPrevias.setText(String.valueOf(estudianteDAO.contarFaltasPrevias(estudiante.getId())));
    }

    private void limpiarDatosEstudiante() {
        estudianteSeleccionado = null;
        lbIdentificacion.setText("-");
        lbGrado.setText("-");
        lbFaltasPrevias.setText("0");
    }

    private void seleccionarDocente(Docente docente) {
        docenteSeleccionado = docente;
    }

    private void limpiarDatosDocente() {
        limpiarSeleccionDocente();
        txtDocente.clear();
    }

    private void limpiarSeleccionDocente() {
        docenteSeleccionado = null;
    }

    private void ocultarDocente() {
        hboxDocente.setVisible(false);
        hboxDocente.setManaged(false);
        limpiarDatosDocente();
    }

    private String unirNombre(String... partes) {
        StringBuilder nombre = new StringBuilder();

        for (String parte : partes) {
            if (parte == null) {
                continue;
            }

            String valor = parte.trim();
            if (valor.isEmpty()) {
                continue;
            }

            if (nombre.length() > 0) {
                nombre.append(' ');
            }
            nombre.append(valor);
        }

        return nombre.toString();
    }

    @FXML
    void clickGuardar(ActionEvent event) {
        if (estudianteSeleccionado == null) {
            Alertas.mostrarWarning("Debes seleccionar un estudiante desde las sugerencias");
            return;
        }

        if (comboCaso.getValue() == null) {
            Alertas.mostrarWarning("Debes seleccionar un caso");
            return;
        }

        if (comboLugar.getValue() == null) {
            Alertas.mostrarWarning("Debes seleccionar un lugar");
            return;
        }

        if (comboLugar.getValue().getId() == ID_LUGAR_AULA && docenteSeleccionado == null) {
            Alertas.mostrarWarning("Debes seleccionar un docente desde las sugerencias");
            return;
        }

        if (comboFaltaTipo.getValue() == null) {
            Alertas.mostrarWarning("Debes seleccionar el tipo de falta");
            return;
        }

        int idDocente = resolverIdDocenteParaRegistro(comboLugar.getValue());
        if (idDocente <= 0) {
            return;
        }

        Falta falta = construirFalta(idDocente);
        if (faltaDAO.registrar(falta)) {
            Alertas.mostrarExito("Falta registrada correctamente");
            limpiarFormulario();
            return;
        }

        Alertas.mostrarError("No fue posible registrar la falta. Intenta nuevamente.");
    }

    private int resolverIdDocenteParaRegistro(Lugar lugarSeleccionado) {
        if (lugarSeleccionado.getId() == ID_LUGAR_AULA) {
            return docenteSeleccionado.getId();
        }

        boolean existeDocenteNoAplica = cacheDocentes.stream()
                .anyMatch(docente -> docente.getId() == ID_DOCENTE_NO_APLICA);

        if (!existeDocenteNoAplica) {
            Alertas.mostrarError("No existe el docente NO APLICA (id 65). Verifica los datos base.");
            return -1;
        }

        return ID_DOCENTE_NO_APLICA;
    }

    private Falta construirFalta(int idDocente) {
        Falta falta = new Falta();
        falta.setIdEstudiante(estudianteSeleccionado.getId());
        falta.setIdCaso(comboCaso.getValue().getId());
        falta.setIdLugar(comboLugar.getValue().getId());
        falta.setIdDocente(idDocente);
        falta.setTipoFalta(comboFaltaTipo.getValue());
        falta.setDescargo(normalizarTextoOpcional(txtDescargo.getText()));
        falta.setAccionRestaurativa(normalizarTextoOpcional(txtAccionRestaurativa.getText()));
        return falta;
    }

    private String normalizarTextoOpcional(String valor) {
        if (valor == null) {
            return null;
        }

        String texto = valor.trim();
        return texto.isEmpty() ? null : texto;
    }

    private void limpiarFormulario() {
        txtEstudiante.clear();
        txtDescargo.clear();
        txtAccionRestaurativa.clear();

        comboCaso.getSelectionModel().clearSelection();
        comboLugar.getSelectionModel().clearSelection();
        comboFaltaTipo.getSelectionModel().clearSelection();

        limpiarDatosEstudiante();
        menuSugerenciasEstudiante.hide();
        menuSugerenciasDocente.hide();
    }

}