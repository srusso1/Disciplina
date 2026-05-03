package controllers.Rectoria;

import database.CasoDAO;
import database.EstudianteDAO;
import database.FaltaDAO;
import database.models.Caso;
import database.models.FaltaConsultaRow;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import models.Estudiante;
import reports.generators.InformeFaltasEstudianteGenerator;
import reports.models.ReportConfig;
import utils.Alertas;

import java.net.URL;
import java.time.LocalDate;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class InformesController implements Initializable {

    // ───── Tipo de reporte ─────
    @FXML private ComboBox<String> cbTipoReporte;

    // ───── Filtros dinámicos ─────
    @FXML private Label       lblEstudiante;
    @FXML private ComboBox<Estudiante> cbEstudiante;
    @FXML private Label       lblGrado;
    @FXML private ComboBox<String>     cbGrado;
    @FXML private Label       lblTipoFalta;
    @FXML private ComboBox<String>     cbTipoFalta;
    @FXML private Label       lblCaso;
    @FXML private ComboBox<Caso>       cbCaso;

    // ───── Fechas ─────
    @FXML private DatePicker dpFechaInicio;
    @FXML private DatePicker dpFechaFin;

    // ───── Acciones ─────
    @FXML private Button  btnFiltrar;
    @FXML private Button  btnLimpiarFiltro;
    @FXML private Button  btnGenerarReporte;
    @FXML private CheckBox chkIncluirTablas;

    // ───── Resumen ─────
    @FXML private Label lblTotalFaltas;
    @FXML private Label lblFaltasGraves;
    @FXML private Label lblFaltasLeves;
    @FXML private Label lblEstudiantesAfectados;
    @FXML private Label lblDatosTabla;

    // ───── Tabla ─────
    @FXML private TableView<FaltaConsultaRow> tblFaltas;
    @FXML private TableColumn<FaltaConsultaRow, String> colFecha;
    @FXML private TableColumn<FaltaConsultaRow, String> colEstudiante;
    @FXML private TableColumn<FaltaConsultaRow, String> colGrado;
    @FXML private TableColumn<FaltaConsultaRow, String> colTipoFalta;
    @FXML private TableColumn<FaltaConsultaRow, String> colCaso;
    @FXML private TableColumn<FaltaConsultaRow, String> colLugar;
    @FXML private TableColumn<FaltaConsultaRow, String> colDocente;
    @FXML private TableColumn<FaltaConsultaRow, String> colDescargo;
    @FXML private TableColumn<FaltaConsultaRow, String> colAccion;

    // ───── DAOs ─────
    private final FaltaDAO     faltaDAO     = new FaltaDAO();
    private final EstudianteDAO estudianteDAO = new EstudianteDAO();
    private final CasoDAO      casoDAO      = new CasoDAO();

    // ───── Tipos de reporte disponibles ─────
    private static final String TIPO_FALTAS_ESTUDIANTE = "Faltas por Estudiante";
    // (aquí se añaden los futuros tipos)

    // ───── Estado ─────
    private ObservableList<FaltaConsultaRow> datosCargados = FXCollections.observableArrayList();

    // ─────────────────────────────────────────────────────────────────────────
    //  INICIALIZACIÓN
    // ─────────────────────────────────────────────────────────────────────────

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        configurarColumnas();
        cargarTiposDeReporte();
        cargarEstudiantes();
        cargarCasos();
        cargarGrados();
        cargarTiposFalta();

        // Cuando cambie el tipo de reporte → mostrar/ocultar filtros
        cbTipoReporte.valueProperty().addListener((obs, oldVal, newVal) -> actualizarFiltrosVisibles(newVal));

        // Cargar datos iniciales (sin filtros)
        cargarDatos(null, null, null, null, null);
    }

    private void configurarColumnas() {
        colFecha.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getFecha()));
        colEstudiante.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getEstudiante()));
        colGrado.setCellValueFactory(c -> new SimpleStringProperty(String.valueOf(c.getValue().getGrado())));
        colTipoFalta.setCellValueFactory(c -> new SimpleStringProperty(resolverTipoFalta(c.getValue().getTipoFalta())));
        colCaso.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getCaso()));
        colLugar.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getLugar()));
        colDocente.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getDocente()));
        colDescargo.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getDescargo()));
        colAccion.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getAccionRestaurativa()));

        tblFaltas.setItems(datosCargados);
    }

    private void cargarTiposDeReporte() {
        cbTipoReporte.getItems().clear();
        cbTipoReporte.getItems().add(TIPO_FALTAS_ESTUDIANTE);
        cbTipoReporte.setValue(TIPO_FALTAS_ESTUDIANTE);
    }

    private void cargarEstudiantes() {
        List<Estudiante> estudiantes = estudianteDAO.obtenerTodos();
        cbEstudiante.getItems().clear();
        cbEstudiante.getItems().addAll(estudiantes);

        // Mostrar nombre completo en el combo
        cbEstudiante.setConverter(new javafx.util.StringConverter<Estudiante>() {
            @Override public String toString(Estudiante e) {
                if (e == null) return "";
                return (e.getNombre1() + " " +
                        (e.getNombre2() != null ? e.getNombre2() + " " : "") +
                        e.getApellido1() + " " +
                        (e.getApellido2() != null ? e.getApellido2() : "")).trim();
            }
            @Override public Estudiante fromString(String s) { return null; }
        });
    }

    private void cargarCasos() {
        List<Caso> casos = casoDAO.obtenerTodos();
        cbCaso.getItems().clear();
        cbCaso.getItems().addAll(casos);
    }

    private void cargarGrados() {
        cbGrado.getItems().clear();
        cbGrado.getItems().addAll(
                "6°", "7°", "8°", "9°", "10°", "11°"
        );
    }

    private void cargarTiposFalta() {
        cbTipoFalta.getItems().clear();
        cbTipoFalta.getItems().addAll("Leve (Tipo 1)", "Grave (Tipo 2)", "Gravísima (Tipo 3)");
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  VISIBILIDAD DINÁMICA DE FILTROS
    // ─────────────────────────────────────────────────────────────────────────

    private void actualizarFiltrosVisibles(String tipo) {
        boolean esFaltasEstudiante = TIPO_FALTAS_ESTUDIANTE.equals(tipo);

        setVisible(lblEstudiante,  cbEstudiante,  esFaltasEstudiante);
        setVisible(lblGrado,       cbGrado,        esFaltasEstudiante);
        setVisible(lblTipoFalta,   cbTipoFalta,    esFaltasEstudiante);
        setVisible(lblCaso,        cbCaso,         esFaltasEstudiante);

        // Actualizar el subtítulo de la tabla
        if (esFaltasEstudiante) {
            lblDatosTabla.setText("Faltas por Estudiante");
        }

        cargarDatos(null, null, null, null, null);
    }

    private void setVisible(Label label, Control control, boolean visible) {
        label.setVisible(visible);
        label.setManaged(visible);
        control.setVisible(visible);
        control.setManaged(visible);
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  CARGA DE DATOS
    // ─────────────────────────────────────────────────────────────────────────

    private void cargarDatos(Integer idEstudiante, String fechaDesde, String fechaHasta,
                             Integer tipoFalta, Integer idCaso) {
        List<FaltaConsultaRow> filas = faltaDAO.consultarFaltas(
                idEstudiante, fechaDesde, fechaHasta, tipoFalta, idCaso, null
        );

        datosCargados.setAll(filas);
        actualizarResumen(filas);
    }

    private void actualizarResumen(List<FaltaConsultaRow> filas) {
        int total  = filas.size();
        int graves = (int) filas.stream()
                .filter(f -> "2".equals(f.getTipoFalta()) || "3".equals(f.getTipoFalta()))
                .count();
        int leves  = (int) filas.stream()
                .filter(f -> "1".equals(f.getTipoFalta()))
                .count();
        long estudiantesUnicos = filas.stream()
                .map(FaltaConsultaRow::getIdEstudiante)
                .distinct().count();

        lblTotalFaltas.setText(String.valueOf(total));
        lblFaltasGraves.setText(String.valueOf(graves));
        lblFaltasLeves.setText(String.valueOf(leves));
        lblEstudiantesAfectados.setText(String.valueOf(estudiantesUnicos));
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  HANDLERS
    // ─────────────────────────────────────────────────────────────────────────

    @FXML
    void clickFiltrar(ActionEvent event) {
        Integer idEstudiante = null;
        if (cbEstudiante.isVisible() && cbEstudiante.getValue() != null) {
            idEstudiante = cbEstudiante.getValue().getId();
        }

        String fechaDesde = dpFechaInicio.getValue() != null
                ? dpFechaInicio.getValue().toString() : null;
        String fechaHasta = dpFechaFin.getValue() != null
                ? dpFechaFin.getValue().toString() : null;

        Integer tipoFalta = null;
        if (cbTipoFalta.isVisible() && cbTipoFalta.getValue() != null) {
            // "Leve (Tipo 1)" → extraer el número
            String sel = cbTipoFalta.getValue();
            if (sel.contains("1")) tipoFalta = 1;
            else if (sel.contains("2")) tipoFalta = 2;
            else if (sel.contains("3")) tipoFalta = 3;
        }

        Integer idCaso = null;
        if (cbCaso.isVisible() && cbCaso.getValue() != null) {
            idCaso = cbCaso.getValue().getId();
        }

        cargarDatos(idEstudiante, fechaDesde, fechaHasta, tipoFalta, idCaso);
    }

    @FXML
    void clickLimpiarFiltro(ActionEvent event) {
        cbEstudiante.setValue(null);
        cbGrado.setValue(null);
        cbTipoFalta.setValue(null);
        cbCaso.setValue(null);
        dpFechaInicio.setValue(null);
        dpFechaFin.setValue(null);
        cargarDatos(null, null, null, null, null);
    }

    @FXML
    void clickGenerarReporte(ActionEvent event) {
        String tipo = cbTipoReporte.getValue();
        if (tipo == null) {
            Alertas.mostrarError("Seleccione un tipo de reporte.");
            return;
        }
        if (datosCargados.isEmpty()) {
            Alertas.mostrarError("No hay datos para generar el reporte. Aplique filtros primero.");
            return;
        }

        if (TIPO_FALTAS_ESTUDIANTE.equals(tipo)) {
            generarInformeFaltasEstudiante();
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  GENERACIÓN DE REPORTES
    // ─────────────────────────────────────────────────────────────────────────

    private void generarInformeFaltasEstudiante() {
        ReportConfig config = new ReportConfig();
        config.setFechaInicio(dpFechaInicio.getValue());
        config.setFechaFin(dpFechaFin.getValue());
        config.setIncluirTablas(chkIncluirTablas.isSelected());

        // Filtros opcionales para el encabezado del PDF
        Estudiante estudianteSeleccionado = cbEstudiante.getValue();
        if (estudianteSeleccionado != null) {
            config.setIdEstudiante(estudianteSeleccionado.getId());
        }

        Caso casoSeleccionado = cbCaso.getValue();
        String nombreCaso = casoSeleccionado != null ? casoSeleccionado.getNombreCaso() : null;

        Integer tipoFaltaNum = null;
        if (cbTipoFalta.getValue() != null) {
            String sel = cbTipoFalta.getValue();
            if (sel.contains("1")) tipoFaltaNum = 1;
            else if (sel.contains("2")) tipoFaltaNum = 2;
            else if (sel.contains("3")) tipoFaltaNum = 3;
        }

        String nombreArchivo = construirNombreArchivo(estudianteSeleccionado);

        InformeFaltasEstudianteGenerator gen = new InformeFaltasEstudianteGenerator(
                config,
                nombreArchivo,
                new java.util.ArrayList<>(datosCargados),
                estudianteSeleccionado,
                nombreCaso,
                tipoFaltaNum
        );
        gen.generar();
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  UTILIDADES
    // ─────────────────────────────────────────────────────────────────────────

    private String construirNombreArchivo(Estudiante estudiante) {
        String base = "Informe_Faltas";
        if (estudiante != null) {
            base += "_" + estudiante.getApellido1().replaceAll("\\s+", "_");
        }
        base += "_" + LocalDate.now().toString();
        return base + ".pdf";
    }

    /** Convierte el entero tipo_falta a texto legible. */
    private String resolverTipoFalta(String tipo) {
        if (tipo == null) return "";
        switch (tipo.trim()) {
            case "1": return "Leve";
            case "2": return "Grave";
            case "3": return "Gravísima";
            default:  return tipo;
        }
    }
}