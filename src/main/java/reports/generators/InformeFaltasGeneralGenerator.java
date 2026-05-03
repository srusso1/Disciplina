package reports.generators;

import com.itextpdf.layout.element.Table;
import database.FaltaDAO;
import database.models.FaltaConsultaRow;
import reports.models.ReportConfig;
import utils.Fechas;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Genera el informe PDF "General de Faltas".
 *
 * Estructura del PDF:
 *  1. Encabezado institucional estándar
 *  2. Bloque de parámetros (rango de fechas aplicado)
 *  3. Resumen estadístico global
 *  4. Tabla: Distribución por tipo de falta
 *  5. Tabla: Faltas por caso
 *  6. Tabla: Faltas por lugar
 *  7. Tabla: Faltas por mes
 *  8. Tabla: Top 10 estudiantes con más faltas
 *  9. [Opcional] Tabla detallada de todos los registros
 * 10. Pie de página con fecha de generación
 */
public class InformeFaltasGeneralGenerator extends BaseReportGenerator {

    private final FaltaDAO faltaDAO;

    // Datos cargados una sola vez para reutilizar en todas las secciones
    private List<FaltaConsultaRow> todasLasFaltas;

    // ──────────────────────────────────────────────────────────────────────────
    //  CONSTRUCTOR
    // ──────────────────────────────────────────────────────────────────────────

    public InformeFaltasGeneralGenerator(ReportConfig config, String nombreArchivoSugerido) {
        super(config, nombreArchivoSugerido);
        this.faltaDAO = new FaltaDAO();
    }

    // ──────────────────────────────────────────────────────────────────────────
    //  PUNTO DE ENTRADA
    // ──────────────────────────────────────────────────────────────────────────

    @Override
    public void generar() {
        if (!puedeGenerar()) return;

        // Convertir fechas del config a String ISO para las queries
        String fechaDesde = config.getFechaInicio() != null
                ? config.getFechaInicio().toString()
                : null;
        String fechaHasta = config.getFechaFin() != null
                ? config.getFechaFin().toString()
                : null;

        // Cargar el listado completo una sola vez
        todasLasFaltas = faltaDAO.consultarFaltas(null, fechaDesde, fechaHasta, null, null, null);

        agregarEncabezadoEstandar("INFORME GENERAL DE FALTAS DISCIPLINARIAS");

        agregarBloqueParametros(fechaDesde, fechaHasta);
        agregarEspacio();

        agregarResumenEstadisticoGlobal(fechaDesde, fechaHasta);
        agregarEspacio();

        agregarTablaPorTipo();
        agregarEspacio();

        agregarTablaPorCaso(fechaDesde, fechaHasta);
        agregarEspacio();

        agregarTablaPorLugar(fechaDesde, fechaHasta);
        agregarEspacio();

        agregarTablaPorMes(fechaDesde, fechaHasta);
        agregarEspacio();

        agregarTablaTop10Estudiantes(fechaDesde, fechaHasta);
        agregarEspacio();

        if (config.isIncluirTablas() && !todasLasFaltas.isEmpty()) {
            agregarTablaDetallada();
        }

        finalizarReporte();
    }

    // ──────────────────────────────────────────────────────────────────────────
    //  SECCIONES DEL PDF
    // ──────────────────────────────────────────────────────────────────────────

    /** Parámetros del informe: rango de fechas aplicado. */
    private void agregarBloqueParametros(String fechaDesde, String fechaHasta) {
        pdfBuilder.agregarSeccion("Parámetros del Informe");

        String desde = config.getFechaInicio() != null
                ? config.getFechaInicio().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
                : "Sin límite";
        String hasta = config.getFechaFin() != null
                ? config.getFechaFin().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
                : "Sin límite";

        pdfBuilder.agregarLineaDetalle("Período", desde + "  →  " + hasta);
        pdfBuilder.agregarLineaDetalle("Total de registros encontrados", String.valueOf(todasLasFaltas.size()));
    }

