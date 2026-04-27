package database.models;

public class Lugar {
    private int id;
    private String nombreLugar;

    public Lugar() {
    }

    public Lugar(int id, String nombreLugar) {
        this.id = id;
        this.nombreLugar = nombreLugar;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNombreLugar() {
        return nombreLugar;
    }

    public void setNombreLugar(String nombreLugar) {
        this.nombreLugar = nombreLugar;
    }

    @Override
    public String toString() {
        return nombreLugar;
    }
}

