package models;

public abstract class Persona {
    protected int id;
    protected String nombre1;
    protected String nombre2;
    protected String apellido1;
    protected String apellido2;

    public Persona() {}

    public Persona(int id, String nombre1, String nombre2, String apellido1, String apellido2) {
        this.id = id;
        this.nombre1 = nombre1;
        this.nombre2 = nombre2;
        this.apellido1 = apellido1;
        this.apellido2 = apellido2;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getNombre1() { return nombre1; }
    public void setNombre1(String nombre1) { this.nombre1 = nombre1; }

    public String getNombre2() { return nombre2; }
    public void setNombre2(String nombre2) { this.nombre2 = nombre2; }

    public String getApellido1() { return apellido1; }
    public void setApellido1(String apellido1) { this.apellido1 = apellido1; }

    public String getApellido2() { return apellido2; }
    public void setApellido2(String apellido2) { this.apellido2 = apellido2; }
}
