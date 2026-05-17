package reports.generators;

import com.itextpdf.layout.element.Table;
import database.FaltaDAO;
import reports.models.ReportConfig;

import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Genera un informe PDF con la distribución de faltas analizadas por grado.
 *
 * Estructura del PDF:
 *  1. Encabezado institucional estándar
 *  2. Bloque de parámetros (rango de fechas)
 *  3. Resumen general (total faltas, grados involucrados)
 *  4. Tabla principal: Faltas por Grado
 *  5. Para cada grado: Desglose de faltas por caso
 *  6. Pie de página con fecha de generación
 */
public class InformeFaltasPorGradoGenerator extends BaseReportGenerator {

    private final FaltaDAO faltaDAO = new FaltaDAO();
    private static final List<String> GRADOS_ESPERADOS = Arrays.asList(
            "601", "602", "603", "604", "605",
            "701", "702", "703", "704", "705",
            "801", "802", "803", "804", "805",
            "901", "902", "903", "904", "905",
            "1001", "1002", "1003", "1004", "1005",
            "1101", "1102", "1103", "1104", "1105"
    );

    public InformeFaltasPorGradoGenerator(ReportConfig config, String nombreArchivoSugerido) {
        super(config, nombreArchivoSugerido);
    }

    @Override
    public void generar() {
        if (!puedeGenerar()) return;

        String fechaDesde = config.getFechaInicio() != null ? config.getFechaInicio().toString() : null;
        String fechaHasta = config.getFechaFin() != null ? config.getFechaFin().toString() : null;

        agregarEncabezadoEstandar("INFORME DE FALTAS ANALIZADAS POR GRADO");
        agregarBloqueParametros(fechaDesde, fechaHasta);
        pdfBuilder.agregarEspacio(8);

        Map<Integer, Integer[]> faltasPorGrado = faltaDAO.obtenerFaltasPorGradoConEstudiantes(fechaDesde, fechaHasta);

        if (faltasPorGrado.isEmpty()) {
            pdfBuilder.agregarParrafoIndentado("No hay faltas registradas para el período seleccionado.");
            finalizarReporte();
            return;
        }

        agregarResumenEstadistico(faltasPorGrado);
        pdfBuilder.agregarEspacio(8);

        agregarTablaPrincipalPorGrado(faltasPorGrado);
        pdfBuilder.agregarEspacio(12);

        if (config.isIncluirTablas()) {
            agregarDesglosePorCasoEnCadaGrado(faltasPorGrado, fechaDesde, fechaHasta);
        }

        finalizarReporte();
    }

    // ──────────────────────────────────────────────────────────────────────────
    //  SECCIONES
    // ──────────────────────────────────────────────────────────────────────────

    private void agregarBloqueParametros(String fechaDesde, String fechaHasta) {
        pdfBuilder.agregarSeccion("Parámetros del Informe");

        String desde = config.getFechaInicio() != null
                ? config.getFechaInicio().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
                : "Sin límite";
        String hasta = config.getFechaFin() != null
                ? config.getFechaFin().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
                : "Sin límite";

        pdfBuilder.agregarLineaDetalle("Período", desde + "  →  " + hasta);
        pdfBuilder.agregarLineaDetalle("Tipo de análisis", "Distribución por Grado");
    }

    private void agregarResumenEstadistico(Map<Integer, Integer[]> faltasPorGrado) {
        pdfBuilder.agregarSeccion("Resumen General");

        int totalFaltas = 0;
        int totalEstudiantes = 0;
        int gradosInvolucrados = faltasPorGrado.size();

        for (Integer[] estadistica : faltasPorGrado.values()) {
            totalFaltas += estadistica[0];
            totalEstudiantes += estadistica[1]; // Suma aproximada (puede haber overlap)
        }

        pdfBuilder.agregarLineaDetalle("Total de faltas", String.valueOf(totalFaltas));
        pdfBuilder.agregarLineaDetalle("Grados involucrados", String.valueOf(gradosInvolucrados));
        pdfBuilder.agregarLineaDetalle("Estudiantes con al menos una falta", String.valueOf(totalEstudiantes));
    }

