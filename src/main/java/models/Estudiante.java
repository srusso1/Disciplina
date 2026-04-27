package models;

public class Estudiante extends Persona {
    private int identificacion;
    private int grado;
    private String genero;

    public Estudiante() {
        super();
    }

    public Estudiante(int id, int identificacion, int grado, String apellido1, String apellido2, String nombre1, String nombre2, String genero) {
        super(id, nombre1, nombre2, apellido1, apellido2);
        this.identificacion = identificacion;
        this.grado = grado;
        this.genero = genero;
    }

    public int getIdentificacion() { return identificacion; }
    public void setIdentificacion(int identificacion) { this.identificacion = identificacion; }

    public int getGrado() { return grado; }
    public void setGrado(int grado) { this.grado = grado; }

    public String getGenero() { return genero; }
    public void setGenero(String genero) { this.genero = genero; }
}
