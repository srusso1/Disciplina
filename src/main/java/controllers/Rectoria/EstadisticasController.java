package controllers.Rectoria;

import database.CasoDAO;
import database.FaltaDAO;
import database.models.Caso;
import database.models.FaltaConsultaRow;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.Chart;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

public class EstadisticasController {

    private static final double TICK_LABEL_FONT_SIZE = 12.0;
    private static final List<String> PALETA_BARRAS = Arrays.asList(
            "#295C7A",
            "#3D8DBC",
            "#47A3A5",
            "#5FB49C",
            "#7EA36D",
            "#D9A441",
            "#D97D54",
            "#C56B8B",
            "#8F6BB3",
            "#6B7280"
    );
    private static final List<String> PALETA_PIE = Arrays.asList(
            "#295C7A",
            "#3D8DBC",
            "#47A3A5",
            "#5FB49C",
            "#7EA36D",
            "#D9A441",
            "#D97D54",
            "#C56B8B"
    );
    private static final String COLOR_LINEA = "#295C7A";

    @FXML
    private DatePicker dateDesde;

    @FXML
    private DatePicker dateHasta;

    @FXML
    private ComboBox<Caso> comboCasoComparativa;

    @FXML
    private ComboBox<Integer> comboAnio1Comparativa;

    @FXML
    private ComboBox<Integer> comboAnio2Comparativa;

    @FXML
    private VBox vbComparativasContainer;

    @FXML
    private BarChart<Number, String> chartFaltasPorCaso;

    @FXML
    private PieChart chartFaltasPorTipo;

    @FXML
    private BarChart<Number, String> chartFaltasPorLugar;

    @FXML
    private BarChart<Number, String> chartFaltasPorGrado;

    @FXML
    private LineChart<String, Number> chartEvolucionTemporal;

    @FXML
    private BarChart<Number, String> chartTop10Estudiantes;

    @FXML
    private BarChart<String, Number> chartFaltasPorGenero;

    @FXML
    private BarChart<Number, String> chartTop10Docentes;

    private static final DateTimeFormatter FORMATO_FECHA_UI = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final List<String> NOMBRES_MESES = Arrays.asList(
            "Enero", "Febrero", "Marzo", "Abril", "Mayo", "Junio",
            "Julio", "Agosto", "Septiembre", "Octubre", "Noviembre", "Diciembre"
    );

    private final FaltaDAO faltaDAO = new FaltaDAO();
    private final CasoDAO casoDAO = new CasoDAO();

    @FXML
    void initialize() {
        configurarEstilosGraficos();
        configurarComparativas();
        cargarGraficos();
        cargarComparativas();

        dateDesde.valueProperty().addListener((obs, oldVal, newVal) -> cargarGraficos());
        dateHasta.valueProperty().addListener((obs, oldVal, newVal) -> cargarGraficos());
    }

    private void configurarComparativas() {
        if (comboCasoComparativa == null || comboAnio1Comparativa == null || comboAnio2Comparativa == null) {
            return;
        }

        List<Caso> casos = new ArrayList<>();
        casos.add(new Caso(0, "Todos los casos"));
        casos.addAll(casoDAO.obtenerTodos());

        comboCasoComparativa.setItems(FXCollections.observableArrayList(casos));
        comboCasoComparativa.getSelectionModel().selectFirst();

        List<Integer> anios = faltaDAO.obtenerAniosRegistrados();
        comboAnio1Comparativa.setItems(FXCollections.observableArrayList(anios));
        comboAnio2Comparativa.setItems(FXCollections.observableArrayList(anios));

        if (!anios.isEmpty()) {
            comboAnio1Comparativa.getSelectionModel().select(anios.size() >= 2 ? anios.size() - 2 : 0);
            comboAnio2Comparativa.getSelectionModel().select(anios.size() - 1);
        }

        comboCasoComparativa.valueProperty().addListener((obs, oldVal, newVal) -> cargarComparativas());
        comboAnio1Comparativa.valueProperty().addListener((obs, oldVal, newVal) -> cargarComparativas());
        comboAnio2Comparativa.valueProperty().addListener((obs, oldVal, newVal) -> cargarComparativas());
    }

