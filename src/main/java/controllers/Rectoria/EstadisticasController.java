package controllers.Rectoria;

import database.FaltaDAO;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;

import java.util.Map;

public class EstadisticasController {

    @FXML
    private DatePicker dateDesde;

    @FXML
    private DatePicker dateHasta;

    @FXML
    private BarChart<Number, String> chartFaltasPorCaso;

    @FXML
    private PieChart chartFaltasPorTipo;

    @FXML
    private BarChart<String, Number> chartFaltasPorLugar;

    @FXML
    private BarChart<String, Number> chartFaltasPorGrado;

    @FXML
    private LineChart<String, Number> chartEvolucionTemporal;

    @FXML
    private BarChart<String, Number> chartTop10Estudiantes;

    @FXML
    private BarChart<String, Number> chartFaltasPorGenero;

    @FXML
    private BarChart<String, Number> chartTop10Docentes;

    private final FaltaDAO faltaDAO = new FaltaDAO();

    @FXML
    void initialize() {
        cargarGraficos();

        dateDesde.valueProperty().addListener((obs, oldVal, newVal) -> cargarGraficos());
        dateHasta.valueProperty().addListener((obs, oldVal, newVal) -> cargarGraficos());
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
    }

    private static final int MAX_CHARS_PER_LINE = 30;

    private void cargarGraficoFaltasPorCaso(String fechaDesde, String fechaHasta) {
        chartFaltasPorCaso.getData().clear();
        Map<String, Integer> datos = faltaDAO.obtenerFaltasPorCaso(fechaDesde, fechaHasta);

        XYChart.Series<Number, String> series = new XYChart.Series<>();
        series.setName("Cantidad de Faltas");
        datos.forEach((caso, cantidad) -> {
            String casoEnvuelto = wrapLabel(caso, MAX_CHARS_PER_LINE);
            XYChart.Data<Number, String> data = new XYChart.Data<>(cantidad, casoEnvuelto);
            // Tooltip con el texto completo sin envolver
            data.nodeProperty().addListener((obs, oldNode, newNode) -> {
                if (newNode != null) {
                    Tooltip.install(newNode, new Tooltip(caso + ": " + cantidad));
                }
            });
            series.getData().add(data);
        });

        chartFaltasPorCaso.getData().add(series);
    }

    private static String wrapLabel(String text, int maxCharsPerLine) {
        if (text == null || text.length() <= maxCharsPerLine) return text;

        String[] words = text.split("\\s+");
        StringBuilder line = new StringBuilder();
        StringBuilder out = new StringBuilder();

        for (String w : words) {
            if (line.length() == 0) {
                line.append(w);
            } else if (line.length() + 1 + w.length() <= maxCharsPerLine) {
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

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Cantidad de Faltas");
        datos.forEach((lugar, cantidad) -> series.getData().add(new XYChart.Data<>(lugar, cantidad)));

        chartFaltasPorLugar.getData().add(series);
    }

    private void cargarGraficoFaltasPorGrado(String fechaDesde, String fechaHasta) {
        chartFaltasPorGrado.getData().clear();
        Map<Integer, Integer> datos = faltaDAO.obtenerFaltasPorGrado(fechaDesde, fechaHasta);

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Cantidad de Faltas");
        datos.forEach((grado, cantidad) -> series.getData().add(new XYChart.Data<>("Grado " + grado, cantidad)));

        chartFaltasPorGrado.getData().add(series);
    }

    private void cargarGraficoEvolucionTemporal(String fechaDesde, String fechaHasta) {
        chartEvolucionTemporal.getData().clear();
        Map<String, Integer> datos = faltaDAO.obtenerFaltasPorMes(fechaDesde, fechaHasta);

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Evolución Mensual");
        datos.forEach((mes, cantidad) -> series.getData().add(new XYChart.Data<>(mes, cantidad)));

        chartEvolucionTemporal.getData().add(series);
    }

    private void cargarGraficoTop10Estudiantes(String fechaDesde, String fechaHasta) {
        chartTop10Estudiantes.getData().clear();
        Map<String, Integer> datos = faltaDAO.obtenerTop10Estudiantes(fechaDesde, fechaHasta);

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Cantidad de Faltas");
        datos.forEach((estudiante, cantidad) -> series.getData().add(new XYChart.Data<>(estudiante, cantidad)));

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

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Faltas en Aula");
        datos.forEach((docente, cantidad) -> series.getData().add(new XYChart.Data<>(docente, cantidad)));

        chartTop10Docentes.getData().add(series);
    }
}

