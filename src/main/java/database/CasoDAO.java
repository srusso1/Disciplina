package database;

import database.models.Caso;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class CasoDAO {

    public List<Caso> obtenerTodos() {
        String sql = "SELECT id, nombre_caso FROM casos WHERE estado = 1 ORDER BY nombre_caso ASC";
        List<Caso> casos = new ArrayList<>();
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = ConexionSQLite.conectar();
            if (conn == null) return casos;

            pstmt = conn.prepareStatement(sql);
            rs = pstmt.executeQuery();

            while (rs.next()) {
                casos.add(new Caso(rs.getInt("id"), rs.getString("nombre_caso")));
            }
        } catch (SQLException e) {
            System.err.println("Error al listar casos: " + e.getMessage());
        } finally {
            try {
                if (rs != null) rs.close();
                if (pstmt != null) pstmt.close();
            } catch (SQLException e) {
                System.err.println("Error al cerrar recursos de casos: " + e.getMessage());
            }
            ConexionSQLite.cerrarConexion();
        }

        return casos;
    }
}

