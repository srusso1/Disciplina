package reports.generators;

import com.itextpdf.layout.element.Table;
import database.models.FaltaConsultaRow;
import models.Estudiante;
import reports.models.ReportConfig;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.LinkedHashMap;
import java.util.stream.Collectors;

/**
 * Genera el informe PDF de "Faltas por Estudiante".
 *
 * Estructura del PDF:
 *  1. Encabezado institucional estándar
 *  2. Datos del estudiante (si se seleccionó uno)
 *  3. Bloque de parámetros del informe (filtros aplicados)
 *  4. Resumen estadístico (total por Tipo 1 / Tipo 2 / Tipo 3)
 *  5. [Opcional] Tabla detallada de faltas
 *  6. Distribución por caso (cuando es informe individual)
 *  7. Pie de página con fecha de generación
 *
 * NOTA: getTipoFalta() en FaltaConsultaRow devuelve "Tipo 1", "Tipo 2" o "Tipo 3"
 *       (el DAO ya prefija "Tipo " al construir la fila). Las comparaciones deben
 *       usar ese formato exacto.
 */
public class InformeFaltasEstudianteGenerator extends BaseReportGenerator {

    private final List<FaltaConsultaRow> faltas;
    private final Estudiante             estudianteSeleccionado;
    private final String                 nombreCasoFiltro;
    private final Integer                tipoFaltaFiltro;   // número 1, 2 ó 3 (o null = todos)

    // ──────────────────────────────────────────────────────────────────────────
    //  CONSTRUCTOR
    // ──────────────────────────────────────────────────────────────────────────

    public InformeFaltasEstudianteGenerator(ReportConfig config,
                                            String nombreArchivoSugerido,
                                            List<FaltaConsultaRow> faltas,
                                            Estudiante estudianteSeleccionado,
                                            String nombreCasoFiltro,
                                            Integer tipoFaltaFiltro) {
        super(config, nombreArchivoSugerido);
        this.faltas                 = faltas;
        this.estudianteSeleccionado = estudianteSeleccionado;
        this.nombreCasoFiltro       = nombreCasoFiltro;
        this.tipoFaltaFiltro        = tipoFaltaFiltro;
    }

    // ──────────────────────────────────────────────────────────────────────────
    //  PUNTO DE ENTRADA
    // ──────────────────────────────────────────────────────────────────────────

    @Override
    public void generar() {
        if (!puedeGenerar()) return;

        agregarEncabezadoEstandar("INFORME DE FALTAS DISCIPLINARIAS");

        if (estudianteSeleccionado != null) {
            agregarDatosEstudiante();
            agregarEspacio();
        }

        agregarBloqueParametros();
        agregarEspacio();
        agregarResumenEstadistico();
        agregarEspacio();

        if (config.isIncluirTablas() && !faltas.isEmpty()) {
            agregarTablaDetallada();
            agregarEspacio();
        }

        if (estudianteSeleccionado != null && !faltas.isEmpty()) {
            agregarResumenPorCaso();
        }

        finalizarReporte();
    }

    // ──────────────────────────────────────────────────────────────────────────
    //  SECCIONES DEL PDF
    // ──────────────────────────────────────────────────────────────────────────

    /** Bloque con datos personales del estudiante seleccionado. */
    private void agregarDatosEstudiante() {
        pdfBuilder.agregarSeccion("Datos del Estudiante");
        pdfBuilder.agregarLineaDetalle("Nombre",         nombreCompleto(estudianteSeleccionado));
        pdfBuilder.agregarLineaDetalle("Identificación", String.valueOf(estudianteSeleccionado.getIdentificacion()));
        pdfBuilder.agregarLineaDetalle("Grado",          estudianteSeleccionado.getGrado() + "°");
        pdfBuilder.agregarLineaDetalle("Género",         safeStr(estudianteSeleccionado.getGenero()));
    }

    /** Bloque con los filtros aplicados al generar el informe. */
    private void agregarBloqueParametros() {
        pdfBuilder.agregarSeccion("Parámetros del Informe");

        String nombreEstudiante = estudianteSeleccionado != null
                ? nombreCompleto(estudianteSeleccionado)
                : "Todos los estudiantes";
        pdfBuilder.agregarLineaDetalle("Estudiante", nombreEstudiante);

        String desde = config.getFechaInicio() != null
                ? config.getFechaInicio().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
                : "Sin límite";
        String hasta = config.getFechaFin() != null
                ? config.getFechaFin().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
                : "Sin límite";
        pdfBuilder.agregarLineaDetalle("Período", desde + "  →  " + hasta);

        // Tipo de falta: "Tipo N" o "Todos los tipos"
        String tipoFaltaTexto = tipoFaltaFiltro != null ? "Tipo " + tipoFaltaFiltro : "Todos los tipos";
        pdfBuilder.agregarLineaDetalle("Tipo de Falta", tipoFaltaTexto);

        String casoTexto = nombreCasoFiltro != null ? nombreCasoFiltro : "Todos los casos";
        pdfBuilder.agregarLineaDetalle("Caso", casoTexto);

    }

