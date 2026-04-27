package controllers.Orientador;

import database.CasoDAO;
import database.EstudianteDAO;
import database.FaltaDAO;
import database.LugarDAO;
import database.models.Caso;
import database.models.FaltaConsultaRow;
import database.models.Lugar;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import models.Estudiante;
import utils.BusquedaSugerencias;
import utils.Fechas;

import java.util.ArrayList;
import java.util.List;

public class ConsultasController {

    @FXML
    private TextField txtEstudiante;

    @FXML
    private DatePicker dateDesde;

    @FXML
    private DatePicker dateHasta;

    @FXML
    private ComboBox<Integer> comboTipoFalta;

    @FXML
    private ComboBox<Caso> comboCaso;

    @FXML
    private ComboBox<Lugar> comboLugar;

    @FXML
    private TableView<FaltaConsultaRow> tableResultados;

    @FXML
    private TableColumn<FaltaConsultaRow, String> colFecha;

    @FXML
    private TableColumn<FaltaConsultaRow, String> colEstudiante;

    @FXML
    private TableColumn<FaltaConsultaRow, String> colTipo;

    @FXML
    private TableColumn<FaltaConsultaRow, String> colCaso;

    @FXML
    private TableColumn<FaltaConsultaRow, String> colLugar;

    // ...existing code...
    private final EstudianteDAO estudianteDAO = new EstudianteDAO();
    private final FaltaDAO faltaDAO = new FaltaDAO();
    private final CasoDAO casoDAO = new CasoDAO();
    private final LugarDAO lugarDAO = new LugarDAO();

    private final List<Estudiante> cacheEstudiantes = new ArrayList<>();
    private final ContextMenu menuSugerenciasEstudiante = new ContextMenu();

    private Estudiante estudianteSeleccionado;

    @FXML
    void initialize() {
        configurarTabla();
        cargarFiltros();
        configurarAutocompletadoEstudiante();
        buscar();
    }

    private void configurarTabla() {
        colFecha.setCellValueFactory(new PropertyValueFactory<>("fecha"));
        colEstudiante.setCellValueFactory(new PropertyValueFactory<>("estudiante"));
        colTipo.setCellValueFactory(new PropertyValueFactory<>("tipoFalta"));
        colCaso.setCellValueFactory(new PropertyValueFactory<>("caso"));
        colLugar.setCellValueFactory(new PropertyValueFactory<>("lugar"));

        tableResultados.setOnMouseClicked(event -> {
            if (event.getClickCount() == 1) {
                FaltaConsultaRow seleccionada = tableResultados.getSelectionModel().getSelectedItem();
                if (seleccionada != null) {
                    mostrarDetallesFalta(seleccionada);
                }
            }
        });
    }

    private void cargarFiltros() {
        cacheEstudiantes.clear();
        cacheEstudiantes.addAll(estudianteDAO.obtenerTodos());

        comboTipoFalta.setItems(FXCollections.observableArrayList(1, 2, 3));

        List<Caso> casos = new ArrayList<>();
        casos.add(new Caso(0, "Todos"));
        casos.addAll(casoDAO.obtenerTodos());
        comboCaso.setItems(FXCollections.observableArrayList(casos));
        comboCaso.getSelectionModel().selectFirst();

        List<Lugar> lugares = new ArrayList<>();
        lugares.add(new Lugar(0, "Todos"));
        lugares.addAll(lugarDAO.obtenerTodos());
        comboLugar.setItems(FXCollections.observableArrayList(lugares));
        comboLugar.getSelectionModel().selectFirst();

        txtEstudiante.setContextMenu(menuSugerenciasEstudiante);
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
                this::limpiarSeleccionEstudiante
        );

