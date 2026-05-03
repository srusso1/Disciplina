package reports.generators;

import javafx.stage.FileChooser;
import javafx.stage.Window;
import reports.models.ReportConfig;
import reports.utils.PDFBuilder;

import java.awt.Desktop;
import java.io.File;

/**
 * Clase base para generadores de reportes PDF en Disciplina.
 * Replica el flujo de BookTech: selecciona ruta, compone encabezado/pie y construye el documento.
 */
public abstract class BaseReportGenerator {

    protected final ReportConfig config;
    protected String rutaArchivo;
    protected PDFBuilder pdfBuilder;

    public BaseReportGenerator(ReportConfig config, String nombreArchivoSugerido) {
        this.config = config;
        this.rutaArchivo = seleccionarRutaArchivo(nombreArchivoSugerido);
        if (this.rutaArchivo != null) {
            this.pdfBuilder = new PDFBuilder(this.rutaArchivo);
        }
    }

    private String seleccionarRutaArchivo(String nombreArchivo) {
        try {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Guardar reporte PDF");
            if (nombreArchivo != null && !nombreArchivo.trim().isEmpty()) {
                fileChooser.setInitialFileName(nombreArchivo);
            }
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Archivos PDF", "*.pdf"));

            File carpeta = new File(ReportConfig.RUTA_REPORTES);
            if (!carpeta.exists()) carpeta.mkdirs();
            if (carpeta.exists() && carpeta.isDirectory()) {
                fileChooser.setInitialDirectory(carpeta);
            }

            // Para máxima compatibilidad con Java 8 evitamos Window.getWindows()
            // El owner puede ser null; JavaFX abrirá el diálogo de forma independiente.
            File seleccionado = fileChooser.showSaveDialog(null);
            if (seleccionado == null) return null;
            String ruta = seleccionado.getAbsolutePath();
            return ruta.toLowerCase().endsWith(".pdf") ? ruta : (ruta + ".pdf");
        } catch (Exception e) {
            return null; // en headless/testing puede fallar; los generadores deben respetar null
        }
    }

    protected boolean puedeGenerar() {
        return pdfBuilder != null && rutaArchivo != null;
    }

    public abstract void generar();

    protected void agregarEncabezadoEstandar(String titulo) {
        pdfBuilder
                .agregarEncabezadoInstitucional(
                        ReportConfig.INSTITUCION,
                        ReportConfig.ESCUELA,
                        ReportConfig.ESCUDO_REPORTE
                )
                .agregarLineaCiudadFecha(ReportConfig.CIUDAD_REPORTE)
                .agregarAsunto(titulo)
                .agregarEspacio(10);
    }

    protected void agregarPieEstandar() {
        pdfBuilder.agregarFooterConFecha();
    }

    protected void finalizarReporte() {
        agregarPieEstandar();
        pdfBuilder.construir();
        abrirCarpetaDestino();
    }

    private void abrirCarpetaDestino() {
        try {
            if (rutaArchivo == null) return;
            File archivo = new File(rutaArchivo);
            File carpeta = archivo.getParentFile();
            if (carpeta != null && carpeta.exists() && Desktop.isDesktopSupported()) {
                Desktop.getDesktop().open(carpeta);
            }
        } catch (Exception ignore) {
        }
    }

    public String getRutaArchivo() { return rutaArchivo; }
    public PDFBuilder getPDFBuilder() { return pdfBuilder; }
}
