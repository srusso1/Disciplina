package database;

import database.models.Lugar;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class LugarDAO {

    public List<Lugar> obtenerTodos() {
        String sql = "SELECT id, nombre_lugar FROM lugares WHERE estado = 1 ORDER BY nombre_lugar ASC";
        List<Lugar> lugares = new ArrayList<>();
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = ConexionSQLite.conectar();
            if (conn == null) return lugares;

            pstmt = conn.prepareStatement(sql);
            rs = pstmt.executeQuery();

            while (rs.next()) {
                lugares.add(new Lugar(rs.getInt("id"), rs.getString("nombre_lugar")));
            }
        } catch (SQLException e) {
            System.err.println("Error al listar lugares: " + e.getMessage());
        } finally {
            try {
                if (rs != null) rs.close();
                if (pstmt != null) pstmt.close();
            } catch (SQLException e) {
                System.err.println("Error al cerrar recursos de lugares: " + e.getMessage());
            }
            ConexionSQLite.cerrarConexion();
        }

        return lugares;
    }
}


