package models;

public class Estudiante extends Persona {
    private int identificacion;
    private int grado;
    private String genero;
    private int estado;              // 1=activo, 0=inactivo
    private int año_escolar;         // Año escolar actual del estudiante

    public Estudiante() {
        super();
        this.estado = 1;             // Por defecto activo
        this.año_escolar = 2026;     // Por defecto año actual
    }

    public Estudiante(int id, int identificacion, int grado, String apellido1, String apellido2, String nombre1, String nombre2, String genero) {
        super(id, nombre1, nombre2, apellido1, apellido2);
        this.identificacion = identificacion;
        this.grado = grado;
        this.genero = genero;
        this.estado = 1;
        this.año_escolar = 2026;
    }

    // Getters y setters
    public int getIdentificacion() { return identificacion; }
    public void setIdentificacion(int identificacion) { this.identificacion = identificacion; }

    public int getGrado() { return grado; }
    public void setGrado(int grado) { this.grado = grado; }

    public String getGenero() { return genero; }
    public void setGenero(String genero) { this.genero = genero; }

    public int getEstado() { return estado; }
    public void setEstado(int estado) { this.estado = estado; }

    public int getAño_escolar() { return año_escolar; }
    public void setAño_escolar(int año_escolar) { this.año_escolar = año_escolar; }
}
