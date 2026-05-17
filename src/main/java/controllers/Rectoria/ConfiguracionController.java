package controllers.Rectoria;

import database.CasoDAO;
import database.EstudianteDAO;
import database.LugarDAO;
import database.models.Caso;
import database.models.Lugar;
import javafx.beans.binding.Bindings;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.stage.FileChooser;
import models.Estudiante;
import utils.Alertas;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class ConfiguracionController {

    private static final String FILTRO_TODOS = "Todos";

    private final EstudianteDAO estudianteDAO = new EstudianteDAO();
    private final LugarDAO lugarDAO = new LugarDAO();
    private final CasoDAO casoDAO = new CasoDAO();

    private final List<Estudiante> baseEstudiantes = new ArrayList<>();
    private final ObservableList<Estudiante> estudiantesFiltrados = FXCollections.observableArrayList();
    private final ObservableList<Lugar> lugares = FXCollections.observableArrayList();
    private final ObservableList<Caso> casos = FXCollections.observableArrayList();

    private Estudiante estudianteSeleccionado;
    private Lugar lugarSeleccionado;
    private Caso casoSeleccionado;

    // Archivo CSV seleccionado para importar
    private File archivoCSVSeleccionado = null;

    // ─────────────────────────────────────────────────────────────
    // Estudiantes
    // ─────────────────────────────────────────────────────────────
    @FXML private TableView<Estudiante> tblEstudiantes;
    @FXML private TableColumn<Estudiante, String> colEstIdentificacion;
    @FXML private TableColumn<Estudiante, String> colEstGrado;
    @FXML private TableColumn<Estudiante, String> colEstApellido1;
    @FXML private TableColumn<Estudiante, String> colEstApellido2;
    @FXML private TableColumn<Estudiante, String> colEstNombre1;
    @FXML private TableColumn<Estudiante, String> colEstNombre2;
    @FXML private TableColumn<Estudiante, String> colEstGenero;
    @FXML private TableColumn<Estudiante, String> colEstEstado;
    @FXML private TableColumn<Estudiante, String> colEstAñoEscolar;
    @FXML private ComboBox<String> cbFiltroGrado;
    @FXML private ComboBox<String> cbFiltroEstado;
    @FXML private TextField txtBuscarEstudiante;
    @FXML private TextField txtIdentificacion;
    @FXML private TextField txtGrado;
    @FXML private TextField txtApellido1;
    @FXML private TextField txtApellido2;
    @FXML private TextField txtNombre1;
    @FXML private TextField txtNombre2;
    @FXML private ComboBox<String> cbGenero;
    @FXML private Button btnGuardarEstudiante;
    @FXML private Button btnNuevoEstudiante;
    @FXML private Button btnAlternarEstadoEstudiante;
    @FXML private Label lblResumenEstudiantes;

    // CSV Import
    @FXML private ComboBox<Integer> cbAñoEscolarImport;
    @FXML private Button btnSeleccionarCSV;
    @FXML private Button btnImportarCSV;
    @FXML private Label lblEstadoImport;

    // ─────────────────────────────────────────────────────────────
    // Lugares
    // ─────────────────────────────────────────────────────────────
    @FXML private TableView<Lugar> tblLugares;
    @FXML private TableColumn<Lugar, String> colLugarId;
    @FXML private TableColumn<Lugar, String> colLugarNombre;
    @FXML private TableColumn<Lugar, String> colLugarEstado;
    @FXML private TextField txtNombreLugar;
    @FXML private Button btnGuardarLugar;
    @FXML private Button btnNuevoLugar;
    @FXML private Button btnAlternarEstadoLugar;
    @FXML private Label lblEstadoLugar;
    @FXML private Label lblResumenLugares;

    // ─────────────────────────────────────────────────────────────
    // Casos
    // ─────────────────────────────────────────────────────────────
    @FXML private TableView<Caso> tblCasos;
    @FXML private TableColumn<Caso, String> colCasoId;
    @FXML private TableColumn<Caso, String> colCasoNombre;
    @FXML private TableColumn<Caso, String> colCasoEstado;
    @FXML private TextField txtNombreCaso;
    @FXML private Button btnGuardarCaso;
    @FXML private Button btnNuevoCaso;
    @FXML private Button btnAlternarEstadoCaso;
    @FXML private Label lblEstadoCaso;
    @FXML private Label lblResumenCasos;

    @FXML
    void initialize() {
        cargarOpcionesGenero();
        cargarOpcionesEstado();
        configurarTablaEstudiantes();
        configurarTablaLugares();
        configurarTablaCasos();
        configurarFiltrosEstudiantes();
        cargarAñosEscolaresImport();

        cargarEstudiantes();
        cargarLugares();
        cargarCasos();
        actualizarModoEstudiante();
        actualizarModoLugar();
        actualizarModoCaso();
    }

    private void cargarOpcionesGenero() {
        ObservableList<String> generos = FXCollections.observableArrayList("Masculino", "Femenino");
        cbGenero.setItems(generos);
    }

    private void cargarOpcionesEstado() {
        ObservableList<String> estados = FXCollections.observableArrayList("Todos", "Activos", "Inactivos");
        cbFiltroEstado.setItems(estados);
        cbFiltroEstado.setValue("Todos");
    }

    private void cargarAñosEscolaresImport() {
        int añoActual = LocalDate.now().getYear();
        List<Integer> años = new ArrayList<>();
        años.add(añoActual + 1);
        años.add(añoActual + 2);
        cbAñoEscolarImport.setItems(FXCollections.observableArrayList(años));
        cbAñoEscolarImport.setValue(años.get(0)); // Selecciono el primer año por defecto
    }

    // ─────────────────────────────────────────────────────────────
    // Configuración tablas
    // ─────────────────────────────────────────────────────────────

    private void configurarTablaEstudiantes() {
        colEstIdentificacion.setCellValueFactory(cd -> Bindings.createStringBinding(() -> String.valueOf(cd.getValue().getIdentificacion())));
        colEstGrado.setCellValueFactory(cd -> Bindings.createStringBinding(() -> String.valueOf(cd.getValue().getGrado())));
        colEstApellido1.setCellValueFactory(cd -> Bindings.createStringBinding(() -> valorSeguro(cd.getValue().getApellido1())));
        colEstApellido2.setCellValueFactory(cd -> Bindings.createStringBinding(() -> valorSeguro(cd.getValue().getApellido2())));
        colEstNombre1.setCellValueFactory(cd -> Bindings.createStringBinding(() -> valorSeguro(cd.getValue().getNombre1())));
        colEstNombre2.setCellValueFactory(cd -> Bindings.createStringBinding(() -> valorSeguro(cd.getValue().getNombre2())));
        colEstGenero.setCellValueFactory(cd -> Bindings.createStringBinding(() -> valorSeguro(cd.getValue().getGenero())));
        colEstEstado.setCellValueFactory(cd -> Bindings.createStringBinding(() -> estadoTexto(cd.getValue().getEstado())));
        colEstAñoEscolar.setCellValueFactory(cd -> Bindings.createStringBinding(() -> String.valueOf(cd.getValue().getAño_escolar())));

        tblEstudiantes.setItems(estudiantesFiltrados);
        tblEstudiantes.getSelectionModel().selectedItemProperty().addListener((obs, oldValue, nuevo) -> {
            estudianteSeleccionado = nuevo;
            cargarEstudianteEnFormulario(nuevo);
        });
    }

    private void configurarTablaLugares() {
        colLugarId.setCellValueFactory(cd -> Bindings.createStringBinding(() -> String.valueOf(cd.getValue().getId())));
        colLugarNombre.setCellValueFactory(cd -> Bindings.createStringBinding(() -> valorSeguro(cd.getValue().getNombreLugar())));
        colLugarEstado.setCellValueFactory(cd -> Bindings.createStringBinding(() -> estadoTexto(cd.getValue().getEstado())));

        tblLugares.setItems(lugares);
        tblLugares.getSelectionModel().selectedItemProperty().addListener((obs, oldValue, nuevo) -> {
            lugarSeleccionado = nuevo;
            cargarLugarEnFormulario(nuevo);
        });
        tblLugares.setRowFactory(tv -> new TableRow<>() {
            @Override
            protected void updateItem(Lugar item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setStyle("");
                } else if (item.getEstado() == 0) {
                    setStyle("-fx-opacity: 0.72;");
                } else {
                    setStyle("");
                }
            }
        });
    }

    private void configurarTablaCasos() {
        colCasoId.setCellValueFactory(cd -> Bindings.createStringBinding(() -> String.valueOf(cd.getValue().getId())));
        colCasoNombre.setCellValueFactory(cd -> Bindings.createStringBinding(() -> valorSeguro(cd.getValue().getNombreCaso())));
        colCasoEstado.setCellValueFactory(cd -> Bindings.createStringBinding(() -> estadoTexto(cd.getValue().getEstado())));

        tblCasos.setItems(casos);
        tblCasos.getSelectionModel().selectedItemProperty().addListener((obs, oldValue, nuevo) -> {
            casoSeleccionado = nuevo;
            cargarCasoEnFormulario(nuevo);
        });
        tblCasos.setRowFactory(tv -> new TableRow<>() {
            @Override
            protected void updateItem(Caso item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setStyle("");
                } else if (item.getEstado() == 0) {
                    setStyle("-fx-opacity: 0.72;");
                } else {
                    setStyle("");
                }
            }
        });
    }

    private void configurarFiltrosEstudiantes() {
        cbFiltroGrado.valueProperty().addListener((obs, oldValue, newValue) -> aplicarFiltrosEstudiantes());
        cbFiltroEstado.valueProperty().addListener((obs, oldValue, newValue) -> aplicarFiltrosEstudiantes());
        txtBuscarEstudiante.textProperty().addListener((obs, oldText, newText) -> aplicarFiltrosEstudiantes());
    }

    // ─────────────────────────────────────────────────────────────
    // Carga de datos
    // ─────────────────────────────────────────────────────────────

    private void cargarEstudiantes() {
        baseEstudiantes.clear();
        baseEstudiantes.addAll(estudianteDAO.obtenerTodosConEstado());
        actualizarOpcionesFiltroGrado();
        aplicarFiltrosEstudiantes();
        actualizarResumenEstudiantes();
    }

    private void cargarLugares() {
        lugares.setAll(lugarDAO.obtenerTodosConEstado());
        actualizarResumenLugares();
    }

    private void cargarCasos() {
        casos.setAll(casoDAO.obtenerTodosConEstado());
        actualizarResumenCasos();
    }

    private void actualizarOpcionesFiltroGrado() {
        String actual = cbFiltroGrado.getValue();
        List<String> grados = new ArrayList<>();
        grados.add(FILTRO_TODOS);
        grados.addAll(baseEstudiantes.stream()
                .map(Estudiante::getGrado)
                .distinct()
                .sorted()
                .map(String::valueOf)
                .collect(Collectors.toList()));
        cbFiltroGrado.setItems(FXCollections.observableArrayList(grados));
        cbFiltroGrado.setValue(actual != null && grados.contains(actual) ? actual : FILTRO_TODOS);
    }

    private void aplicarFiltrosEstudiantes() {
        String filtroNombre = txtBuscarEstudiante.getText() == null ? "" : txtBuscarEstudiante.getText().trim().toUpperCase();
        String filtroGrado = cbFiltroGrado.getValue();
        String filtroEstado = cbFiltroEstado.getValue();

        List<Estudiante> filtrados = baseEstudiantes.stream()
                .filter(est -> {
                    if (filtroGrado == null || FILTRO_TODOS.equalsIgnoreCase(filtroGrado)) {
                        return true;
                    }
                    try {
                        return est.getGrado() == Integer.parseInt(filtroGrado);
                    } catch (NumberFormatException e) {
                        return true;
                    }
                })
                .filter(est -> {
                    if (filtroEstado == null || "Todos".equalsIgnoreCase(filtroEstado)) {
                        return true;
                    }
                    if ("Activos".equalsIgnoreCase(filtroEstado)) {
                        return est.getEstado() == 1;
                    }
                    if ("Inactivos".equalsIgnoreCase(filtroEstado)) {
                        return est.getEstado() == 0;
                    }
                    return true;
                })
                .filter(est -> filtroNombre.isEmpty() || nombreCompleto(est).toUpperCase().contains(filtroNombre))
                .collect(Collectors.toList());

        estudiantesFiltrados.setAll(filtrados);
        actualizarResumenEstudiantes();
    }

    // ─────────────────────────────────────────────────────────────
    // Estudiantes
    // ─────────────────────────────────────────────────────────────

    @FXML
    void clickNuevoEstudiante() {
        estudianteSeleccionado = null;
        tblEstudiantes.getSelectionModel().clearSelection();
        limpiarFormularioEstudiante();
        actualizarModoEstudiante();
    }

    @FXML
    void clickGuardarEstudiante() {
        String identificacionTxt = txtIdentificacion.getText() == null ? "" : txtIdentificacion.getText().trim();
        String gradoTxt = txtGrado.getText() == null ? "" : txtGrado.getText().trim();
        String apellido1 = txtApellido1.getText().toUpperCase();
        String apellido2 = txtApellido2.getText().toUpperCase();
        String nombre1 = txtNombre1.getText().toUpperCase();
        String nombre2 = txtNombre2.getText().toUpperCase();
        String genero = cbGenero.getValue() == null ? "" : cbGenero.getValue().toUpperCase();

        if (identificacionTxt.isEmpty() || gradoTxt.isEmpty()) {
            Alertas.mostrarError("Identificación y grado son obligatorios.");
            return;
        }

        if (genero.isEmpty()) {
            Alertas.mostrarError("Debe seleccionar un género.");
            return;
        }

        try {
            int identificacion = Integer.parseInt(identificacionTxt);
            int grado = Integer.parseInt(gradoTxt);
            if (grado <= 0) {
                Alertas.mostrarError("El grado debe ser mayor que 0.");
                return;
            }

            if (nombre1 == null || nombre1.trim().isEmpty() || apellido1 == null || apellido1.trim().isEmpty()) {
                Alertas.mostrarError("El estudiante debe tener al menos nombre 1 y apellido 1.");
                return;
            }

            Estudiante estudiante = new Estudiante();
            estudiante.setId(estudianteSeleccionado != null ? estudianteSeleccionado.getId() : 0);
            estudiante.setIdentificacion(identificacion);
            estudiante.setGrado(grado);
            estudiante.setApellido1(valorSeguro(apellido1));
            estudiante.setApellido2(valorSeguro(apellido2));
            estudiante.setNombre1(valorSeguro(nombre1));
            estudiante.setNombre2(valorSeguro(nombre2));
            estudiante.setGenero(genero);
            if (estudianteSeleccionado != null) {
                estudiante.setEstado(estudianteSeleccionado.getEstado());
                estudiante.setAño_escolar(estudianteSeleccionado.getAño_escolar());
            }

            boolean guardado;
            if (estudianteSeleccionado == null) {
                if (estudianteDAO.existeIdentificacion(identificacion)) {
                    Alertas.mostrarError("Ya existe un estudiante con esa identificación.");
                    return;
                }
                guardado = estudianteDAO.agregarEstudiante(estudiante);
            } else {
                if (estudianteDAO.existeIdentificacionEnOtroRegistro(identificacion, estudianteSeleccionado.getId())) {
                    Alertas.mostrarError("Ya existe otro estudiante con esa identificación.");
                    return;
                }
                guardado = estudianteDAO.actualizarEstudiante(estudiante);
            }

            if (guardado) {
                Alertas.mostrarExito(estudianteSeleccionado == null ? "Estudiante agregado correctamente." : "Estudiante actualizado correctamente.");
                cargarEstudiantes();
                clickNuevoEstudiante();
            }
        } catch (NumberFormatException e) {
            Alertas.mostrarError("Identificación y grado deben ser números válidos.");
        }
    }

    private void cargarEstudianteEnFormulario(Estudiante nuevo) {
        if (nuevo == null) {
            limpiarFormularioEstudiante();
            return;
        }

        txtIdentificacion.setText(String.valueOf(nuevo.getIdentificacion()));
        txtGrado.setText(String.valueOf(nuevo.getGrado()));
        txtApellido1.setText(valorSeguro(nuevo.getApellido1()));
        txtApellido2.setText(valorSeguro(nuevo.getApellido2()));
        txtNombre1.setText(valorSeguro(nuevo.getNombre1()));
        txtNombre2.setText(valorSeguro(nuevo.getNombre2()));
        cbGenero.setValue(valorSeguro(nuevo.getGenero()));
        actualizarModoEstudiante();
    }

    @FXML
    void clickAlternarEstadoEstudiante() {
        if (estudianteSeleccionado == null) {
            Alertas.mostrarError("Seleccione un estudiante para cambiar su estado.");
            return;
        }

        int idEstudianteActual = estudianteSeleccionado.getId();
        int nuevoEstado = estudianteSeleccionado.getEstado() == 1 ? 0 : 1;
        if (estudianteDAO.cambiarEstadoEstudiante(idEstudianteActual, nuevoEstado)) {
            Alertas.mostrarExito("Estado del estudiante actualizado correctamente.");
            cargarEstudiantes();
            // Buscar y seleccionar nuevamente el estudiante actualizado
            for (Estudiante est : estudiantesFiltrados) {
                if (est.getId() == idEstudianteActual) {
                    tblEstudiantes.getSelectionModel().select(est);
                    cargarEstudianteEnFormulario(est);
                    break;
                }
            }
        }
    }

    private void limpiarFormularioEstudiante() {
        txtIdentificacion.clear();
        txtGrado.clear();
        txtApellido1.clear();
        txtApellido2.clear();
        txtNombre1.clear();
        txtNombre2.clear();
        cbGenero.setValue(null);
    }

    private void actualizarModoEstudiante() {
        if (btnGuardarEstudiante != null) {
            btnGuardarEstudiante.setText(estudianteSeleccionado == null ? "Guardar estudiante" : "Actualizar estudiante");
        }
        if (btnAlternarEstadoEstudiante != null) {
            btnAlternarEstadoEstudiante.setDisable(estudianteSeleccionado == null);
        }
        if (lblResumenEstudiantes != null) {
            if (estudianteSeleccionado == null) {
                lblResumenEstudiantes.setText("Seleccione un estudiante para editarlo o use guardar para crear uno nuevo.");
            } else {
                lblResumenEstudiantes.setText("Editando estudiante: " + nombreCompleto(estudianteSeleccionado) + " | Estado: " + estadoTexto(estudianteSeleccionado.getEstado()));
            }
        }
    }

    private void actualizarResumenEstudiantes() {
        if (lblResumenEstudiantes == null) return;
        String estado = cbFiltroEstado.getValue() != null ? cbFiltroEstado.getValue() : "Todos";
        lblResumenEstudiantes.setText("Estudiantes cargados: " + estudiantesFiltrados.size() + " | Grado: " + valorSeguro(cbFiltroGrado.getValue()) + " | Estado: " + estado);
    }

    // ─────────────────────────────────────────────────────────────
    // Lugares
    // ─────────────────────────────────────────────────────────────

    @FXML
    void clickNuevoLugar() {
        lugarSeleccionado = null;
        tblLugares.getSelectionModel().clearSelection();
        limpiarFormularioLugar();
        actualizarModoLugar();
    }

    @FXML
    void clickGuardarLugar() {
        String nombre = txtNombreLugar.getText() == null ? "" : txtNombreLugar.getText().trim().toUpperCase();
        if (nombre.isEmpty()) {
            Alertas.mostrarError("Debe ingresar el nombre del lugar.");
            return;
        }

        boolean ok;
        if (lugarSeleccionado == null) {
            ok = lugarDAO.agregarLugar(nombre);
        } else {
            Lugar lugar = new Lugar(lugarSeleccionado.getId(), nombre, lugarSeleccionado.getEstado());
            ok = lugarDAO.actualizarLugar(lugar);
        }

        if (ok) {
            Alertas.mostrarExito(lugarSeleccionado == null ? "Lugar agregado correctamente." : "Lugar actualizado correctamente.");
            cargarLugares();
            clickNuevoLugar();
        }
    }

    @FXML
    void clickAlternarEstadoLugar() {
        if (lugarSeleccionado == null) {
            Alertas.mostrarError("Seleccione un lugar para cambiar su estado.");
            return;
        }

        int nuevoEstado = lugarSeleccionado.getEstado() == 1 ? 0 : 1;
        if (lugarDAO.cambiarEstadoLugar(lugarSeleccionado.getId(), nuevoEstado)) {
            Alertas.mostrarExito("Estado del lugar actualizado correctamente.");
            cargarLugares();
            clickNuevoLugar();
        }
    }

    private void cargarLugarEnFormulario(Lugar lugar) {
        if (lugar == null) {
            limpiarFormularioLugar();
            return;
        }

        txtNombreLugar.setText(valorSeguro(lugar.getNombreLugar()));
        if (lblEstadoLugar != null) {
            lblEstadoLugar.setText("Estado actual: " + estadoTexto(lugar.getEstado()));
        }
        actualizarModoLugar();
    }

    private void limpiarFormularioLugar() {
        txtNombreLugar.clear();
        if (lblEstadoLugar != null) {
            lblEstadoLugar.setText("Seleccione un lugar para editarlo o use guardar para crear uno nuevo.");
        }
    }

    private void actualizarModoLugar() {
        if (btnGuardarLugar != null) {
            btnGuardarLugar.setText(lugarSeleccionado == null ? "Agregar lugar" : "Actualizar lugar");
        }
        if (btnAlternarEstadoLugar != null) {
            btnAlternarEstadoLugar.setDisable(lugarSeleccionado == null);
        }
        if (lblResumenLugares != null) {
            if (lugarSeleccionado == null) {
                lblResumenLugares.setText("Lugares cargados: " + lugares.size());
            } else {
                lblResumenLugares.setText("Seleccionado: " + valorSeguro(lugarSeleccionado.getNombreLugar()) + " | Estado: " + estadoTexto(lugarSeleccionado.getEstado()));
            }
        }
    }

    private void actualizarResumenLugares() {
        if (lblResumenLugares != null) {
            lblResumenLugares.setText("Lugares cargados: " + lugares.size());
        }
    }

    // ─────────────────────────────────────────────────────────────
    // Casos
    // ─────────────────────────────────────────────────────────────

    @FXML
    void clickNuevoCaso() {
        casoSeleccionado = null;
        tblCasos.getSelectionModel().clearSelection();
        limpiarFormularioCaso();
        actualizarModoCaso();
    }

    @FXML
    void clickGuardarCaso() {
        String nombre = txtNombreCaso.getText() == null ? "" : txtNombreCaso.getText().trim().toUpperCase();
        if (nombre.isEmpty()) {
            Alertas.mostrarError("Debe ingresar el nombre del caso.");
            return;
        }

        boolean ok;
        if (casoSeleccionado == null) {
            ok = casoDAO.agregarCaso(nombre);
        } else {
            Caso caso = new Caso(casoSeleccionado.getId(), nombre, casoSeleccionado.getEstado());
            ok = casoDAO.actualizarCaso(caso);
        }

        if (ok) {
            Alertas.mostrarExito(casoSeleccionado == null ? "Caso agregado correctamente." : "Caso actualizado correctamente.");
            cargarCasos();
            clickNuevoCaso();
        }
    }

    @FXML
    void clickAlternarEstadoCaso() {
        if (casoSeleccionado == null) {
            Alertas.mostrarError("Seleccione un caso para cambiar su estado.");
            return;
        }

        int nuevoEstado = casoSeleccionado.getEstado() == 1 ? 0 : 1;
        if (casoDAO.cambiarEstadoCaso(casoSeleccionado.getId(), nuevoEstado)) {
            Alertas.mostrarExito("Estado del caso actualizado correctamente.");
            cargarCasos();
            clickNuevoCaso();
        }
    }

    private void cargarCasoEnFormulario(Caso caso) {
        if (caso == null) {
            limpiarFormularioCaso();
            return;
        }

        txtNombreCaso.setText(valorSeguro(caso.getNombreCaso()));
        if (lblEstadoCaso != null) {
            lblEstadoCaso.setText("Estado actual: " + estadoTexto(caso.getEstado()));
        }
        actualizarModoCaso();
    }

    private void limpiarFormularioCaso() {
        txtNombreCaso.clear();
        if (lblEstadoCaso != null) {
            lblEstadoCaso.setText("Seleccione un caso para editarlo o use guardar para crear uno nuevo.");
        }
    }

    private void actualizarModoCaso() {
        if (btnGuardarCaso != null) {
            btnGuardarCaso.setText(casoSeleccionado == null ? "Agregar caso" : "Actualizar caso");
        }
        if (btnAlternarEstadoCaso != null) {
            btnAlternarEstadoCaso.setDisable(casoSeleccionado == null);
        }
        if (lblResumenCasos != null) {
            if (casoSeleccionado == null) {
                lblResumenCasos.setText("Casos cargados: " + casos.size());
            } else {
                lblResumenCasos.setText("Seleccionado: " + valorSeguro(casoSeleccionado.getNombreCaso()) + " | Estado: " + estadoTexto(casoSeleccionado.getEstado()));
            }
        }
    }

    private void actualizarResumenCasos() {
        if (lblResumenCasos != null) {
            lblResumenCasos.setText("Casos cargados: " + casos.size());
        }
    }

    // ─────────────────────────────────────────────────────────────
    // Importación CSV
    // ─────────────────────────────────────────────────────────────

    @FXML
    void clickSeleccionarCSV() {
        FileChooser fc = new FileChooser();
        fc.setTitle("Seleccionar archivo CSV de estudiantes");
        fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("Archivos CSV", "*.csv", "*.CSV"));
        File archivo = fc.showOpenDialog(null);

        if (archivo != null) {
            archivoCSVSeleccionado = archivo;
            lblEstadoImport.setText("Archivo seleccionado: " + archivo.getName() + " (" + (archivo.length() / 1024) + " KB)");
        }
    }

    @FXML
    void clickImportarCSV() {
        if (archivoCSVSeleccionado == null) {
            Alertas.mostrarError("Debe seleccionar un archivo CSV primero.");
            return;
        }

        Integer año_escolar = cbAñoEscolarImport.getValue();
        if (año_escolar == null) {
            Alertas.mostrarError("Debe seleccionar un año escolar.");
            return;
        }

        procesarImportCSV(archivoCSVSeleccionado, año_escolar);
    }

    private void procesarImportCSV(File archivo, int año_escolar) {
        int insertados = 0;
        int actualizados = 0;
        int errores = 0;
        Set<Integer> identificacionesEnCSV = new HashSet<>();

        try (BufferedReader br = new BufferedReader(new FileReader(archivo))) {
            String linea;
            int numLinea = 0;

            // Saltar encabezado
            br.readLine();

            while ((linea = br.readLine()) != null) {
                numLinea++;
                linea = linea.trim();

                if (linea.isEmpty()) continue;

                String[] datos = linea.split(";");

                // Validar que tenga los 7 campos requeridos
                if (datos.length < 7) {
                    System.err.println("Línea " + (numLinea + 1) + ": Formato incorrecto, ignorada.");
                    errores++;
                    continue;
                }

                try {
                    int identificacion = Integer.parseInt(datos[0].trim());
                    int grado = Integer.parseInt(datos[1].trim());
                    String apellido1 = datos[2].trim();
                    String apellido2 = datos[3].trim();
                    String nombre1 = datos[4].trim();
                    String nombre2 = datos[5].trim();
                    String genero = datos[6].trim();

                    // Validaciones básicas
                    if (apellido1.isEmpty() || nombre1.isEmpty()) {
                        errores++;
                        continue;
                    }

                    identificacionesEnCSV.add(identificacion);

                    // Buscar si el estudiante ya existe
                    Estudiante existente = estudianteDAO.obtenerPorIdentificacion(identificacion);

                    if (existente != null) {
                        // Actualizar: cambiar grado + año_escolar, activar si estaba inactivo
                        existente.setGrado(grado);
                        existente.setAño_escolar(año_escolar);
                        existente.setEstado(1);
                        existente.setApellido1(apellido1);
                        existente.setApellido2(apellido2);
                        existente.setNombre1(nombre1);
                        existente.setNombre2(nombre2);
                        existente.setGenero(genero);

                        if (estudianteDAO.actualizarEstudiante(existente)) {
                            actualizados++;
                        } else {
                            errores++;
                        }
                    } else {
                        // Insertar nuevo
                        Estudiante nuevo = new Estudiante();
                        nuevo.setIdentificacion(identificacion);
                        nuevo.setGrado(grado);
                        nuevo.setApellido1(apellido1);
                        nuevo.setApellido2(apellido2);
                        nuevo.setNombre1(nombre1);
                        nuevo.setNombre2(nombre2);
                        nuevo.setGenero(genero);
                        nuevo.setEstado(1);
                        nuevo.setAño_escolar(año_escolar);

                        if (estudianteDAO.agregarEstudiante(nuevo)) {
                            insertados++;
                        } else {
                            errores++;
                        }
                    }

                } catch (NumberFormatException e) {
                    System.err.println("Línea " + (numLinea + 1) + ": Error al parsear datos numéricos.");
                    errores++;
                }
            }

            // Segundo paso: marcar como inactivos los estudiantes activos NO en el CSV
            List<Estudiante> todosActivos = estudianteDAO.obtenerTodosConEstado();
            int marcadosInactivos = 0;

            for (Estudiante est : todosActivos) {
                if (est.getEstado() == 1 && !identificacionesEnCSV.contains(est.getIdentificacion())) {
                    if (estudianteDAO.cambiarEstadoEstudiante(est.getId(), 0)) {
                        marcadosInactivos++;
                    }
                }
            }

            String mensaje = String.format(
                "Import completado:\n\n" +
                "✓ Insertados: %d\n" +
                "✓ Actualizados: %d\n" +
                "⚠ Marcados inactivos: %d\n" +
                "✗ Errores: %d",
                insertados, actualizados, marcadosInactivos, errores
            );

            Alertas.mostrarExito(mensaje);

            // Recargar datos y limpiar
            cargarEstudiantes();
            archivoCSVSeleccionado = null;
            lblEstadoImport.setText("Ningún archivo seleccionado");

        } catch (IOException e) {
            Alertas.mostrarError("Error al leer el archivo CSV: " + e.getMessage());
        }
    }

    // ─────────────────────────────────────────────────────────────
    // Utilidades
    // ─────────────────────────────────────────────────────────────

    private String estadoTexto(int estado) {
        return estado == 1 ? "Activo" : "Inactivo";
    }

    private String nombreCompleto(Estudiante estudiante) {
        if (estudiante == null) return "";
        return (valorSeguro(estudiante.getNombre1()) + " " + valorSeguro(estudiante.getNombre2()) + " " +
                valorSeguro(estudiante.getApellido1()) + " " + valorSeguro(estudiante.getApellido2()))
                .replaceAll("\\s+", " ")
                .trim();
    }

    private String valorSeguro(String valor) {
        return valor == null ? "" : valor.trim();
    }
}

