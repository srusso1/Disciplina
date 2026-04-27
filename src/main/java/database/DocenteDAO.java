package database;

import models.Docente;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class DocenteDAO {

    public List<Docente> obtenerTodos() {
        String sql = "SELECT id, nombre_1, nombre_2, apellido_1, apellido_2 " +
                "FROM docentes ORDER BY nombre_1 ASC, nombre_2 ASC, apellido_1 ASC, apellido_2 ASC";
        List<Docente> docentes = new ArrayList<>();
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = ConexionSQLite.conectar();
            if (conn == null) return docentes;

            pstmt = conn.prepareStatement(sql);
            rs = pstmt.executeQuery();

            while (rs.next()) {
                docentes.add(new Docente(
                        rs.getInt("id"),
                        rs.getString("nombre_1"),
                        rs.getString("nombre_2"),
                        rs.getString("apellido_1"),
                        rs.getString("apellido_2")
                ));
            }
        } catch (SQLException e) {
            System.err.println("Error al listar docentes: " + e.getMessage());
        } finally {
            try {
                if (rs != null) rs.close();
                if (pstmt != null) pstmt.close();
            } catch (SQLException e) {
                System.err.println("Error al cerrar recursos de docentes: " + e.getMessage());
            }
            ConexionSQLite.cerrarConexion();
        }

        return docentes;
    }
}