    private void cargarComparativas() {
        if (vbComparativasContainer == null || comboCasoComparativa == null || comboAnio1Comparativa == null || comboAnio2Comparativa == null) {
            return;
        }

        vbComparativasContainer.getChildren().clear();

        Integer anio1 = comboAnio1Comparativa.getValue();
        Integer anio2 = comboAnio2Comparativa.getValue();

        if (anio1 == null || anio2 == null) {
            vbComparativasContainer.getChildren().add(crearEtiquetaSinDatos("Seleccione dos años para realizar la comparativa."));
            return;
        }

        if (anio1.equals(anio2)) {
            vbComparativasContainer.getChildren().add(crearEtiquetaSinDatos("Seleccione dos años distintos para comparar."));
            return;
        }

        Caso casoSeleccionado = comboCasoComparativa.getValue();
        if (casoSeleccionado != null && casoSeleccionado.getId() != 0) {
            if (!agregarComparativaCaso(casoSeleccionado, anio1, anio2)) {
                vbComparativasContainer.getChildren().add(crearEtiquetaSinDatos("No hay registros para este caso."));
            }
            return;
        }

        boolean hayContenido = false;
        for (Caso caso : casoDAO.obtenerTodos()) {
            if (agregarComparativaCaso(caso, anio1, anio2)) {
                hayContenido = true;
            }
        }

        if (!hayContenido) {
            vbComparativasContainer.getChildren().add(crearEtiquetaSinDatos("No hay datos comparativos para mostrar."));
        }
    }

    private boolean agregarComparativaCaso(Caso caso, int anio1, int anio2) {
        // Obtener faltas para verificar si hay datos
        List<FaltaConsultaRow> faltas = faltaDAO.obtenerFaltasComparativa(caso.getId());
        if (faltas.isEmpty()) {
            return false;
        }

        Label titulo = new Label("Caso " + caso.getId() + " - " + caso.getNombreCaso());
        titulo.getStyleClass().add("subtitulo-seccion");

        TableView<FilaComparativaMes> tabla = construirTablaComparativa(caso.getId(), anio1, anio2);
        if (tabla == null) {
            return false;
        }

        VBox bloque = new VBox(12, titulo, tabla);
        bloque.getStyleClass().add("form-card");
        bloque.setStyle("-fx-padding: 16; -fx-background-color: #ffffff; -fx-background-radius: 8; -fx-border-color: #dee2e6; -fx-border-radius: 8;");

        vbComparativasContainer.getChildren().add(bloque);
        return true;
    }

    private TableView<FilaComparativaMes> construirTablaComparativa(Integer idCaso, int anio1, int anio2) {
        // Obtener conteo correcto por mes y año, combinando faltas + faltas_historico
        Map<Integer, Map<Integer, Integer>> conteoPorMes = faltaDAO.obtenerConteoPorMesYAnio(idCaso);

        if (conteoPorMes.isEmpty()) {
            return null;
        }

        if (conteoPorMes.isEmpty()) {
            return null;
        }

        List<FilaComparativaMes> filas = new ArrayList<>();
        Map<Integer, Integer> totalPorAnio = new LinkedHashMap<>();
        totalPorAnio.put(anio1, 0);
        totalPorAnio.put(anio2, 0);

        for (int mes = 1; mes <= 12; mes++) {
            Map<Integer, Integer> valores = new LinkedHashMap<>();
            int totalMes = 0;

            int valorAnio1 = obtenerValorMes(conteoPorMes, mes, anio1);
            int valorAnio2 = obtenerValorMes(conteoPorMes, mes, anio2);
            valores.put(anio1, valorAnio1);
            valores.put(anio2, valorAnio2);
            totalMes = valorAnio1 + valorAnio2;
            totalPorAnio.put(anio1, totalPorAnio.get(anio1) + valorAnio1);
            totalPorAnio.put(anio2, totalPorAnio.get(anio2) + valorAnio2);

            filas.add(new FilaComparativaMes(NOMBRES_MESES.get(mes - 1), valores, totalMes));
        }

        int totalGeneral = 0;
        for (Integer total : totalPorAnio.values()) {
            totalGeneral += total;
        }
        filas.add(new FilaComparativaMes("TOTAL", totalPorAnio, totalGeneral));

        TableView<FilaComparativaMes> tabla = new TableView<>();
        tabla.getStyleClass().add("tabla-datos");
        tabla.setItems(FXCollections.observableArrayList(filas));
        tabla.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        tabla.setPrefHeight(390.0);

        TableColumn<FilaComparativaMes, String> colMes = new TableColumn<>("MES");
        colMes.setCellValueFactory(data -> new ReadOnlyStringWrapper(data.getValue().getMes()));
        tabla.getColumns().add(colMes);

        TableColumn<FilaComparativaMes, String> colAnio1 = new TableColumn<>(String.valueOf(anio1));
        colAnio1.setCellValueFactory(data -> new ReadOnlyStringWrapper(String.valueOf(data.getValue().getValor(anio1))));
        tabla.getColumns().add(colAnio1);

        TableColumn<FilaComparativaMes, String> colAnio2 = new TableColumn<>(String.valueOf(anio2));
        colAnio2.setCellValueFactory(data -> new ReadOnlyStringWrapper(String.valueOf(data.getValue().getValor(anio2))));
        tabla.getColumns().add(colAnio2);

        return tabla;
    }

