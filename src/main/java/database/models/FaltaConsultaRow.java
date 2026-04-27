package database.models;

public class FaltaConsultaRow {
    private final int idFalta;
    private final int idEstudiante;
    private final int idLugar;
    private final String fecha;
    private final String estudiante;
    private final int grado;
    private final String identificacion;
    private final String tipoFalta;
    private final String caso;
    private final String lugar;
    private final String docente;
    private final String descargo;
    private final String accionRestaurativa;

    public FaltaConsultaRow(int idFalta, int idEstudiante, int idLugar, String fecha, String estudiante, 
                           int grado, String identificacion, String tipoFalta, String caso, 
                           String lugar, String docente, String descargo, String accionRestaurativa) {
        this.idFalta = idFalta;
        this.idEstudiante = idEstudiante;
        this.idLugar = idLugar;
        this.fecha = fecha;
        this.estudiante = estudiante;
        this.grado = grado;
        this.identificacion = identificacion;
        this.tipoFalta = tipoFalta;
        this.caso = caso;
        this.lugar = lugar;
        this.docente = docente;
        this.descargo = descargo;
        this.accionRestaurativa = accionRestaurativa;
    }

    public int getIdFalta() { return idFalta; }
    public int getIdEstudiante() { return idEstudiante; }
    public int getIdLugar() { return idLugar; }
    public String getFecha() { return fecha; }
    public String getEstudiante() { return estudiante; }
    public int getGrado() { return grado; }
    public String getIdentificacion() { return identificacion; }
    public String getTipoFalta() { return tipoFalta; }
    public String getCaso() { return caso; }
    public String getLugar() { return lugar; }
    public String getDocente() { return docente; }
    public String getDescargo() { return descargo; }
    public String getAccionRestaurativa() { return accionRestaurativa; }
}