    /**
     * Tabla con resumen de faltas por grado, cantidad y estudiantes únicos.
     */
    private void agregarTablaPrincipalPorGrado(Map<Integer, Integer[]> faltasPorGrado) {
        pdfBuilder.agregarSeccion("Distribución de Faltas por Grado");

        float[] anchos = {2f, 3f, 4f, 3f};
        String[] encabezados = {"Grado", "Faltas", "Estudiantes", "Porcentaje"};
        Table tabla = pdfBuilder.crearTabla(anchos, encabezados, HEADER_COLOR);

        int totalFaltas = faltasPorGrado.values().stream()
                .mapToInt(arr -> arr[0])
                .sum();

        for (Map.Entry<Integer, Integer[]> entry : faltasPorGrado.entrySet()) {
            int grado = entry.getKey();
            int cantidad = entry.getValue()[0];
            int estudiantes = entry.getValue()[1];
            String porcentaje = totalFaltas > 0
                    ? String.format("%.1f%%", (cantidad * 100.0) / totalFaltas)
                    : "0.0%";

            pdfBuilder.agregarFilaTabla(tabla, new String[]{
                    "Grado " + grado,
                    String.valueOf(cantidad),
                    String.valueOf(estudiantes),
                    porcentaje
            });
        }

        pdfBuilder.agregarFilaTabla(tabla, new String[]{
                "TOTAL",
                String.valueOf(totalFaltas),
                "—",
                "100.0%"
        }, true);

        pdfBuilder.agregarTabla(tabla);
    }

    /**
     * Para cada grado, agrega una tabla con desglose de faltas por caso.
     */
    private void agregarDesglosePorCasoEnCadaGrado(Map<Integer, Integer[]> faltasPorGrado,
                                                     String fechaDesde, String fechaHasta) {
        pdfBuilder.agregarSeccion("Análisis Detallado por Grado");
        pdfBuilder.agregarEspacio(4);

        for (Map.Entry<Integer, Integer[]> entry : faltasPorGrado.entrySet()) {
            int grado = entry.getKey();
            int totalGrado = entry.getValue()[0];

            Map<String, Integer> faltasPorCaso = faltaDAO.obtenerFaltasPorCasoEnGrado(grado, fechaDesde, fechaHasta);

            if (faltasPorCaso.isEmpty()) {
                continue;
            }

            // Subtítulo para cada grado
            pdfBuilder.agregarSubSeccion("Grado " + grado + " - Total: " + totalGrado + " faltas");

            float[] anchos = {4.5f, 2.5f, 2.5f};
            String[] encabezados = {"Caso", "Cantidad", "% del Grado"};
            Table tabla = pdfBuilder.crearTabla(anchos, encabezados, HEADER_COLOR);

            for (Map.Entry<String, Integer> casoEntry : faltasPorCaso.entrySet()) {
                int cantidadCaso = casoEntry.getValue();
                String porcentajeGrado = totalGrado > 0
                        ? String.format("%.1f%%", (cantidadCaso * 100.0) / totalGrado)
                        : "0.0%";

                pdfBuilder.agregarFilaTabla(tabla, new String[]{
                        casoEntry.getKey(),
                        String.valueOf(cantidadCaso),
                        porcentajeGrado
                });
            }

            pdfBuilder.agregarFilaTabla(tabla, new String[]{
                    "TOTAL " + grado,
                    String.valueOf(totalGrado),
                    "100.0%"
            }, true);

            pdfBuilder.agregarTabla(tabla);
            pdfBuilder.agregarEspacio(12);
        }
    }

    // ──────────────────────────────────────────────────────────────────────────
    //  UTILIDADES
    // ──────────────────────────────────────────────────────────────────────────

    private static final com.itextpdf.kernel.colors.DeviceRgb HEADER_COLOR =
            new com.itextpdf.kernel.colors.DeviceRgb(0, 82, 147);
}

