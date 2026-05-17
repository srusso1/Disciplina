package database;

import database.models.Caso;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class CasoDAO {

    public List<Caso> obtenerTodosConEstado() {
        String sql = "SELECT id, nombre_caso, estado FROM casos ORDER BY nombre_caso ASC";
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
                casos.add(new Caso(rs.getInt("id"), rs.getString("nombre_caso"), rs.getInt("estado")));
            }
        } catch (SQLException e) {
            System.err.println("Error al listar casos con estado: " + e.getMessage());
        } finally {
            try {
                if (rs != null) rs.close();
                if (pstmt != null) pstmt.close();
            } catch (SQLException e) {
                System.err.println("Error al cerrar recursos de casos con estado: " + e.getMessage());
            }
            ConexionSQLite.cerrarConexion();
        }

        return casos;
    }

    public boolean agregarCaso(String nombreCaso) {
        String sql = "INSERT INTO casos (nombre_caso, estado) VALUES (?, 1)";
        Connection conn = null;
        PreparedStatement pstmt = null;

        try {
            conn = ConexionSQLite.conectar();
            if (conn == null) return false;

            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, nombreCaso);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error al agregar caso: " + e.getMessage());
        } finally {
            try {
                if (pstmt != null) pstmt.close();
            } catch (SQLException e) {
                System.err.println("Error al cerrar recursos de agregar caso: " + e.getMessage());
            }
            ConexionSQLite.cerrarConexion();
        }

        return false;
    }

    public boolean actualizarCaso(Caso caso) {
        String sql = "UPDATE casos SET nombre_caso = ?, estado = ? WHERE id = ?";
        Connection conn = null;
        PreparedStatement pstmt = null;

        try {
            conn = ConexionSQLite.conectar();
            if (conn == null) return false;

            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, caso.getNombreCaso());
            pstmt.setInt(2, caso.getEstado());
            pstmt.setInt(3, caso.getId());
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error al actualizar caso: " + e.getMessage());
        } finally {
            try {
                if (pstmt != null) pstmt.close();
            } catch (SQLException e) {
                System.err.println("Error al cerrar recursos de actualizar caso: " + e.getMessage());
            }
            ConexionSQLite.cerrarConexion();
        }

        return false;
    }

    public boolean cambiarEstadoCaso(int id, int nuevoEstado) {
        String sql = "UPDATE casos SET estado = ? WHERE id = ?";
        Connection conn = null;
        PreparedStatement pstmt = null;

        try {
            conn = ConexionSQLite.conectar();
            if (conn == null) return false;

            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, nuevoEstado);
            pstmt.setInt(2, id);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error al cambiar estado del caso: " + e.getMessage());
        } finally {
            try {
                if (pstmt != null) pstmt.close();
            } catch (SQLException e) {
                System.err.println("Error al cerrar recursos de cambiar estado de caso: " + e.getMessage());
            }
            ConexionSQLite.cerrarConexion();
        }

        return false;
    }

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