    /** Resumen estadístico con los indicadores clave del período. */
    private void agregarResumenEstadisticoGlobal(String fechaDesde, String fechaHasta) {
        pdfBuilder.agregarSeccion("Resumen Estadístico General");

        int total = todasLasFaltas.size();

        long tipo1 = todasLasFaltas.stream().filter(f -> "Tipo 1".equals(f.getTipoFalta())).count();
        long tipo2 = todasLasFaltas.stream().filter(f -> "Tipo 2".equals(f.getTipoFalta())).count();
        long tipo3 = todasLasFaltas.stream().filter(f -> "Tipo 3".equals(f.getTipoFalta())).count();

        long estudiantesUnicos = todasLasFaltas.stream()
                .map(FaltaConsultaRow::getIdEstudiante).distinct().count();
        long docentesUnicos = todasLasFaltas.stream()
                .map(FaltaConsultaRow::getDocente)
                .filter(d -> d != null && !d.trim().isEmpty())
                .distinct().count();

        pdfBuilder.agregarLineaDetalle("Total de faltas registradas", String.valueOf(total));
        pdfBuilder.agregarLineaDetalle("Faltas Tipo 1", formatearConPorcentaje(tipo1, total));
        pdfBuilder.agregarLineaDetalle("Faltas Tipo 2", formatearConPorcentaje(tipo2, total));
        pdfBuilder.agregarLineaDetalle("Faltas Tipo 3", formatearConPorcentaje(tipo3, total));
        pdfBuilder.agregarLineaDetalle("Estudiantes con al menos una falta", String.valueOf(estudiantesUnicos));
        pdfBuilder.agregarLineaDetalle("Docentes involucrados", String.valueOf(docentesUnicos));

        // Caso más frecuente
        String casoMasComun = faltaDAO.obtenerCasoMasComun();
        pdfBuilder.agregarLineaDetalle("Caso más frecuente", casoMasComun);

        // Lugar con más incidentes
        String lugarMasFaltas = faltaDAO.obtenerLugarMasFaltas();
        pdfBuilder.agregarLineaDetalle("Lugar con más incidentes", lugarMasFaltas);
    }

    /** Tabla: distribución por tipo de falta (Tipo 1, Tipo 2, Tipo 3). */
    private void agregarTablaPorTipo() {
        pdfBuilder.agregarSeccion("Distribución por Tipo de Falta");

        int total = todasLasFaltas.size();

        // Agrupar por tipo usando el stream (ya tenemos los datos en memoria)
        Map<String, Long> porTipo = todasLasFaltas.stream()
                .collect(Collectors.groupingBy(FaltaConsultaRow::getTipoFalta, Collectors.counting()));

        float[] anchos       = {4f, 3f, 3f};
        String[] encabezados = {"Tipo de Falta", "Cantidad", "Porcentaje"};
        Table tabla = pdfBuilder.crearTabla(anchos, encabezados);

        // Mostrar en orden Tipo 1 → Tipo 2 → Tipo 3
        for (int tipo = 1; tipo <= 3; tipo++) {
            String clave   = "Tipo " + tipo;
            long cantidad  = porTipo.getOrDefault(clave, 0L);
            String porcentaje = total > 0
                    ? String.format("%.1f%%", (cantidad * 100.0) / total)
                    : "0.0%";
            pdfBuilder.agregarFilaTabla(tabla, new String[]{clave, String.valueOf(cantidad), porcentaje});
        }

        pdfBuilder.agregarTabla(tabla);
    }

    /** Tabla: faltas agrupadas por caso. */
    private void agregarTablaPorCaso(String fechaDesde, String fechaHasta) {
        pdfBuilder.agregarSeccion("Faltas por Caso / Tipo de Situación");

        Map<String, Integer> porCaso = faltaDAO.obtenerFaltasPorCaso(fechaDesde, fechaHasta);

        if (porCaso.isEmpty()) {
            pdfBuilder.agregarParrafoIndentado("No hay datos disponibles para el período seleccionado.");
            return;
        }

        int totalCasos = porCaso.values().stream().mapToInt(Integer::intValue).sum();

        float[] anchos       = {5f, 2.5f, 2.5f};
        String[] encabezados = {"Caso", "Cantidad", "Porcentaje"};
        Table tabla = pdfBuilder.crearTabla(anchos, encabezados);

        for (Map.Entry<String, Integer> entry : porCaso.entrySet()) {
            String porcentaje = totalCasos > 0
                    ? String.format("%.1f%%", (entry.getValue() * 100.0) / totalCasos)
                    : "0.0%";
            pdfBuilder.agregarFilaTabla(tabla, new String[]{
                    entry.getKey(),
                    String.valueOf(entry.getValue()),
                    porcentaje
            });
        }

        pdfBuilder.agregarTabla(tabla);
    }

    /** Tabla: faltas agrupadas por lugar. */
    private void agregarTablaPorLugar(String fechaDesde, String fechaHasta) {
        pdfBuilder.agregarSeccion("Faltas por Lugar");

        Map<String, Integer> porLugar = faltaDAO.obtenerFaltasPorLugar(fechaDesde, fechaHasta);

        if (porLugar.isEmpty()) {
            pdfBuilder.agregarParrafoIndentado("No hay datos disponibles para el período seleccionado.");
            return;
        }

        int totalLugares = porLugar.values().stream().mapToInt(Integer::intValue).sum();

        float[] anchos       = {5f, 2.5f, 2.5f};
        String[] encabezados = {"Lugar", "Cantidad", "Porcentaje"};
        Table tabla = pdfBuilder.crearTabla(anchos, encabezados);

        for (Map.Entry<String, Integer> entry : porLugar.entrySet()) {
            String porcentaje = totalLugares > 0
                    ? String.format("%.1f%%", (entry.getValue() * 100.0) / totalLugares)
                    : "0.0%";
            pdfBuilder.agregarFilaTabla(tabla, new String[]{
                    entry.getKey(),
                    String.valueOf(entry.getValue()),
                    porcentaje
            });
        }

        pdfBuilder.agregarTabla(tabla);
    }

