package database.models;

public class Lugar {
    private int id;
    private String nombreLugar;
    private int estado;

    public Lugar() {
    }

    public Lugar(int id, String nombreLugar) {
        this.id = id;
        this.nombreLugar = nombreLugar;
    }

    public Lugar(int id, String nombreLugar, int estado) {
        this.id = id;
        this.nombreLugar = nombreLugar;
        this.estado = estado;
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

    public int getEstado() {
        return estado;
    }

    public void setEstado(int estado) {
        this.estado = estado;
    }

    @Override
    public String toString() {
        return nombreLugar;
    }
}

