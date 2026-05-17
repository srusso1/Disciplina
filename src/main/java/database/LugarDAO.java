package database;

import database.models.Lugar;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class LugarDAO {

    public List<Lugar> obtenerTodosConEstado() {
        String sql = "SELECT id, nombre_lugar, estado FROM lugares ORDER BY nombre_lugar ASC";
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
                lugares.add(new Lugar(rs.getInt("id"), rs.getString("nombre_lugar"), rs.getInt("estado")));
            }
        } catch (SQLException e) {
            System.err.println("Error al listar lugares con estado: " + e.getMessage());
        } finally {
            try {
                if (rs != null) rs.close();
                if (pstmt != null) pstmt.close();
            } catch (SQLException e) {
                System.err.println("Error al cerrar recursos de lugares con estado: " + e.getMessage());
            }
            ConexionSQLite.cerrarConexion();
        }

        return lugares;
    }

    public boolean agregarLugar(String nombreLugar) {
        String sql = "INSERT INTO lugares (nombre_lugar, estado) VALUES (?, 1)";
        Connection conn = null;
        PreparedStatement pstmt = null;

        try {
            conn = ConexionSQLite.conectar();
            if (conn == null) return false;

            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, nombreLugar);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error al agregar lugar: " + e.getMessage());
        } finally {
            try {
                if (pstmt != null) pstmt.close();
            } catch (SQLException e) {
                System.err.println("Error al cerrar recursos de agregar lugar: " + e.getMessage());
            }
            ConexionSQLite.cerrarConexion();
        }

        return false;
    }

    public boolean actualizarLugar(Lugar lugar) {
        String sql = "UPDATE lugares SET nombre_lugar = ?, estado = ? WHERE id = ?";
        Connection conn = null;
        PreparedStatement pstmt = null;

        try {
            conn = ConexionSQLite.conectar();
            if (conn == null) return false;

            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, lugar.getNombreLugar());
            pstmt.setInt(2, lugar.getEstado());
            pstmt.setInt(3, lugar.getId());
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error al actualizar lugar: " + e.getMessage());
        } finally {
            try {
                if (pstmt != null) pstmt.close();
            } catch (SQLException e) {
                System.err.println("Error al cerrar recursos de actualizar lugar: " + e.getMessage());
            }
            ConexionSQLite.cerrarConexion();
        }

        return false;
    }

    public boolean cambiarEstadoLugar(int id, int nuevoEstado) {
        String sql = "UPDATE lugares SET estado = ? WHERE id = ?";
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
            System.err.println("Error al cambiar estado del lugar: " + e.getMessage());
        } finally {
            try {
                if (pstmt != null) pstmt.close();
            } catch (SQLException e) {
                System.err.println("Error al cerrar recursos de cambiar estado de lugar: " + e.getMessage());
            }
            ConexionSQLite.cerrarConexion();
        }

        return false;
    }

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


