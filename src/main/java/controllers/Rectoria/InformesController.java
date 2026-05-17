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
import reports.generators.InformeComparativoAniosGenerator;
import reports.generators.InformeEvolucionMensualGenerator;
import reports.generators.InformeFaltasEstudianteGenerator;
import reports.generators.InformeFaltasGeneralGenerator;
import reports.generators.InformeFaltasPorGradoGenerator;
import reports.models.ReportConfig;
import utils.Alertas;
import utils.BusquedaSugerencias;

import java.net.URL;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

public class InformesController implements Initializable {

    // ───── Tipo de reporte ─────
    @FXML private ComboBox<String> cbTipoReporte;

    // ───── Filtro estudiante (TextField + autocompletado) ─────
    @FXML private Label     lblEstudiante;
    @FXML private TextField txtEstudiante;

    // ───── Filtros dinámicos ─────
    @FXML private Label            lblTipoFalta;
    @FXML private ComboBox<String> cbTipoFalta;
    @FXML private Label            lblCaso;
    @FXML private ComboBox<Caso>   cbCaso;
    @FXML private Label            lblAnio1;
    @FXML private ComboBox<Integer> cbAnio1;
    @FXML private Label            lblAnio2;
    @FXML private ComboBox<Integer> cbAnio2;

    // ───── Fechas ─────
    @FXML private DatePicker dpFechaInicio;
    @FXML private DatePicker dpFechaFin;

    // ───── Acciones ─────
    @FXML private Button   btnFiltrar;
    @FXML private Button   btnLimpiarFiltro;
    @FXML private Button   btnGenerarReporte;
    @FXML private CheckBox chkIncluirTablas;

    // ───── Tarjetas resumen ─────
    @FXML private Label lblTotalFaltas;
    @FXML private Label lblFaltasTipo1;
    @FXML private Label lblFaltasTipo2;
    @FXML private Label lblFaltasTipo3;
    @FXML private Label lblEstudiantesAfectados;
    @FXML private Label lblDatosTabla;

    // ───── Tabla ─────
    @FXML private TableView<FaltaConsultaRow>           tblFaltas;
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
    private final FaltaDAO      faltaDAO      = new FaltaDAO();
    private final EstudianteDAO estudianteDAO = new EstudianteDAO();
    private final CasoDAO       casoDAO       = new CasoDAO();

    // ───── Tipos de reporte ─────
    private static final String TIPO_GENERAL           = "Informe General";
    private static final String TIPO_FALTAS_ESTUDIANTE = "Faltas por Estudiante";
    private static final String TIPO_FALTAS_POR_GRADO  = "Faltas por Grado";
    private static final String TIPO_EVOLUCION_MENSUAL = "Evolución mensual";
    private static final String TIPO_COMPARATIVO_ANIOS = "Comparativo entre años";

    // ───── Estado autocompletado estudiante ─────
    private final List<Estudiante> cacheEstudiantes          = new ArrayList<>();
    private final ContextMenu      menuSugerenciasEstudiante = new ContextMenu();
    private       Estudiante       estudianteSeleccionado    = null;

    // ───── Datos tabla ─────
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
        cargarAniosComparativos();
        cargarTiposFalta();
        configurarAutocompletadoEstudiante();

        cbTipoReporte.valueProperty().addListener((obs, oldVal, newVal) -> actualizarFiltrosVisibles(newVal));

