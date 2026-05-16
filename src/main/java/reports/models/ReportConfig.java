package reports.models;

import java.time.LocalDate;

/**
 * Configuración y parámetros comunes para la generación de reportes PDF.
 * Replica el enfoque del proyecto BookTech para mantener la identidad institucional.
 */
public class ReportConfig {
    // Identidad institucional
    public static final String INSTITUCION = "INSTITUCION EDUCATIVA TRUJILLO"; // Ajustable
    public static final String ESCUELA = "Becerril - Cesar"; // Ajustable
    public static final String CIUDAD_REPORTE = "Becerril"; // Ajustable
    public static final String ESCUDO_REPORTE = "/images/escudo.png"; // Recurso del classpath

    // Rutas de salida
    public static final String RUTA_REPORTES = "reportes/"; // Carpeta sugerida de destino
    public static final String FORMATO_FECHA = "yyyy-MM-dd";

    // Parámetros dinámicos opcionales (extensibles según necesidad)
    private LocalDate fechaInicio;
    private LocalDate fechaFin;
    private String tipoReporte;
    private int idEstudiante;
    private int grado;
    private Integer idCaso;
    private Integer anioComparativo1;
    private Integer anioComparativo2;
    private boolean incluirTablas = true;

    public ReportConfig() {}

    public ReportConfig(LocalDate fechaInicio, LocalDate fechaFin) {
        this.fechaInicio = fechaInicio;
        this.fechaFin = fechaFin;
    }

    public LocalDate getFechaInicio() { return fechaInicio; }
    public void setFechaInicio(LocalDate fechaInicio) { this.fechaInicio = fechaInicio; }

    public LocalDate getFechaFin() { return fechaFin; }
    public void setFechaFin(LocalDate fechaFin) { this.fechaFin = fechaFin; }

    public String getTipoReporte() { return tipoReporte; }
    public void setTipoReporte(String tipoReporte) { this.tipoReporte = tipoReporte; }

    public int getIdEstudiante() { return idEstudiante; }
    public void setIdEstudiante(int idEstudiante) { this.idEstudiante = idEstudiante; }

    public int getGrado() { return grado; }
    public void setGrado(int grado) { this.grado = grado; }

    public Integer getIdCaso() { return idCaso; }
    public void setIdCaso(Integer idCaso) { this.idCaso = idCaso; }

    public Integer getAnioComparativo1() { return anioComparativo1; }
    public void setAnioComparativo1(Integer anioComparativo1) { this.anioComparativo1 = anioComparativo1; }

    public Integer getAnioComparativo2() { return anioComparativo2; }
    public void setAnioComparativo2(Integer anioComparativo2) { this.anioComparativo2 = anioComparativo2; }

    public boolean isIncluirTablas() { return incluirTablas; }
    public void setIncluirTablas(boolean incluirTablas) { this.incluirTablas = incluirTablas; }

    /**
     * Devuelve una ruta de archivo de reporte basada en la carpeta por defecto.
     */
    public String getRutaArchivoReporte(String nombreArchivo) {
        return RUTA_REPORTES + nombreArchivo;
    }
}
