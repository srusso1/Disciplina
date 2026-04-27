package database;

import models.Estudiante;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class EstudianteDAO {

    public List<Estudiante> obtenerTodos() {
        String sql = "SELECT id, identificacion, grado, apellido_1, apellido_2, nombre_1, nombre_2, genero " +
                "FROM estudiantes ORDER BY nombre_1 ASC, nombre_2 ASC, apellido_1 ASC, apellido_2 ASC";
        List<Estudiante> estudiantes = new ArrayList<>();
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = ConexionSQLite.conectar();
            if (conn == null) return estudiantes;

            pstmt = conn.prepareStatement(sql);
            rs = pstmt.executeQuery();

            while (rs.next()) {
                estudiantes.add(new Estudiante(
                        rs.getInt("id"),
                        rs.getInt("identificacion"),
                        rs.getInt("grado"),
                        rs.getString("apellido_1"),
                        rs.getString("apellido_2"),
                        rs.getString("nombre_1"),
                        rs.getString("nombre_2"),
                        rs.getString("genero")
                ));
            }
        } catch (SQLException e) {
            System.err.println("Error al listar estudiantes: " + e.getMessage());
        } finally {
            try {
                if (rs != null) rs.close();
                if (pstmt != null) pstmt.close();
            } catch (SQLException e) {
                System.err.println("Error al cerrar recursos de estudiantes: " + e.getMessage());
            }
            ConexionSQLite.cerrarConexion();
        }

        return estudiantes;
    }

    public int contarFaltasPrevias(int idEstudiante) {
        String sql = "SELECT COUNT(*) total FROM faltas WHERE id_estudiante = ?";
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = ConexionSQLite.conectar();
            if (conn == null) return 0;

            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, idEstudiante);
            rs = pstmt.executeQuery();

            if (rs.next()) {
                return rs.getInt("total");
            }
        } catch (SQLException e) {
            System.err.println("Error al contar faltas previas del estudiante: " + e.getMessage());
        } finally {
            try {
                if (rs != null) rs.close();
                if (pstmt != null) pstmt.close();
            } catch (SQLException e) {
                System.err.println("Error al cerrar recursos de faltas previas: " + e.getMessage());
            }
            ConexionSQLite.cerrarConexion();
        }

        return 0;
    }
}