    private int obtenerValorMes(Map<Integer, Map<Integer, Integer>> conteoPorMes, int mes, int anio) {
        if (!conteoPorMes.containsKey(mes)) {
            return 0;
        }
        Map<Integer, Integer> conteoAnual = conteoPorMes.get(mes);
        Integer valor = conteoAnual.get(anio);
        return valor != null ? valor : 0;
    }

    private Label crearEtiquetaSinDatos(String texto) {
        Label label = new Label(texto);
        label.getStyleClass().add("descripcion-seccion");
        label.setWrapText(true);
        return label;
    }

    private void programarRefrescoVisual() {
        Platform.runLater(this::refrescarEstilosGraficos);
    }

    private void refrescarEstilosGraficos() {
        refrescarGrafico(chartFaltasPorCaso);
        refrescarGrafico(chartFaltasPorTipo);
        refrescarGrafico(chartFaltasPorLugar);
        refrescarGrafico(chartFaltasPorGrado);
        refrescarGrafico(chartEvolucionTemporal);
        refrescarGrafico(chartTop10Estudiantes);
        refrescarGrafico(chartFaltasPorGenero);
        refrescarGrafico(chartTop10Docentes);

        aplicarColoresBarras(chartFaltasPorCaso, 0);
        aplicarColoresPie(chartFaltasPorTipo);
        aplicarColoresBarras(chartFaltasPorLugar, 1);
        aplicarColoresBarras(chartFaltasPorGrado, 2);
        aplicarColorLinea(chartEvolucionTemporal);
        aplicarColoresBarras(chartTop10Estudiantes, 3);
        aplicarColoresBarras(chartFaltasPorGenero, 4);
        aplicarColoresBarras(chartTop10Docentes, 5);
    }

    private void refrescarGrafico(Chart chart) {
        chart.applyCss();
        chart.layout();
    }

    private void configurarEstilosGraficos() {
        configurarGraficoBarras(chartFaltasPorCaso, "grafico-caso");
        configurarGraficoPie(chartFaltasPorTipo, "grafico-tipo");
        configurarGraficoBarras(chartFaltasPorLugar, "grafico-lugar");
        configurarGraficoBarras(chartFaltasPorGrado, "grafico-grado");
        configurarGraficoLinea(chartEvolucionTemporal, "grafico-evolucion");
        configurarGraficoBarras(chartTop10Estudiantes, "grafico-estudiantes");
        configurarGraficoBarras(chartFaltasPorGenero, "grafico-genero");
        configurarGraficoBarras(chartTop10Docentes, "grafico-docentes");
    }

    private void configurarGraficoBase(Chart chart, String claseEspecifica) {
        chart.getStyleClass().add("estadisticas-chart");
        chart.getStyleClass().add(claseEspecifica);
        chart.setAnimated(false);
        chart.setFocusTraversable(false);
    }

    private void configurarGraficoBarras(BarChart<?, ?> chart, String claseEspecifica) {
        configurarGraficoBase(chart, claseEspecifica);
        chart.setCategoryGap(12.0);
        chart.setBarGap(4.0);

        if (chart.getXAxis() instanceof CategoryAxis) {
            CategoryAxis categoria = (CategoryAxis) chart.getXAxis();
            categoria.setTickLabelFont(Font.font("Roboto", TICK_LABEL_FONT_SIZE));
        }
        if (chart.getYAxis() instanceof CategoryAxis) {
            CategoryAxis categoria = (CategoryAxis) chart.getYAxis();
            categoria.setTickLabelFont(Font.font("Roboto", TICK_LABEL_FONT_SIZE));
        }
    }

