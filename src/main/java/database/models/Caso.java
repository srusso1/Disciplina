package database.models;

public class Caso {
    private int id;
    private String nombreCaso;

    public Caso() {
    }

    public Caso(int id, String nombreCaso) {
        this.id = id;
        this.nombreCaso = nombreCaso;
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

    @Override
    public String toString() {
        return nombreCaso;
    }
}


