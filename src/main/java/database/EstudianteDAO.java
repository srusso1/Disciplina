package database;

import models.Estudiante;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class EstudianteDAO {

    public boolean agregarEstudiante(Estudiante estudiante) {
        String sql = "INSERT INTO estudiantes (identificacion, grado, apellido_1, apellido_2, nombre_1, nombre_2, genero) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?)";
        Connection conn = null;
        PreparedStatement pstmt = null;

        try {
            conn = ConexionSQLite.conectar();
            if (conn == null) return false;

            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, estudiante.getIdentificacion());
            pstmt.setInt(2, estudiante.getGrado());
            pstmt.setString(3, estudiante.getApellido1());
            pstmt.setString(4, estudiante.getApellido2());
            pstmt.setString(5, estudiante.getNombre1());
            pstmt.setString(6, estudiante.getNombre2());
            pstmt.setString(7, estudiante.getGenero());

            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error al agregar estudiante: " + e.getMessage());
        } finally {
            try {
                if (pstmt != null) pstmt.close();
            } catch (SQLException e) {
                System.err.println("Error al cerrar recursos de agregar estudiante: " + e.getMessage());
            }
            ConexionSQLite.cerrarConexion();
        }

        return false;
    }

    public boolean actualizarEstudiante(Estudiante estudiante) {
        String sql = "UPDATE estudiantes SET identificacion = ?, grado = ?, apellido_1 = ?, apellido_2 = ?, nombre_1 = ?, nombre_2 = ?, genero = ?, estado = ?, año_escolar = ? " +
                "WHERE id = ?";
        Connection conn = null;
        PreparedStatement pstmt = null;

        try {
            conn = ConexionSQLite.conectar();
            if (conn == null) return false;

            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, estudiante.getIdentificacion());
            pstmt.setInt(2, estudiante.getGrado());
            pstmt.setString(3, estudiante.getApellido1());
            pstmt.setString(4, estudiante.getApellido2());
            pstmt.setString(5, estudiante.getNombre1());
            pstmt.setString(6, estudiante.getNombre2());
            pstmt.setString(7, estudiante.getGenero());
            pstmt.setInt(8, estudiante.getEstado());
            pstmt.setInt(9, estudiante.getAño_escolar());
            pstmt.setInt(10, estudiante.getId());

            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error al actualizar estudiante: " + e.getMessage());
        } finally {
            try {
                if (pstmt != null) pstmt.close();
            } catch (SQLException e) {
                System.err.println("Error al cerrar recursos de actualizar estudiante: " + e.getMessage());
            }
            ConexionSQLite.cerrarConexion();
        }

        return false;
    }

    public boolean existeIdentificacion(int identificacion) {
        String sql = "SELECT COUNT(*) total FROM estudiantes WHERE identificacion = ?";
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = ConexionSQLite.conectar();
            if (conn == null) return false;

            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, identificacion);
            rs = pstmt.executeQuery();

            return rs.next() && rs.getInt("total") > 0;
        } catch (SQLException e) {
            System.err.println("Error al verificar identificación de estudiante: " + e.getMessage());
        } finally {
            try {
                if (rs != null) rs.close();
                if (pstmt != null) pstmt.close();
            } catch (SQLException e) {
                System.err.println("Error al cerrar recursos de verificación de estudiante: " + e.getMessage());
            }
            ConexionSQLite.cerrarConexion();
        }

        return false;
    }

    public boolean existeIdentificacionEnOtroRegistro(int identificacion, int idActual) {
        String sql = "SELECT COUNT(*) total FROM estudiantes WHERE identificacion = ? AND id <> ?";
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = ConexionSQLite.conectar();
            if (conn == null) return false;

            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, identificacion);
            pstmt.setInt(2, idActual);
            rs = pstmt.executeQuery();

            return rs.next() && rs.getInt("total") > 0;
        } catch (SQLException e) {
            System.err.println("Error al verificar identificación duplicada: " + e.getMessage());
        } finally {
            try {
                if (rs != null) rs.close();
                if (pstmt != null) pstmt.close();
            } catch (SQLException e) {
                System.err.println("Error al cerrar recursos de verificación duplicada: " + e.getMessage());
            }
            ConexionSQLite.cerrarConexion();
        }

        return false;
    }

    public List<Estudiante> obtenerTodos() {
        String sql = "SELECT id, identificacion, grado, apellido_1, apellido_2, nombre_1, nombre_2, genero, estado, año_escolar " +
                "FROM estudiantes WHERE estado = 1 ORDER BY nombre_1 ASC, nombre_2 ASC, apellido_1 ASC, apellido_2 ASC";
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
                Estudiante est = new Estudiante(
                        rs.getInt("id"),
                        rs.getInt("identificacion"),
                        rs.getInt("grado"),
                        rs.getString("apellido_1"),
                        rs.getString("apellido_2"),
                        rs.getString("nombre_1"),
                        rs.getString("nombre_2"),
                        rs.getString("genero")
                );
                est.setEstado(rs.getInt("estado"));
                est.setAño_escolar(rs.getInt("año_escolar"));
                estudiantes.add(est);
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

    public Estudiante obtenerPorIdentificacion(int identificacion) {
        String sql = "SELECT id, identificacion, grado, apellido_1, apellido_2, nombre_1, nombre_2, genero, estado, año_escolar " +
                "FROM estudiantes WHERE identificacion = ? LIMIT 1";
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = ConexionSQLite.conectar();
            if (conn == null) return null;

            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, identificacion);
            rs = pstmt.executeQuery();

            if (rs.next()) {
                Estudiante est = new Estudiante(
                        rs.getInt("id"),
                        rs.getInt("identificacion"),
                        rs.getInt("grado"),
                        rs.getString("apellido_1"),
                        rs.getString("apellido_2"),
                        rs.getString("nombre_1"),
                        rs.getString("nombre_2"),
                        rs.getString("genero")
                );
                est.setEstado(rs.getInt("estado"));
                est.setAño_escolar(rs.getInt("año_escolar"));
                return est;
            }
        } catch (SQLException e) {
            System.err.println("Error al obtener estudiante por identificación: " + e.getMessage());
        } finally {
            try {
                if (rs != null) rs.close();
                if (pstmt != null) pstmt.close();
            } catch (SQLException e) {
                System.err.println("Error al cerrar recursos: " + e.getMessage());
            }
            ConexionSQLite.cerrarConexion();
        }

        return null;
    }

    public List<Estudiante> obtenerTodosConEstado() {
        String sql = "SELECT id, identificacion, grado, apellido_1, apellido_2, nombre_1, nombre_2, genero, estado, año_escolar " +
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
                Estudiante est = new Estudiante(
                        rs.getInt("id"),
                        rs.getInt("identificacion"),
                        rs.getInt("grado"),
                        rs.getString("apellido_1"),
                        rs.getString("apellido_2"),
                        rs.getString("nombre_1"),
                        rs.getString("nombre_2"),
                        rs.getString("genero")
                );
                est.setEstado(rs.getInt("estado"));
                est.setAño_escolar(rs.getInt("año_escolar"));
                estudiantes.add(est);
            }
        } catch (SQLException e) {
            System.err.println("Error al listar estudiantes con estado: " + e.getMessage());
        } finally {
            try {
                if (rs != null) rs.close();
                if (pstmt != null) pstmt.close();
            } catch (SQLException e) {
                System.err.println("Error al cerrar recursos: " + e.getMessage());
            }
            ConexionSQLite.cerrarConexion();
        }

        return estudiantes;
    }

    public boolean cambiarEstadoEstudiante(int id, int estado) {
        String sql = "UPDATE estudiantes SET estado = ? WHERE id = ?";
        Connection conn = null;
        PreparedStatement pstmt = null;

        try {
            conn = ConexionSQLite.conectar();
            if (conn == null) return false;

            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, estado);
            pstmt.setInt(2, id);

            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error al cambiar estado del estudiante: " + e.getMessage());
        } finally {
            try {
                if (pstmt != null) pstmt.close();
            } catch (SQLException e) {
                System.err.println("Error al cerrar recursos: " + e.getMessage());
            }
            ConexionSQLite.cerrarConexion();
        }

        return false;
    }
}