    private void configurarGraficoPie(PieChart chart, String claseEspecifica) {
        configurarGraficoBase(chart, claseEspecifica);
        chart.setLegendVisible(true);
    }

    private void configurarGraficoLinea(LineChart<String, Number> chart, String claseEspecifica) {
        configurarGraficoBase(chart, claseEspecifica);
        if (chart.getXAxis() instanceof CategoryAxis) {
            CategoryAxis categoria = (CategoryAxis) chart.getXAxis();
            categoria.setTickLabelFont(Font.font("Roboto", 14.0));
        }
        if (chart.getYAxis() instanceof NumberAxis) {
            NumberAxis numerico = (NumberAxis) chart.getYAxis();
            numerico.setTickLabelFont(Font.font("Roboto", TICK_LABEL_FONT_SIZE));
        }
    }

    private void aplicarColoresBarras(BarChart<?, ?> chart, int offsetColor) {
        int indice = 0;

        for (XYChart.Series<?, ?> serie : chart.getData()) {
            for (XYChart.Data<?, ?> data : serie.getData()) {
                String color = PALETA_BARRAS.get((indice + offsetColor) % PALETA_BARRAS.size());
                aplicarEstiloNodo(data, node -> {
                    node.setStyle("-fx-bar-fill: " + color + ";");
                    String textoTooltip = construirTextoTooltipBarra(data);
                    Tooltip tooltip = new Tooltip(textoTooltip);
                    tooltip.setStyle("-fx-font-size: 12px;");
                    Tooltip.install(node, tooltip);
                });
                indice++;
            }
        }
    }

    private String construirTextoTooltipBarra(XYChart.Data<?, ?> data) {
        Object xValue = data.getXValue();
        Object yValue = data.getYValue();
        Object extraValue = data.getExtraValue();

        if (extraValue instanceof String) {
            if (xValue instanceof Number) {
                return extraValue + ": " + xValue;
            }
            if (yValue instanceof Number) {
                return extraValue + ": " + yValue;
            }
        }

        if (xValue instanceof String) {
            return xValue + ": " + yValue;
        } else if (yValue instanceof String) {
            return yValue + ": " + xValue;
        } else {
            return xValue + " - " + yValue;
        }
    }

    private void aplicarColoresPie(PieChart chart) {
        int indice = 0;
        double total = chart.getData().stream().mapToDouble(PieChart.Data::getPieValue).sum();

        for (PieChart.Data data : chart.getData()) {
            String color = PALETA_PIE.get(indice % PALETA_PIE.size());
            double valor = data.getPieValue();
            double porcentaje = (valor / total) * 100;
            String textoTooltip = String.format("%s: %d (%.1f%%)", data.getName(), (int) valor, porcentaje);

            aplicarEstiloNodo(data, node -> {
                node.setStyle("-fx-pie-color: " + color + ";");
                Tooltip tooltip = new Tooltip(textoTooltip);
                tooltip.setStyle("-fx-font-size: 12px;");
                Tooltip.install(node, tooltip);
            });
            indice++;
        }
    }

    private void aplicarColorLinea(LineChart<String, Number> chart) {
        for (XYChart.Series<String, Number> serie : chart.getData()) {
            aplicarEstiloNodo(serie, node -> node.setStyle("-fx-stroke: " + COLOR_LINEA + ";"));

            for (XYChart.Data<String, Number> data : serie.getData()) {
                aplicarEstiloNodo(data, node -> {
                    node.setStyle("-fx-background-color: " + COLOR_LINEA + ", white;");
                    String textoTooltip = data.getXValue() + ": " + data.getYValue();
                    Tooltip tooltip = new Tooltip(textoTooltip);
                    tooltip.setStyle("-fx-font-size: 12px;");
                    Tooltip.install(node, tooltip);
                });
            }
        }
    }

    private void aplicarEstiloNodo(XYChart.Data<?, ?> data, java.util.function.Consumer<Node> aplicador) {
        if (data.getNode() != null) {
            aplicador.accept(data.getNode());
        }

        data.nodeProperty().addListener((obs, oldNode, newNode) -> {
            if (newNode != null) {
                aplicador.accept(newNode);
            }
        });
    }