    /**
     * Resumen estadístico.
     * IMPORTANTE: getTipoFalta() devuelve "Tipo 1", "Tipo 2", "Tipo 3" — comparar con ese formato.
     */
    private void agregarResumenEstadistico() {
        pdfBuilder.agregarSeccion("Resumen Estadístico");

        long tipo1 = faltas.stream().filter(f -> "Tipo 1".equals(f.getTipoFalta())).count();
        long tipo2 = faltas.stream().filter(f -> "Tipo 2".equals(f.getTipoFalta())).count();
        long tipo3 = faltas.stream().filter(f -> "Tipo 3".equals(f.getTipoFalta())).count();
        long estudiantesUnicos = faltas.stream()
                .map(FaltaConsultaRow::getIdEstudiante).distinct().count();

        pdfBuilder.agregarLineaDetalle("Total de faltas registradas", String.valueOf(faltas.size()));
        pdfBuilder.agregarLineaDetalle("Faltas Tipo 1", String.valueOf(tipo1));
        pdfBuilder.agregarLineaDetalle("Faltas Tipo 2", String.valueOf(tipo2));
        pdfBuilder.agregarLineaDetalle("Faltas Tipo 3", String.valueOf(tipo3));

        // Caso más frecuente
        faltas.stream()
                .collect(Collectors.groupingBy(FaltaConsultaRow::getCaso, Collectors.counting()))
                .entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .ifPresent(e ->
                        pdfBuilder.agregarLineaDetalle("Caso más frecuente",
                                e.getKey() + " (" + e.getValue() + " faltas)")
                );
    }

    /** Tabla detallada con todos los registros del período. */
    private void agregarTablaDetallada() {
        pdfBuilder.agregarSeccion("Detalle de Faltas");

        // Tabla compacta para mejor legibilidad; usar encabezado con color
        float[] anchos      = {2.5f, 3f, 1.5f, 2f, 3f, 2f};
        String[] encabezados = {"Fecha", "Estudiante", "Grado", "Tipo", "Caso", "Lugar"};
        com.itextpdf.kernel.colors.Color headerColor = new com.itextpdf.kernel.colors.DeviceRgb(0, 82, 147);
        Table tabla = pdfBuilder.crearTabla(anchos, encabezados, headerColor);

        for (FaltaConsultaRow falta : faltas) {
            // Nota: los campos largos (descargo/acción) se agregan como anexo numerado
            pdfBuilder.agregarFilaTabla(tabla, new String[]{
                    falta.getFecha(),
                    falta.getEstudiante(),
                    falta.getGrado() + "°",
                    falta.getTipoFalta(),
                    falta.getCaso(),
                    falta.getLugar()
            });
        }

        pdfBuilder.agregarTabla(tabla);

        // Anexos: Descragos y acciones restaurativas (si existen)
        int notaIndex = 1;
        for (FaltaConsultaRow falta : faltas) {
            String desc = safeStr(falta.getDescargo());
            String accion = safeStr(falta.getAccionRestaurativa());
            if (!desc.isEmpty() || !accion.isEmpty()) {
                pdfBuilder.agregarSeccion("Anexo " + notaIndex + " — Detalle de la falta: " + falta.getFecha());
                if (!desc.isEmpty()) pdfBuilder.agregarLineaDetalle("Descargo", desc);
                if (!accion.isEmpty()) pdfBuilder.agregarLineaDetalle("Acción restaurativa", accion);
                notaIndex++;
            }
        }
    }

    /** Distribución de faltas por caso (para informes individuales). */
    private void agregarResumenPorCaso() {
        pdfBuilder.agregarSeccion("Distribución por Caso — " + nombreCompleto(estudianteSeleccionado));

        Map<String, Long> porCaso = faltas.stream()
                .collect(Collectors.groupingBy(FaltaConsultaRow::getCaso,
                        LinkedHashMap::new,
                        Collectors.counting()));

        float[] anchos      = {6f, 2f};
        String[] encabezados = {"Caso", "Cantidad"};
        Table tabla = pdfBuilder.crearTabla(anchos, encabezados);

        for (Map.Entry<String, Long> entry : porCaso.entrySet()) {
            pdfBuilder.agregarFilaTabla(tabla, new String[]{
                    entry.getKey(),
                    String.valueOf(entry.getValue())
            });
        }

        pdfBuilder.agregarTabla(tabla);
    }

    // ──────────────────────────────────────────────────────────────────────────
    //  UTILIDADES
    // ──────────────────────────────────────────────────────────────────────────

    private void agregarEspacio() { pdfBuilder.agregarEspacio(10); }

    private String nombreCompleto(Estudiante e) {
        if (e == null) return "";
        return (e.getNombre1()           + " " +
                safeStr(e.getNombre2())  + " " +
                e.getApellido1()         + " " +
                safeStr(e.getApellido2())).replaceAll("\\s+", " ").trim();
    }

    private String safeStr(String s) { return s != null ? s : ""; }
}