package database.models;

public class Falta {
    private int id;
    private int idEstudiante;
    private int idCaso;
    private int idLugar;
    private int idDocente;
    private int tipoFalta;
    private String descargo;
    private String accionRestaurativa;

    public Falta() {
    }

    public Falta(int id, int idEstudiante, int idCaso, int idLugar, int idDocente, int tipoFalta, String descargo, String accionRestaurativa) {
        this.id = id;
        this.idEstudiante = idEstudiante;
        this.idCaso = idCaso;
        this.idLugar = idLugar;
        this.idDocente = idDocente;
        this.tipoFalta = tipoFalta;
        this.descargo = descargo;
        this.accionRestaurativa = accionRestaurativa;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getIdEstudiante() {
        return idEstudiante;
    }

    public void setIdEstudiante(int idEstudiante) {
        this.idEstudiante = idEstudiante;
    }

    public int getIdCaso() {
        return idCaso;
    }

    public void setIdCaso(int idCaso) {
        this.idCaso = idCaso;
    }

    public int getIdLugar() {
        return idLugar;
    }

    public void setIdLugar(int idLugar) {
        this.idLugar = idLugar;
    }

    public int getIdDocente() {
        return idDocente;
    }

    public void setIdDocente(int idDocente) {
        this.idDocente = idDocente;
    }

    public int getTipoFalta() {
        return tipoFalta;
    }

    public void setTipoFalta(int tipoFalta) {
        this.tipoFalta = tipoFalta;
    }

    public String getDescargo() {
        return descargo;
    }

    public void setDescargo(String descargo) {
        this.descargo = descargo;
    }

    public String getAccionRestaurativa() {
        return accionRestaurativa;
    }

    public void setAccionRestaurativa(String accionRestaurativa) {
        this.accionRestaurativa = accionRestaurativa;
    }
}