    private void aplicarEstiloNodo(PieChart.Data data, java.util.function.Consumer<Node> aplicador) {
        if (data.getNode() != null) {
            aplicador.accept(data.getNode());
        }

        data.nodeProperty().addListener((obs, oldNode, newNode) -> {
            if (newNode != null) {
                aplicador.accept(newNode);
            }
        });
    }

    private void aplicarEstiloNodo(XYChart.Series<?, ?> serie, java.util.function.Consumer<Node> aplicador) {
        if (serie.getNode() != null) {
            aplicador.accept(serie.getNode());
        }

        serie.nodeProperty().addListener((obs, oldNode, newNode) -> {
            if (newNode != null) {
                aplicador.accept(newNode);
            }
        });
    }

    private void cargarGraficos() {
        String fechaDesde = dateDesde.getValue() != null ? dateDesde.getValue().toString() : null;
        String fechaHasta = dateHasta.getValue() != null ? dateHasta.getValue().toString() : null;

        cargarGraficoFaltasPorCaso(fechaDesde, fechaHasta);
        cargarGraficoFaltasPorTipo(fechaDesde, fechaHasta);
        cargarGraficoFaltasPorLugar(fechaDesde, fechaHasta);
        cargarGraficoFaltasPorGrado(fechaDesde, fechaHasta);
        cargarGraficoEvolucionTemporal(fechaDesde, fechaHasta);
        cargarGraficoTop10Estudiantes(fechaDesde, fechaHasta);
        cargarGraficoFaltasPorGenero(fechaDesde, fechaHasta);
        cargarGraficoTop10Docentes(fechaDesde, fechaHasta);

        programarRefrescoVisual();
    }

    private static final int MAX_CHARS_PER_LINE = 30;

    private String acortarNombreCaso(String nombreCaso) {
        if (nombreCaso == null) return "";

        Map<String, String> mapeoCasos = new LinkedHashMap<>();
        mapeoCasos.put("VIOLENCIA DIRECTA", "Violencia Directa");
        mapeoCasos.put("REDES SOCIALES", "Redes Sociales");
        mapeoCasos.put("CONSUMO DE VAPER", "Consumo de Vaper");
        mapeoCasos.put("FALSA INFORMACIÓN (CHISME)", "Falsa Información");
        mapeoCasos.put("AGRESIÓN VERBAL", "Agresión Verbal");
        mapeoCasos.put("INADECUADA GESTIÓN DE RELACIONES SENTIMENTALES", "Relaciones Sent.");
        mapeoCasos.put("ACTO SEXUAL ABUSIVO (TOCAMIENTO)", "Acto Sexual Abusivo");
        mapeoCasos.put("CONSUMO DE SPA", "Consumo de SPA");
        mapeoCasos.put("CONFLICTO POR AMENAZAS", "Conflicto Amenazas");
        mapeoCasos.put("VIOLENCIA DE GENERO", "Violencia de Género");
        mapeoCasos.put("INCUMPLIMIENTO DE NORMAS INSTITUCIONALES", "Incumplimiento Normas");

        String abreviado = mapeoCasos.getOrDefault(nombreCaso, nombreCaso);
        if (abreviado.length() <= MAX_CHARS_PER_LINE) {
            return abreviado;
        }
        return abreviado.substring(0, MAX_CHARS_PER_LINE - 3) + "...";
    }

    private String acortarNombreLugar(String nombreLugar) {
        if (nombreLugar == null) return "";

        Map<String, String> mapeoLugares = new LinkedHashMap<>();
        mapeoLugares.put("AULA DE CLASES", "Aula de Clases");
        mapeoLugares.put("CANCHA", "Cancha");
        mapeoLugares.put("FUERA DE LA INSTITUCIÓN", "Fuera Institución");
        mapeoLugares.put("BAÑO DE MUJERES", "Baño Mujeres");
        mapeoLugares.put("BAÑO DE HOMBRES", "Baño Hombres");
        mapeoLugares.put("ZONA DE CAFETERAS", "Cafetería");
        mapeoLugares.put("ZONA LATERAL DERECHA (ÁRBOLES DE MANGO)", "Zona Lateral Der.");
        mapeoLugares.put("ZONA LATERAL IZQUIERDA (DEPOSITO DE AGUA)", "Zona Lateral Izq.");
        mapeoLugares.put("PASILLOS PRIMER PISO", "Pasillo 1°");
        mapeoLugares.put("PASILLOS SEGUNDO PISO", "Pasillo 2°");
        mapeoLugares.put("COMEDOR ESCOLAR", "Comedor");
        mapeoLugares.put("JARDINERAS", "Jardineras");

        return mapeoLugares.getOrDefault(nombreLugar, nombreLugar);
    }