        cargarVistaPrevia();
    }

    private void configurarColumnas() {
        colFecha.setCellValueFactory(c      -> new SimpleStringProperty(c.getValue().getFecha()));
        colEstudiante.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getEstudiante()));
        colGrado.setCellValueFactory(c      -> new SimpleStringProperty(c.getValue().getGrado() + "°"));
        // getTipoFalta() ya devuelve "Tipo 1" / "Tipo 2" / "Tipo 3" — no agregar prefijo
        colTipoFalta.setCellValueFactory(c  -> new SimpleStringProperty(c.getValue().getTipoFalta()));
        colCaso.setCellValueFactory(c       -> new SimpleStringProperty(c.getValue().getCaso()));
        colLugar.setCellValueFactory(c      -> new SimpleStringProperty(c.getValue().getLugar()));
        colDocente.setCellValueFactory(c    -> new SimpleStringProperty(c.getValue().getDocente()));
        colDescargo.setCellValueFactory(c   -> new SimpleStringProperty(safeStr(c.getValue().getDescargo())));
        colAccion.setCellValueFactory(c     -> new SimpleStringProperty(safeStr(c.getValue().getAccionRestaurativa())));

        tblFaltas.setItems(datosCargados);
    }

    private void cargarTiposDeReporte() {
        cbTipoReporte.getItems().clear();
        cbTipoReporte.getItems().addAll(TIPO_GENERAL, TIPO_FALTAS_ESTUDIANTE, TIPO_FALTAS_POR_GRADO, TIPO_EVOLUCION_MENSUAL, TIPO_COMPARATIVO_ANIOS);
        cbTipoReporte.setValue(TIPO_GENERAL);
    }

    private void cargarEstudiantes() {
        cacheEstudiantes.clear();
        cacheEstudiantes.addAll(estudianteDAO.obtenerTodos());
        txtEstudiante.setContextMenu(menuSugerenciasEstudiante);
    }

    private void cargarCasos() {
        List<Caso> casos = casoDAO.obtenerTodos();
        cbCaso.getItems().clear();
        cbCaso.getItems().addAll(casos);
    }

    private void cargarAniosComparativos() {
        List<Integer> anios = faltaDAO.obtenerAniosRegistrados();
        cbAnio1.getItems().clear();
        cbAnio2.getItems().clear();
        cbAnio1.getItems().addAll(anios);
        cbAnio2.getItems().addAll(anios);

        if (!anios.isEmpty()) {
            cbAnio1.getSelectionModel().select(anios.size() >= 2 ? anios.size() - 2 : 0);
            cbAnio2.getSelectionModel().select(anios.size() - 1);
        }
    }

    private void cargarTiposFalta() {
        cbTipoFalta.getItems().clear();
        // Deben coincidir exactamente con FaltaConsultaRow.getTipoFalta()
        cbTipoFalta.getItems().addAll("Tipo 1", "Tipo 2", "Tipo 3");
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  AUTOCOMPLETADO ESTUDIANTE
    // ─────────────────────────────────────────────────────────────────────────

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

        // Si el usuario edita manualmente el texto, limpiar selección
        txtEstudiante.textProperty().addListener((obs, oldText, newText) -> {
            if (estudianteSeleccionado == null) return;
            String actual = newText == null ? "" : newText.trim();
            if (!actual.equalsIgnoreCase(textoSeleccionEstudiante(estudianteSeleccionado))) {
                limpiarSeleccionEstudiante();
            }
        });
    }

    private String textoBusquedaEstudiante(Estudiante e) {
        return textoSeleccionEstudiante(e) + " " + e.getIdentificacion();
    }

    private String textoMenuEstudiante(Estudiante e) {
        return textoSeleccionEstudiante(e)
                + " | ID: " + e.getIdentificacion()
                + " | Grado: " + e.getGrado() + "°";
    }

    private String textoSeleccionEstudiante(Estudiante e) {
        return unirPartes(e.getNombre1(), e.getNombre2(), e.getApellido1(), e.getApellido2());
    }

    private void seleccionarEstudiante(Estudiante e) {
        estudianteSeleccionado = e;
        cargarDatos(
                e.getId(),
                dpFechaInicio.getValue() != null ? dpFechaInicio.getValue().toString() : null,
                dpFechaFin.getValue()    != null ? dpFechaFin.getValue().toString()    : null,
                tipoFaltaSeleccionado(),
                idCasoSeleccionado()
        );
    }

    private void limpiarSeleccionEstudiante() {
        estudianteSeleccionado = null;
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  VISIBILIDAD DINÁMICA DE FILTROS
    // ─────────────────────────────────────────────────────────────────────────

    private void actualizarFiltrosVisibles(String tipo) {
        boolean esEstudiante = TIPO_FALTAS_ESTUDIANTE.equals(tipo);
        boolean esPorGrado   = TIPO_FALTAS_POR_GRADO.equals(tipo);
        boolean esEvolucion  = TIPO_EVOLUCION_MENSUAL.equals(tipo);
        boolean esComparativo = TIPO_COMPARATIVO_ANIOS.equals(tipo);

        setVisible(lblEstudiante, txtEstudiante, esEstudiante);
        setVisible(lblTipoFalta,  cbTipoFalta,   esEstudiante);
        setVisible(lblCaso,       cbCaso,         esEstudiante || esComparativo);
        setVisible(lblAnio1,      cbAnio1,        esComparativo);
        setVisible(lblAnio2,      cbAnio2,        esComparativo);
        setVisible(dpFechaInicio, esEstudiante || esEvolucion || esPorGrado || TIPO_GENERAL.equals(tipo));
        setVisible(dpFechaFin,    esEstudiante || esEvolucion || esPorGrado || TIPO_GENERAL.equals(tipo));

        if (TIPO_GENERAL.equals(tipo)) {
            lblDatosTabla.setText("Vista previa — Informe General");
        } else if (TIPO_FALTAS_ESTUDIANTE.equals(tipo)) {
            lblDatosTabla.setText("Faltas por Estudiante");
        } else if (TIPO_FALTAS_POR_GRADO.equals(tipo)) {
            lblDatosTabla.setText("Faltas analizadas por Grado");
        } else if (TIPO_EVOLUCION_MENSUAL.equals(tipo)) {
            lblDatosTabla.setText("Evolución mensual de faltas");
        } else {
            lblDatosTabla.setText("Comparativo entre años");
        }

        cargarVistaPrevia();
    }

    private void setVisible(Label label, Control control, boolean visible) {
        label.setVisible(visible);
        label.setManaged(visible);
        control.setVisible(visible);
        control.setManaged(visible);
    }

    private void setVisible(Control control, boolean visible) {
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

    private void cargarVistaPrevia() {
        String tipo = cbTipoReporte.getValue();

        if (TIPO_COMPARATIVO_ANIOS.equals(tipo)) {
            cargarDatosComparativo();
            return;
        }

        Integer idEstudiante = estudianteSeleccionado != null ? estudianteSeleccionado.getId() : null;
        String fechaDesde = dpFechaInicio.getValue() != null ? dpFechaInicio.getValue().toString() : null;
        String fechaHasta = dpFechaFin.getValue() != null ? dpFechaFin.getValue().toString() : null;

        Integer tipoFalta = null;
        Integer idCaso = null;

        if (TIPO_FALTAS_ESTUDIANTE.equals(tipo)) {
            tipoFalta = tipoFaltaSeleccionado();
            idCaso = idCasoSeleccionado();
        }

        cargarDatos(idEstudiante, fechaDesde, fechaHasta, tipoFalta, idCaso);
    }

    private void cargarDatosComparativo() {
        List<FaltaConsultaRow> filas = faltaDAO.obtenerFaltasComparativa(idCasoSeleccionado());
        datosCargados.setAll(filas);
        actualizarResumen(filas);
    }

    private void actualizarResumen(List<FaltaConsultaRow> filas) {
        int total = filas.size();
        // getTipoFalta() devuelve "Tipo 1", "Tipo 2", "Tipo 3"
        long tipo1 = filas.stream().filter(f -> "Tipo 1".equals(f.getTipoFalta())).count();
        long tipo2 = filas.stream().filter(f -> "Tipo 2".equals(f.getTipoFalta())).count();
        long tipo3 = filas.stream().filter(f -> "Tipo 3".equals(f.getTipoFalta())).count();
        long estudiantesUnicos = filas.stream()
                .map(FaltaConsultaRow::getIdEstudiante).distinct().count();

        lblTotalFaltas.setText(String.valueOf(total));
        lblFaltasTipo1.setText(String.valueOf(tipo1));
        lblFaltasTipo2.setText(String.valueOf(tipo2));
        lblFaltasTipo3.setText(String.valueOf(tipo3));
        lblEstudiantesAfectados.setText(String.valueOf(estudiantesUnicos));
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  HANDLERS
    // ─────────────────────────────────────────────────────────────────────────

    @FXML
    void clickFiltrar(ActionEvent event) {
        cargarVistaPrevia();
    }

    @FXML
    void clickLimpiarFiltro(ActionEvent event) {
        txtEstudiante.clear();
        limpiarSeleccionEstudiante();
        cbTipoFalta.setValue(null);
        cbCaso.setValue(null);
        cbAnio1.setValue(null);
        cbAnio2.setValue(null);
        dpFechaInicio.setValue(null);
        dpFechaFin.setValue(null);
        cargarVistaPrevia();
    }

    @FXML
    void clickGenerarReporte(ActionEvent event) {
        String tipo = cbTipoReporte.getValue();
        if (tipo == null) {
            Alertas.mostrarError("Seleccione un tipo de reporte.");
            return;
        }
        if (!TIPO_GENERAL.equals(tipo) && !TIPO_FALTAS_POR_GRADO.equals(tipo) && !TIPO_EVOLUCION_MENSUAL.equals(tipo) && datosCargados.isEmpty()) {
            Alertas.mostrarError("No hay datos para generar el reporte. Aplique filtros primero.");
            return;
        }
        if (TIPO_GENERAL.equals(tipo)) {
            generarInformeGeneral();
        } else if (TIPO_FALTAS_ESTUDIANTE.equals(tipo)) {
            generarInformeFaltasEstudiante();
        } else if (TIPO_FALTAS_POR_GRADO.equals(tipo)) {
            generarInformeFaltasPorGrado();
        } else if (TIPO_EVOLUCION_MENSUAL.equals(tipo)) {
            generarInformeEvolucionMensual();
        } else if (TIPO_COMPARATIVO_ANIOS.equals(tipo)) {
            generarInformeComparativoAnios();
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  GENERACIÓN DE REPORTES
    // ─────────────────────────────────────────────────────────────────────────

    private void generarInformeGeneral() {
        ReportConfig config = new ReportConfig();
        config.setFechaInicio(dpFechaInicio.getValue());
        config.setFechaFin(dpFechaFin.getValue());
        config.setIncluirTablas(chkIncluirTablas.isSelected());
        new InformeFaltasGeneralGenerator(config, "Informe_General_Faltas_" + LocalDate.now() + ".pdf").generar();
    }

    private void generarInformeFaltasEstudiante() {
        ReportConfig config = new ReportConfig();
        config.setFechaInicio(dpFechaInicio.getValue());
        config.setFechaFin(dpFechaFin.getValue());
        config.setIncluirTablas(chkIncluirTablas.isSelected());
        if (estudianteSeleccionado != null) config.setIdEstudiante(estudianteSeleccionado.getId());

        Caso casoSel    = cbCaso.getValue();
        String nombreCaso = casoSel != null ? casoSel.getNombreCaso() : null;

        new InformeFaltasEstudianteGenerator(
                config,
                construirNombreArchivoEstudiante(estudianteSeleccionado),
                new ArrayList<>(datosCargados),
                estudianteSeleccionado,
                nombreCaso,
                tipoFaltaSeleccionado()
        ).generar();
    }

    private void generarInformeEvolucionMensual() {
        ReportConfig config = new ReportConfig();
        config.setFechaInicio(dpFechaInicio.getValue());
        config.setFechaFin(dpFechaFin.getValue());
        config.setIncluirTablas(chkIncluirTablas.isSelected());

        new InformeEvolucionMensualGenerator(
                config,
                "Informe_Evolucion_Mensual_" + LocalDate.now() + ".pdf"
        ).generar();
    }

    private void generarInformeFaltasPorGrado() {
        ReportConfig config = new ReportConfig();
        config.setFechaInicio(dpFechaInicio.getValue());
        config.setFechaFin(dpFechaFin.getValue());
        config.setIncluirTablas(chkIncluirTablas.isSelected());

        new InformeFaltasPorGradoGenerator(
                config,
                "Informe_Faltas_Por_Grado_" + LocalDate.now() + ".pdf"
        ).generar();
    }

    private void generarInformeComparativoAnios() {
        Integer anio1 = cbAnio1.getValue();
        Integer anio2 = cbAnio2.getValue();

        if (anio1 == null || anio2 == null) {
            Alertas.mostrarError("Seleccione dos años para generar el comparativo.");
            return;
        }

        if (anio1.equals(anio2)) {
            Alertas.mostrarError("Los dos años del comparativo deben ser diferentes.");
            return;
        }

        ReportConfig config = new ReportConfig();
        config.setIncluirTablas(chkIncluirTablas.isSelected());
        config.setIdCaso(idCasoSeleccionado());
        config.setAnioComparativo1(anio1);
        config.setAnioComparativo2(anio2);

        Caso casoSel = cbCaso.getValue();
        String nombreCaso = casoSel != null ? casoSel.getNombreCaso() : null;

        new InformeComparativoAniosGenerator(
                config,
                "Informe_Comparativo_Anios_" + LocalDate.now() + ".pdf",
                nombreCaso
        ).generar();
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  UTILIDADES
    // ─────────────────────────────────────────────────────────────────────────

    /** Informe_Faltas_Estudiante_NOMBRE1_APELLIDO1_FECHA.pdf */
    private String construirNombreArchivoEstudiante(Estudiante e) {
        if (e == null) return "Informe_Faltas_Estudiante_" + LocalDate.now() + ".pdf";
        return "Informe_Faltas_Estudiante_"
                + sanitizar(e.getNombre1()) + "_"
                + sanitizar(e.getApellido1()) + "_"
                + LocalDate.now() + ".pdf";
    }

    private String sanitizar(String valor) {
        if (valor == null || valor.trim().isEmpty()) return "SIN_DATO";
        return valor.trim().toUpperCase()
                .replaceAll("[áÁ]", "A").replaceAll("[éÉ]", "E")
                .replaceAll("[íÍ]", "I").replaceAll("[óÓ]", "O")
                .replaceAll("[úÚüÜ]", "U").replaceAll("[ñÑ]", "N")
                .replaceAll("[^A-Z0-9]", "_").replaceAll("_+", "_")
                .replaceAll("^_|_$", "");
    }

    private Integer tipoFaltaSeleccionado() {
        if (!cbTipoFalta.isVisible() || cbTipoFalta.getValue() == null) return null;
        try {
            return Integer.parseInt(cbTipoFalta.getValue().replace("Tipo ", "").trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private Integer idCasoSeleccionado() {
        if (!cbCaso.isVisible() || cbCaso.getValue() == null) return null;
        return cbCaso.getValue().getId();
    }

    private String unirPartes(String... partes) {
        StringBuilder sb = new StringBuilder();
        for (String p : partes) {
            if (p == null) continue;
            String v = p.trim();
            if (v.isEmpty()) continue;
            if (sb.length() > 0) sb.append(' ');
            sb.append(v);
        }
        return sb.toString();
    }

    private String safeStr(String s) { return s != null ? s : ""; }
}