    /** Tabla: faltas agrupadas por mes. */
    private void agregarTablaPorMes(String fechaDesde, String fechaHasta) {
        pdfBuilder.agregarSeccion("Evolución Mensual de Faltas");

        Map<String, Integer> porMes = faltaDAO.obtenerFaltasPorMes(fechaDesde, fechaHasta);

        if (porMes.isEmpty()) {
            pdfBuilder.agregarParrafoIndentado("No hay datos disponibles para el período seleccionado.");
            return;
        }

        float[] anchos       = {4f, 3f};
        String[] encabezados = {"Mes", "Cantidad"};
        Table tabla = pdfBuilder.crearTabla(anchos, encabezados);

        for (Map.Entry<String, Integer> entry : porMes.entrySet()) {
            pdfBuilder.agregarFilaTabla(tabla, new String[]{
                    formatearMes(entry.getKey()),
                    String.valueOf(entry.getValue())
            });
        }

        pdfBuilder.agregarTabla(tabla);
    }

    /** Tabla: top 10 estudiantes con más faltas en el período. */
    private void agregarTablaTop10Estudiantes(String fechaDesde, String fechaHasta) {
        pdfBuilder.agregarSeccion("Top 10 Estudiantes con Más Faltas");

        Map<String, Integer> top10 = faltaDAO.obtenerTop10Estudiantes(fechaDesde, fechaHasta);

        if (top10.isEmpty()) {
            pdfBuilder.agregarParrafoIndentado("No hay datos disponibles para el período seleccionado.");
            return;
        }

        float[] anchos       = {1.5f, 5f, 2.5f};
        String[] encabezados = {"#", "Estudiante", "Faltas"};
        Table tabla = pdfBuilder.crearTabla(anchos, encabezados);

        int posicion = 1;
        for (Map.Entry<String, Integer> entry : top10.entrySet()) {
            pdfBuilder.agregarFilaTabla(tabla, new String[]{
                    String.valueOf(posicion++),
                    entry.getKey(),
                    String.valueOf(entry.getValue())
            });
        }

        pdfBuilder.agregarTabla(tabla);
    }

    /**
     * Tabla detallada con todos los registros del período.
     * Solo se agrega si config.isIncluirTablas() == true.
     */
    private void agregarTablaDetallada() {
        pdfBuilder.agregarSeccion("Detalle Completo de Faltas");

        if (todasLasFaltas.isEmpty()) {
            pdfBuilder.agregarParrafoIndentado("No hay faltas registradas para el período seleccionado.");
            return;
        }

        float[] anchos       = {2.5f, 4f, 1.5f, 2f, 3.5f, 2.5f, 3f};
        String[] encabezados = {"Fecha", "Estudiante", "Grado", "Tipo", "Caso", "Lugar", "Docente"};
        Table tabla = pdfBuilder.crearTabla(anchos, encabezados);

        for (FaltaConsultaRow falta : todasLasFaltas) {
            pdfBuilder.agregarFilaTabla(tabla, new String[]{
                    falta.getFecha(),
                    falta.getEstudiante(),
                    falta.getGrado() + "°",
                    "Tipo " + falta.getTipoFalta(),
                    falta.getCaso(),
                    falta.getLugar(),
                    falta.getDocente()
            });
        }

        pdfBuilder.agregarTabla(tabla);
    }

    // ──────────────────────────────────────────────────────────────────────────
    //  UTILIDADES
    // ──────────────────────────────────────────────────────────────────────────

    private void agregarEspacio() {
        pdfBuilder.agregarEspacio(10);
    }

    /**
     * Formatea un número con su porcentaje: "42 (35.0%)"
     */
    private String formatearConPorcentaje(long cantidad, int total) {
        if (total == 0) return "0 (0.0%)";
        return cantidad + " (" + String.format("%.1f%%", (cantidad * 100.0) / total) + ")";
    }

    /**
     * Convierte "2026-03" → "Marzo 2026".
     */
    private String formatearMes(String mesISO) {
        if (mesISO == null || mesISO.length() < 7) return mesISO;
        try {
            String[] partes = mesISO.split("-");
            int anio = Integer.parseInt(partes[0]);
            int mes  = Integer.parseInt(partes[1]);
            String[] nombres = {"", "Enero", "Febrero", "Marzo", "Abril", "Mayo", "Junio",
                    "Julio", "Agosto", "Septiembre", "Octubre", "Noviembre", "Diciembre"};
            return (mes >= 1 && mes <= 12 ? nombres[mes] : mesISO) + " " + anio;
        } catch (Exception e) {
            return mesISO;
        }
    }
}