    private void cargarGraficoFaltasPorCaso(String fechaDesde, String fechaHasta) {
        chartFaltasPorCaso.getData().clear();
        Map<String, Integer> datos = faltaDAO.obtenerFaltasPorCaso(fechaDesde, fechaHasta);

        XYChart.Series<Number, String> series = new XYChart.Series<>();
        series.setName("Cantidad de Faltas");

        List<Map.Entry<String, Integer>> ordenDesc = new ArrayList<>(datos.entrySet());
        ordenDesc.sort((e1, e2) -> e2.getValue().compareTo(e1.getValue()));

        // En eje Y de barras horizontales, la primera categoría tiende a quedar abajo;
        // por eso invertimos la carga para que el mayor quede arriba visualmente.
        Collections.reverse(ordenDesc);

        List<String> categoriasOrdenadas = new ArrayList<>();
        for (Map.Entry<String, Integer> entry : ordenDesc) {
            String casoCorto = acortarNombreCaso(entry.getKey());
            categoriasOrdenadas.add(casoCorto);

            XYChart.Data<Number, String> data = new XYChart.Data<>(entry.getValue(), casoCorto);
            data.setExtraValue(entry.getKey());
            series.getData().add(data);
        }

        if (chartFaltasPorCaso.getYAxis() instanceof CategoryAxis) {
            CategoryAxis yAxis = (CategoryAxis) chartFaltasPorCaso.getYAxis();
            yAxis.setAutoRanging(false);
            yAxis.setCategories(FXCollections.observableArrayList(categoriasOrdenadas));
        }

        chartFaltasPorCaso.getData().add(series);
    }

    private static String wrapLabel(String text) {
        if (text == null || text.length() <= MAX_CHARS_PER_LINE) return text;

        String[] words = text.split("\\s+");
        StringBuilder line = new StringBuilder();
        StringBuilder out = new StringBuilder();

        for (String w : words) {
            if (line.length() == 0) {
                line.append(w);
            } else if (line.length() + 1 + w.length() <= MAX_CHARS_PER_LINE) {
                line.append(' ').append(w);
            } else {
                out.append(line).append('\n');
                line.setLength(0);
                line.append(w);
            }
        }
        if (line.length() > 0) out.append(line);
        return out.toString();
    }

    private void cargarGraficoFaltasPorTipo(String fechaDesde, String fechaHasta) {
        chartFaltasPorTipo.getData().clear();
        Map<Integer, Integer> datos = faltaDAO.obtenerFaltasPorTipo(fechaDesde, fechaHasta);

        datos.forEach((tipo, cantidad) ->
            chartFaltasPorTipo.getData().add(new PieChart.Data("Tipo " + tipo, cantidad))
        );
    }

    private void cargarGraficoFaltasPorLugar(String fechaDesde, String fechaHasta) {
        chartFaltasPorLugar.getData().clear();
        Map<String, Integer> datos = faltaDAO.obtenerFaltasPorLugar(fechaDesde, fechaHasta);

        XYChart.Series<Number, String> series = new XYChart.Series<>();
        series.setName("Cantidad de Faltas");

        // Ordenar por cantidad descendente
        datos.entrySet().stream()
            .sorted((e1, e2) -> e2.getValue().compareTo(e1.getValue()))
            .forEach((entry) -> {
                String lugarCorto = acortarNombreLugar(entry.getKey());
                series.getData().add(new XYChart.Data<>(entry.getValue(), lugarCorto));
            });

        chartFaltasPorLugar.getData().add(series);
    }

