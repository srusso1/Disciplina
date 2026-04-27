package database;

import database.models.Falta;
import utils.Fechas;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class FaltaDAO {

    public boolean registrar(Falta falta) {
        String sql = "INSERT INTO faltas (id_estudiante, id_caso, id_lugar, id_docente, tipo_falta, descargo, accion_restaurativa, fecha) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        Connection conn = null;
        PreparedStatement pstmt = null;

        try {
            conn = ConexionSQLite.conectar();
            if (conn == null) return false;

            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, falta.getIdEstudiante());
            pstmt.setInt(2, falta.getIdCaso());
            pstmt.setInt(3, falta.getIdLugar());
            pstmt.setInt(4, falta.getIdDocente());
            pstmt.setInt(5, falta.getTipoFalta());
            pstmt.setString(6, falta.getDescargo());
            pstmt.setString(7, falta.getAccionRestaurativa());
            pstmt.setString(8, Fechas.fechaActualISO());

            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error al registrar falta: " + e.getMessage());
        } finally {
            try {
                if (pstmt != null) pstmt.close();
            } catch (SQLException e) {
                System.err.println("Error al cerrar recursos de registrar falta: " + e.getMessage());
            }
            ConexionSQLite.cerrarConexion();
        }

        return false;
    }
}


