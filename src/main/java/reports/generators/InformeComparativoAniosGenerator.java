package reports.generators;

import com.itextpdf.layout.element.Table;
import database.FaltaDAO;
import reports.models.ReportConfig;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Genera un informe PDF comparando la evolución mensual entre dos años.
 */
public class InformeComparativoAniosGenerator extends BaseReportGenerator {

    private static final List<String> MESES = Arrays.asList(
            "Enero", "Febrero", "Marzo", "Abril", "Mayo", "Junio",
            "Julio", "Agosto", "Septiembre", "Octubre", "Noviembre", "Diciembre"
    );

    private final FaltaDAO faltaDAO = new FaltaDAO();
    private final String nombreCaso;

    public InformeComparativoAniosGenerator(ReportConfig config, String nombreArchivoSugerido, String nombreCaso) {
        super(config, nombreArchivoSugerido);
        this.nombreCaso = nombreCaso;
    }

    @Override
    public void generar() {
        if (!puedeGenerar()) return;

        Integer anio1 = config.getAnioComparativo1();
        Integer anio2 = config.getAnioComparativo2();
        if (anio1 == null || anio2 == null) {
            pdfBuilder.agregarParrafo("Debe seleccionar dos años válidos para generar el comparativo.");
            finalizarReporte();
            return;
        }

        Map<Integer, Map<Integer, Integer>> conteoPorMes = faltaDAO.obtenerConteoPorMesYAnio(config.getIdCaso());

        agregarEncabezadoEstandar("INFORME COMPARATIVO ENTRE AÑOS");
        agregarBloqueParametros(anio1, anio2, conteoPorMes);
        pdfBuilder.agregarEspacio(8);
        agregarResumenEstadistico(conteoPorMes, anio1, anio2);
        pdfBuilder.agregarEspacio(8);

        if (config.isIncluirTablas()) {
            agregarTablaComparativa(conteoPorMes, anio1, anio2);
            pdfBuilder.agregarEspacio(8);
        }

        finalizarReporte();
    }

    private void agregarBloqueParametros(Integer anio1, Integer anio2, Map<Integer, Map<Integer, Integer>> conteoPorMes) {
        pdfBuilder.agregarSeccion("Parámetros del Informe");
        pdfBuilder.agregarLineaDetalle("Caso", nombreCaso != null && !nombreCaso.trim().isEmpty() ? nombreCaso : "Todos los casos");
        pdfBuilder.agregarLineaDetalle("Años comparados", anio1 + " vs " + anio2);
        pdfBuilder.agregarLineaDetalle("Meses analizados", String.valueOf(conteoPorMes.size()));
        pdfBuilder.agregarLineaDetalle("Tipo de informe", "Comparativo entre años");
    }

    private void agregarResumenEstadistico(Map<Integer, Map<Integer, Integer>> conteoPorMes, Integer anio1, Integer anio2) {
        pdfBuilder.agregarSeccion("Resumen Comparativo");

        int totalAnio1 = sumarTotalPorAnio(conteoPorMes, anio1);
        int totalAnio2 = sumarTotalPorAnio(conteoPorMes, anio2);
        int diferencia = totalAnio2 - totalAnio1;

        pdfBuilder.agregarLineaDetalle(String.valueOf(anio1), String.valueOf(totalAnio1));
        pdfBuilder.agregarLineaDetalle(String.valueOf(anio2), String.valueOf(totalAnio2));
        pdfBuilder.agregarLineaDetalle("Diferencia total", formatearDiferencia(diferencia));

        MesMayorDiferencia mesMayor = obtenerMesMayorDiferencia(conteoPorMes, anio1, anio2);
        if (mesMayor != null) {
            pdfBuilder.agregarLineaDetalle(
                    "Mes con mayor variación",
                    mesMayor.mes + " (" + formatearDiferencia(mesMayor.diferencia) + ")"
            );
        }
    }

    private void agregarTablaComparativa(Map<Integer, Map<Integer, Integer>> conteoPorMes, Integer anio1, Integer anio2) {
        pdfBuilder.agregarSeccion("Comparación Mensual");

        float[] anchos = {4f, 2f, 2f, 2f, 2f};
        String[] encabezados = {"Mes", String.valueOf(anio1), String.valueOf(anio2), "Total", "Variación"};
        Table tabla = pdfBuilder.crearTabla(anchos, encabezados);

        int totalAnio1 = 0;
        int totalAnio2 = 0;

        for (int mes = 1; mes <= 12; mes++) {
            int valorAnio1 = obtenerValorMes(conteoPorMes, mes, anio1);
            int valorAnio2 = obtenerValorMes(conteoPorMes, mes, anio2);
            int totalMes = valorAnio1 + valorAnio2;
            int variacion = valorAnio2 - valorAnio1;

            totalAnio1 += valorAnio1;
            totalAnio2 += valorAnio2;

            pdfBuilder.agregarFilaTabla(tabla, new String[]{
                    MESES.get(mes - 1),
                    String.valueOf(valorAnio1),
                    String.valueOf(valorAnio2),
                    String.valueOf(totalMes),
                    formatearDiferencia(variacion)
            });
        }

        pdfBuilder.agregarFilaTabla(tabla, new String[]{
                "TOTAL",
                String.valueOf(totalAnio1),
                String.valueOf(totalAnio2),
                String.valueOf(totalAnio1 + totalAnio2),
                formatearDiferencia(totalAnio2 - totalAnio1)
        });

        pdfBuilder.agregarTabla(tabla);
    }

    private int obtenerValorMes(Map<Integer, Map<Integer, Integer>> conteoPorMes, int mes, Integer anio) {
        if (anio == null) {
            return 0;
        }
        Map<Integer, Integer> conteoAnual = conteoPorMes.get(mes);
        if (conteoAnual == null) {
            return 0;
        }
        Integer valor = conteoAnual.get(anio);
        return valor != null ? valor : 0;
    }

    private int sumarTotalPorAnio(Map<Integer, Map<Integer, Integer>> conteoPorMes, Integer anio) {
        int total = 0;
        for (int mes = 1; mes <= 12; mes++) {
            total += obtenerValorMes(conteoPorMes, mes, anio);
        }
        return total;
    }

    private MesMayorDiferencia obtenerMesMayorDiferencia(Map<Integer, Map<Integer, Integer>> conteoPorMes, Integer anio1, Integer anio2) {
        MesMayorDiferencia resultado = null;

        for (int mes = 1; mes <= 12; mes++) {
            int valorAnio1 = obtenerValorMes(conteoPorMes, mes, anio1);
            int valorAnio2 = obtenerValorMes(conteoPorMes, mes, anio2);
            int diferencia = valorAnio2 - valorAnio1;
            int diferenciaAbsoluta = Math.abs(diferencia);

            if (resultado == null || diferenciaAbsoluta > Math.abs(resultado.diferencia)) {
                resultado = new MesMayorDiferencia(MESES.get(mes - 1), diferencia);
            }
        }

        return resultado;
    }

    private String formatearDiferencia(int valor) {
        if (valor > 0) {
            return "+" + valor;
        }
        return String.valueOf(valor);
    }

    private static class MesMayorDiferencia {
        private final String mes;
        private final int diferencia;

        private MesMayorDiferencia(String mes, int diferencia) {
            this.mes = mes;
            this.diferencia = diferencia;
        }
    }
}

