package database.models;

public class Caso {
    private int id;
    private String nombreCaso;
    private int estado;

    public Caso() {
    }

    public Caso(int id, String nombreCaso) {
        this.id = id;
        this.nombreCaso = nombreCaso;
    }

    public Caso(int id, String nombreCaso, int estado) {
        this.id = id;
        this.nombreCaso = nombreCaso;
        this.estado = estado;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNombreCaso() {
        return nombreCaso;
    }

    public void setNombreCaso(String nombreCaso) {
        this.nombreCaso = nombreCaso;
    }

    public int getEstado() {
        return estado;
    }

    public void setEstado(int estado) {
        this.estado = estado;
    }

    @Override
    public String toString() {
        return nombreCaso;
    }
}


