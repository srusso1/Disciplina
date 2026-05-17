package reports.generators;

import com.itextpdf.layout.element.Table;
import database.FaltaDAO;
import reports.models.ReportConfig;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Genera un informe PDF con la evolución mensual de las faltas registradas.
 */
public class InformeEvolucionMensualGenerator extends BaseReportGenerator {

    private static final List<String> MESES = Arrays.asList(
            "Enero", "Febrero", "Marzo", "Abril", "Mayo", "Junio",
            "Julio", "Agosto", "Septiembre", "Octubre", "Noviembre", "Diciembre"
    );

    private final FaltaDAO faltaDAO = new FaltaDAO();

    public InformeEvolucionMensualGenerator(ReportConfig config, String nombreArchivoSugerido) {
        super(config, nombreArchivoSugerido);
    }

    @Override
    public void generar() {
        if (!puedeGenerar()) return;

        String fechaDesde = config.getFechaInicio() != null ? config.getFechaInicio().toString() : null;
        String fechaHasta = config.getFechaFin() != null ? config.getFechaFin().toString() : null;
        Map<String, Integer> faltasPorMes = faltaDAO.obtenerFaltasPorMes(fechaDesde, fechaHasta);

        agregarEncabezadoEstandar("INFORME DE EVOLUCIÓN MENSUAL DE FALTAS");
        agregarBloqueParametros(fechaDesde, fechaHasta, faltasPorMes);
        pdfBuilder.agregarEspacio(8);
        agregarResumenEstadistico(faltasPorMes);
        pdfBuilder.agregarEspacio(8);

        if (config.isIncluirTablas() && !faltasPorMes.isEmpty()) {
            agregarTablaMensual(faltasPorMes);
            pdfBuilder.agregarEspacio(8);
        }

        finalizarReporte();
    }

    private void agregarBloqueParametros(String fechaDesde, String fechaHasta, Map<String, Integer> faltasPorMes) {
        pdfBuilder.agregarSeccion("Parámetros del Informe");
        pdfBuilder.agregarLineaDetalle("Rango de fechas", formatearRangoFechas(fechaDesde, fechaHasta));
        pdfBuilder.agregarLineaDetalle("Meses con registro", String.valueOf(faltasPorMes.size()));
        pdfBuilder.agregarLineaDetalle("Tipo de informe", "Evolución mensual");
    }

    private void agregarResumenEstadistico(Map<String, Integer> faltasPorMes) {
        pdfBuilder.agregarSeccion("Resumen Mensual");

        int total = faltasPorMes.values().stream().mapToInt(Integer::intValue).sum();
        int promedio = faltasPorMes.isEmpty() ? 0 : Math.round((float) total / faltasPorMes.size());

        pdfBuilder.agregarLineaDetalle("Total de faltas en el período", String.valueOf(total));
        pdfBuilder.agregarLineaDetalle("Promedio mensual", String.valueOf(promedio));

        faltasPorMes.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .ifPresent(entry -> pdfBuilder.agregarLineaDetalle(
                        "Mes con más faltas",
                        formatearMes(entry.getKey()) + " (" + entry.getValue() + ")"
                ));

        faltasPorMes.entrySet().stream()
                .min(Map.Entry.comparingByValue())
                .ifPresent(entry -> pdfBuilder.agregarLineaDetalle(
                        "Mes con menos faltas",
                        formatearMes(entry.getKey()) + " (" + entry.getValue() + ")"
                ));
    }

    private void agregarTablaMensual(Map<String, Integer> faltasPorMes) {
        pdfBuilder.agregarSeccion("Distribución Mensual");

        float[] anchos = {4f, 2f, 2f};
        String[] encabezados = {"Mes", "Faltas", "% del total"};
        com.itextpdf.kernel.colors.Color headerColor = new com.itextpdf.kernel.colors.DeviceRgb(0, 102, 153);
        Table tabla = pdfBuilder.crearTabla(anchos, encabezados, headerColor);

        int total = faltasPorMes.values().stream().mapToInt(Integer::intValue).sum();
        for (Map.Entry<String, Integer> entry : faltasPorMes.entrySet()) {
            int cantidad = entry.getValue() != null ? entry.getValue() : 0;
            double porcentaje = total > 0 ? (cantidad * 100.0) / total : 0.0;
            pdfBuilder.agregarFilaTabla(tabla, new String[]{
                    formatearMes(entry.getKey()),
                    String.valueOf(cantidad),
                    String.format(Locale.US, "%.1f%%", porcentaje)
            }, false);
        }

        // Agregar fila TOTAL resaltada
        pdfBuilder.agregarFilaTabla(tabla, new String[]{"TOTAL", String.valueOf(total), "100.0%"}, true);
        pdfBuilder.agregarTabla(tabla);
    }

    private String formatearRangoFechas(String fechaDesde, String fechaHasta) {
        String desde = fechaDesde != null ? fechaDesde.replace('-', '/') : "Sin límite";
        String hasta = fechaHasta != null ? fechaHasta.replace('-', '/') : "Sin límite";
        return desde + "  -  " + hasta;
    }

    private String formatearMes(String mesISO) {
        if (mesISO == null || mesISO.trim().isEmpty()) {
            return "";
        }

        try {
            String[] partes = mesISO.split("-");
            if (partes.length >= 2) {
                int anio = Integer.parseInt(partes[0]);
                int mes = Integer.parseInt(partes[1]);
                if (mes >= 1 && mes <= 12) {
                    return MESES.get(mes - 1) + " " + anio;
                }
            }
        } catch (Exception ignore) {
        }

        return mesISO;
    }
}