        txtEstudiante.textProperty().addListener((obs, oldText, newText) -> {
            if (estudianteSeleccionado == null) {
                return;
            }

            String textoActual = newText == null ? "" : newText.trim();
            if (!textoActual.equalsIgnoreCase(textoSeleccionEstudiante(estudianteSeleccionado))) {
                limpiarSeleccionEstudiante();
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
        return unirNombre(
                estudiante.getNombre1(),
                estudiante.getNombre2(),
                estudiante.getApellido1(),
                estudiante.getApellido2()
        );
    }

    private void seleccionarEstudiante(Estudiante estudiante) {
        estudianteSeleccionado = estudiante;
    }

    private void limpiarSeleccionEstudiante() {
        estudianteSeleccionado = null;
    }

    private String unirNombre(String... partes) {
        StringBuilder nombre = new StringBuilder();

        for (String parte : partes) {
            if (parte == null) continue;
            String valor = parte.trim();
            if (valor.isEmpty()) continue;

            if (nombre.length() > 0) {
                nombre.append(' ');
            }
            nombre.append(valor);
        }

        return nombre.toString();
    }

    @FXML
    void clickBuscar() {
        buscar();
    }

    @FXML
    void clickLimpiar() {
        txtEstudiante.clear();
        dateDesde.setValue(null);
        dateHasta.setValue(null);
        comboTipoFalta.getSelectionModel().clearSelection();
        comboCaso.getSelectionModel().selectFirst();
        comboLugar.getSelectionModel().selectFirst();
        limpiarSeleccionEstudiante();
        buscar();
    }

    private void buscar() {
        Integer idEstudiante = estudianteSeleccionado != null ? estudianteSeleccionado.getId() : null;
        String fechaDesdeISO = dateDesde.getValue() != null ? Fechas.convertirAISO(dateDesde.getValue()) : null;
        String fechaHastaISO = dateHasta.getValue() != null ? Fechas.convertirAISO(dateHasta.getValue()) : null;
        Integer tipoFalta = comboTipoFalta.getValue();

        Integer idCaso = null;
        if (comboCaso.getValue() != null && comboCaso.getValue().getId() != 0) {
            idCaso = comboCaso.getValue().getId();
        }

        Integer idLugar = null;
        if (comboLugar.getValue() != null && comboLugar.getValue().getId() != 0) {
            idLugar = comboLugar.getValue().getId();
        }

        List<FaltaConsultaRow> resultados = faltaDAO.consultarFaltas(
                idEstudiante,
                fechaDesdeISO,
                fechaHastaISO,
                tipoFalta,
                idCaso,
                idLugar
        );

        tableResultados.setItems(FXCollections.observableArrayList(resultados));
    }

    private void mostrarDetallesFalta(FaltaConsultaRow falta) {
        Stage ventana = new Stage();
        ventana.initModality(Modality.APPLICATION_MODAL);
        ventana.setTitle("Detalles de la Falta");
        ventana.setWidth(700);
        ventana.setHeight(600);

        VBox contenedor = new VBox(15);
        contenedor.setPadding(new Insets(20));
        contenedor.setStyle("-fx-background-color: #f8f9fa;");
        contenedor.setSpacing(15);

        // Información del estudiante
        VBox estudianteBox = crearSeccion("INFORMACIÓN DEL ESTUDIANTE");
        estudianteBox.getChildren().addAll(
                crearFila("Nombre:", falta.getEstudiante()),
                crearFila("Identificación:", falta.getIdentificacion()),
                crearFila("Grado:", String.valueOf(falta.getGrado()))
        );

        // Información de la falta
        VBox faltaBox = crearSeccion("INFORMACIÓN DE LA FALTA");
        faltaBox.getChildren().addAll(
                crearFila("Fecha:", falta.getFecha()),
                crearFila("Tipo:", falta.getTipoFalta()),
                crearFila("Caso:", falta.getCaso()),
                crearFila("Lugar:", falta.getLugar())
        );

        // Mostrar docente solo si es AULA (id_lugar = 1)
        if (falta.getIdLugar() == 1 && falta.getDocente() != null && !falta.getDocente().isEmpty()) {
            faltaBox.getChildren().add(crearFila("Docente presente en el aula:", falta.getDocente()));
        }

        // Descargo
        VBox descargoBox = crearSeccion("DESCARGO DEL ESTUDIANTE");
        String descargo = falta.getDescargo() == null || falta.getDescargo().isEmpty() 
                ? "Sin información" 
                : falta.getDescargo();
        Label lblDescargo = new Label(descargo);
        lblDescargo.setWrapText(true);
        lblDescargo.setStyle("-fx-text-fill: #495057; -fx-font-size: 13px; -fx-line-spacing: 1.5;");
        lblDescargo.setMaxWidth(Double.MAX_VALUE);
        descargoBox.getChildren().add(lblDescargo);

        // Acción restaurativa
        VBox accionBox = crearSeccion("ACCIÓN RESTAURATIVA");
        String accion = falta.getAccionRestaurativa() == null || falta.getAccionRestaurativa().isEmpty() 
                ? "Sin información" 
                : falta.getAccionRestaurativa();
        Label lblAccion = new Label(accion);
        lblAccion.setWrapText(true);
        lblAccion.setStyle("-fx-text-fill: #495057; -fx-font-size: 13px; -fx-line-spacing: 1.5;");
        lblAccion.setMaxWidth(Double.MAX_VALUE);
        accionBox.getChildren().add(lblAccion);

        contenedor.getChildren().addAll(estudianteBox, faltaBox, descargoBox, accionBox);

        javafx.scene.control.ScrollPane scrollPane = new javafx.scene.control.ScrollPane(contenedor);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background: #f8f9fa; -fx-background-color: #f8f9fa;");

        javafx.scene.Scene scene = new javafx.scene.Scene(scrollPane);
        ventana.setScene(scene);
        ventana.showAndWait();
    }

    private VBox crearSeccion(String titulo) {
        VBox seccion = new VBox(10);
        seccion.setStyle("-fx-border-color: #dee2e6; -fx-border-radius: 6; -fx-background-color: #ffffff; -fx-padding: 15;");

        Label lblTitulo = new Label(titulo);
        lblTitulo.setStyle("-fx-text-fill: #0d5f8e; -fx-font-weight: bold; -fx-font-size: 14px;");

        seccion.getChildren().add(lblTitulo);
        return seccion;
    }

    private javafx.scene.layout.HBox crearFila(String etiqueta, String valor) {
        javafx.scene.layout.VBox contenedorFila = new javafx.scene.layout.VBox(5);

        Label lbl = new Label(etiqueta);
        lbl.setStyle("-fx-text-fill: #6c757d; -fx-font-weight: bold; -fx-font-size: 12px;");

        Label val = new Label(valor);
        val.setStyle("-fx-text-fill: #212529; -fx-font-size: 13px;");
        val.setWrapText(true);
        val.setMaxWidth(Double.MAX_VALUE);

        contenedorFila.getChildren().addAll(lbl, val);
        return new javafx.scene.layout.HBox(contenedorFila);
    }
}