    private void cargarGraficoFaltasPorGrado(String fechaDesde, String fechaHasta) {
        chartFaltasPorGrado.getData().clear();
        Map<Integer, Integer> datos = faltaDAO.obtenerFaltasPorGrado(fechaDesde, fechaHasta);

        XYChart.Series<Number, String> series = new XYChart.Series<>();
        series.setName("Cantidad de Faltas");

        // Ordenar por cantidad descendente
        datos.entrySet().stream()
            .sorted((e1, e2) -> e2.getValue().compareTo(e1.getValue()))
            .forEach((entry) -> series.getData().add(new XYChart.Data<>(entry.getValue(), "Grado " + entry.getKey())));

        chartFaltasPorGrado.getData().add(series);
    }

    private void cargarGraficoEvolucionTemporal(String fechaDesde, String fechaHasta) {
        chartEvolucionTemporal.getData().clear();
        Map<String, Integer> datos = faltaDAO.obtenerFaltasPorMes(fechaDesde, fechaHasta);

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Evolución Mensual");
        datos.forEach((mes, cantidad) -> {
            String mesFormato = convertirMesALegible(mes);
            series.getData().add(new XYChart.Data<>(mesFormato, cantidad));
        });

        chartEvolucionTemporal.getData().add(series);
    }

    private String convertirMesALegible(String mesISO) {
        if (mesISO == null || mesISO.length() < 7) return mesISO;

        String[] meses = {
                "Enero", "Febrero", "Marzo", "Abril", "Mayo", "Junio",
                "Julio", "Agosto", "Septiembre", "Octubre", "Noviembre", "Diciembre"
        };

        try {
            String[] partes = mesISO.split("-");
            String anio = partes[0];
            int mesNum = Integer.parseInt(partes[1]) - 1;

            if (mesNum >= 0 && mesNum < meses.length) {
                return meses[mesNum] + "-" + anio;
            }
        } catch (Exception e) {
            return mesISO;
        }

        return mesISO;
    }

    private void cargarGraficoTop10Estudiantes(String fechaDesde, String fechaHasta) {
        chartTop10Estudiantes.getData().clear();
        Map<String, Integer> datos = faltaDAO.obtenerTop10Estudiantes(fechaDesde, fechaHasta);

        XYChart.Series<Number, String> series = new XYChart.Series<>();
        series.setName("Cantidad de Faltas");

        // Ordenar por cantidad descendente
        datos.entrySet().stream()
            .sorted((e1, e2) -> e2.getValue().compareTo(e1.getValue()))
            .forEach((entry) -> series.getData().add(new XYChart.Data<>(entry.getValue(), entry.getKey())));

        chartTop10Estudiantes.getData().add(series);
    }

    private void cargarGraficoFaltasPorGenero(String fechaDesde, String fechaHasta) {
        chartFaltasPorGenero.getData().clear();
        Map<String, Integer> datos = faltaDAO.obtenerFaltasPorGenero(fechaDesde, fechaHasta);

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Cantidad de Faltas");
        datos.forEach((genero, cantidad) -> series.getData().add(new XYChart.Data<>(genero, cantidad)));

        chartFaltasPorGenero.getData().add(series);
    }

    private void cargarGraficoTop10Docentes(String fechaDesde, String fechaHasta) {
        chartTop10Docentes.getData().clear();
        Map<String, Integer> datos = faltaDAO.obtenerTop10DocentesAula(fechaDesde, fechaHasta);

        XYChart.Series<Number, String> series = new XYChart.Series<>();
        series.setName("Faltas en Aula");

        // Ordenar por cantidad descendente
        datos.entrySet().stream()
            .sorted((e1, e2) -> e2.getValue().compareTo(e1.getValue()))
            .forEach((entry) -> series.getData().add(new XYChart.Data<>(entry.getValue(), entry.getKey())));

        chartTop10Docentes.getData().add(series);
    }

    private static class FilaComparativaMes {
        private final String mes;
        private final Map<Integer, Integer> valoresPorAnio;
        private final int total;

        FilaComparativaMes(String mes, Map<Integer, Integer> valoresPorAnio, int total) {
            this.mes = mes;
            this.valoresPorAnio = valoresPorAnio;
            this.total = total;
        }

        String getMes() {
            return mes;
        }

        Integer getValor(Integer anio) {
            Integer valor = valoresPorAnio.get(anio);
            return valor != null ? valor : 0;
        }

        Integer getTotal() {
            return total;
        }
    }
}

