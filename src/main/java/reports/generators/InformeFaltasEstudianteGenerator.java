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
 *  2. Bloque de parámetros del informe (filtros aplicados)
 *  3. Resumen estadístico (total, graves, leves, estudiantes)
 *  4. [Opcional] Tabla detallada de faltas
 *  5. Pie de página con fecha de generación
 */
public class InformeFaltasEstudianteGenerator extends BaseReportGenerator {

    private final List<FaltaConsultaRow> faltas;
    private final Estudiante             estudianteSeleccionado;
    private final String                 nombreCasoFiltro;
    private final Integer                tipoFaltaFiltro;

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
        this.faltas                  = faltas;
        this.estudianteSeleccionado  = estudianteSeleccionado;
        this.nombreCasoFiltro        = nombreCasoFiltro;
        this.tipoFaltaFiltro         = tipoFaltaFiltro;
    }

    // ──────────────────────────────────────────────────────────────────────────
    //  PUNTO DE ENTRADA
    // ──────────────────────────────────────────────────────────────────────────

    @Override
    public void generar() {
        if (!puedeGenerar()) return;

        agregarEncabezadoEstandar("INFORME DE FALTAS DISCIPLINARIAS");

        agregarBloqueParametros();
        agregarEspacio();
        agregarResumenEstadistico();
        agregarEspacio();

        if (config.isIncluirTablas() && !faltas.isEmpty()) {
            agregarTablaDetallada();
            agregarEspacio();
        }

        // Si hay un estudiante individual, agregar resumen agrupado por caso
        if (estudianteSeleccionado != null && !faltas.isEmpty()) {
            agregarResumenPorCaso();
        }

        finalizarReporte();
    }

    // ──────────────────────────────────────────────────────────────────────────
    //  SECCIONES DEL PDF
    // ──────────────────────────────────────────────────────────────────────────

    /** Bloque con los filtros que se aplicaron al generar el informe. */
    private void agregarBloqueParametros() {
        pdfBuilder.agregarSeccion("Parámetros del Informe");

        // Estudiante
        String nombreEstudiante = estudianteSeleccionado != null
                ? nombreCompleto(estudianteSeleccionado)
                : "Todos los estudiantes";
        pdfBuilder.agregarLineaDetalle("Estudiante", nombreEstudiante);

        // Período
        String desde = config.getFechaInicio() != null
                ? config.getFechaInicio().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
                : "Sin límite";
        String hasta = config.getFechaFin() != null
                ? config.getFechaFin().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
                : "Sin límite";
        pdfBuilder.agregarLineaDetalle("Período", desde + "  →  " + hasta);

        // Tipo de falta
        String tipoFaltaTexto = tipoFaltaFiltro != null
                ? resolverTipoFalta(tipoFaltaFiltro)
                : "Todos los tipos";
        pdfBuilder.agregarLineaDetalle("Tipo de Falta", tipoFaltaTexto);

        // Caso
        String casoTexto = nombreCasoFiltro != null ? nombreCasoFiltro : "Todos los casos";
        pdfBuilder.agregarLineaDetalle("Caso", casoTexto);

        pdfBuilder.agregarLineaDetalle("Total de registros", String.valueOf(faltas.size()));
    }

    /** Bloque con contadores globales. */
    private void agregarResumenEstadistico() {
        pdfBuilder.agregarSeccion("Resumen Estadístico");

        long graves    = faltas.stream().filter(f -> esGrave(f.getTipoFalta())).count();
        long leves     = faltas.stream().filter(f -> "1".equals(f.getTipoFalta())).count();
        long gravisimas = faltas.stream().filter(f -> "3".equals(f.getTipoFalta())).count();
        long estudiantesUnicos = faltas.stream()
                .map(FaltaConsultaRow::getIdEstudiante).distinct().count();

        pdfBuilder.agregarLineaDetalle("Total de faltas registradas", String.valueOf(faltas.size()));
        pdfBuilder.agregarLineaDetalle("Faltas leves (Tipo 1)",       String.valueOf(leves));
        pdfBuilder.agregarLineaDetalle("Faltas graves (Tipo 2)",      String.valueOf(graves));
        pdfBuilder.agregarLineaDetalle("Faltas gravísimas (Tipo 3)",  String.valueOf(gravisimas));
        pdfBuilder.agregarLineaDetalle("Estudiantes con faltas",      String.valueOf(estudiantesUnicos));

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

    /** Tabla fila a fila con todos los registros. */
    private void agregarTablaDetallada() {
        pdfBuilder.agregarSeccion("Detalle de Faltas");

        float[] anchos = {3f, 4f, 1.5f, 2f, 3.5f, 2.5f, 3f};
        String[] encabezados = {"Fecha", "Estudiante", "Grado", "Tipo", "Caso", "Lugar", "Docente"};

        Table tabla = pdfBuilder.crearTabla(anchos, encabezados);

        for (FaltaConsultaRow falta : faltas) {
            pdfBuilder.agregarFilaTabla(tabla, new String[]{
                    falta.getFecha(),
                    falta.getEstudiante(),
                    String.valueOf(falta.getGrado()),
                    resolverTipoFalta(falta.getTipoFalta()),
                    falta.getCaso(),
                    falta.getLugar(),
                    falta.getDocente()
            });
        }

        pdfBuilder.agregarTabla(tabla);
    }

    /** Agrupación de faltas por tipo de caso (para informes individuales). */
    private void agregarResumenPorCaso() {
        pdfBuilder.agregarSeccion("Distribución por Caso — " + nombreCompleto(estudianteSeleccionado));

        Map<String, Long> porCaso = faltas.stream()
                .collect(Collectors.groupingBy(FaltaConsultaRow::getCaso,
                        LinkedHashMap::new,
                        Collectors.counting()));

        float[] anchos = {6f, 2f};
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

    private void agregarEspacio() {
        pdfBuilder.agregarEspacio(10);
    }

    private String nombreCompleto(Estudiante e) {
        if (e == null) return "";
        return (e.getNombre1()   + " " +
                safeStr(e.getNombre2())   + " " +
                e.getApellido1() + " " +
                safeStr(e.getApellido2())).replaceAll("\\s+", " ").trim();
    }

    private String safeStr(String s) {
        return s != null ? s : "";
    }

    private boolean esGrave(String tipo) {
        return "2".equals(tipo);
    }

    /** Convierte el String tipo_falta (viene de la DB) a texto legible. */
    private String resolverTipoFalta(String tipo) {
        if (tipo == null) return "";
        switch (tipo.trim()) {
            case "1": return "Leve";
            case "2": return "Grave";
            case "3": return "Gravísima";
            default:  return tipo;
        }
    }

    /** Convierte el Integer tipo_falta al mismo texto. */
    private String resolverTipoFalta(Integer tipo) {
        if (tipo == null) return "Todos";
        switch (tipo) {
            case 1: return "Leve (Tipo 1)";
            case 2: return "Grave (Tipo 2)";
            case 3: return "Gravísima (Tipo 3)";
            default: return String.valueOf(tipo);
        }
    }
}
