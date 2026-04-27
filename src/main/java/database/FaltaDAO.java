package database;

import database.models.Falta;
import database.models.FaltaConsultaRow;
import utils.Fechas;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
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

    public List<FaltaConsultaRow> consultarFaltas(
            Integer idEstudiante,
            String fechaDesdeISO,
            String fechaHastaISO,
            Integer tipoFalta,
            Integer idCaso,
            Integer idLugar
    ) {
        List<FaltaConsultaRow> filas = new ArrayList<>();

        StringBuilder sql = new StringBuilder(
                "SELECT f.id, f.id_lugar, f.fecha, e.id AS id_estudiante, " +
                        "TRIM(COALESCE(e.nombre_1,'') || ' ' || COALESCE(e.nombre_2,'') || ' ' || COALESCE(e.apellido_1,'') || ' ' || COALESCE(e.apellido_2,'')) AS estudiante, " +
                        "e.grado, e.identificacion, f.tipo_falta, c.nombre_caso AS caso, l.nombre_lugar AS lugar, " +
                        "TRIM(COALESCE(d.nombre_1,'') || ' ' || COALESCE(d.nombre_2,'') || ' ' || COALESCE(d.apellido_1,'') || ' ' || COALESCE(d.apellido_2,'')) AS docente, " +
                        "COALESCE(f.descargo, '') AS descargo, COALESCE(f.accion_restaurativa, '') AS accion_restaurativa " +
                        "FROM faltas f " +
                        "INNER JOIN estudiantes e ON e.id = f.id_estudiante " +
                        "INNER JOIN casos c ON c.id = f.id_caso " +
                        "INNER JOIN lugares l ON l.id = f.id_lugar " +
                        "LEFT JOIN docentes d ON d.id = f.id_docente " +
                        "WHERE 1 = 1 "
        );

        if (idEstudiante != null) sql.append("AND f.id_estudiante = ? ");
        if (fechaDesdeISO != null && !fechaDesdeISO.trim().isEmpty()) sql.append("AND f.fecha >= ? ");
        if (fechaHastaISO != null && !fechaHastaISO.trim().isEmpty()) sql.append("AND f.fecha <= ? ");
        if (tipoFalta != null) sql.append("AND f.tipo_falta = ? ");
        if (idCaso != null) sql.append("AND f.id_caso = ? ");
        if (idLugar != null) sql.append("AND f.id_lugar = ? ");

        sql.append("ORDER BY f.fecha DESC, f.id DESC");

        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = ConexionSQLite.conectar();
            if (conn == null) return filas;

            pstmt = conn.prepareStatement(sql.toString());

            int idx = 1;
            if (idEstudiante != null) pstmt.setInt(idx++, idEstudiante);
            if (fechaDesdeISO != null && !fechaDesdeISO.trim().isEmpty()) pstmt.setString(idx++, fechaDesdeISO);
            if (fechaHastaISO != null && !fechaHastaISO.trim().isEmpty()) pstmt.setString(idx++, fechaHastaISO);
            if (tipoFalta != null) pstmt.setInt(idx++, tipoFalta);
            if (idCaso != null) pstmt.setInt(idx++, idCaso);
            if (idLugar != null) pstmt.setInt(idx++, idLugar);

            rs = pstmt.executeQuery();

            while (rs.next()) {
                String fechaUI = Fechas.convertirAUI(rs.getString("fecha"));
                if (fechaUI == null) {
                    fechaUI = rs.getString("fecha");
                }

                filas.add(new FaltaConsultaRow(
                        rs.getInt("id"),
                        rs.getInt("id_estudiante"),
                        rs.getInt("id_lugar"),
                        fechaUI,
                        rs.getString("estudiante"),
                        rs.getInt("grado"),
                        rs.getString("identificacion"),
                        "Tipo " + rs.getInt("tipo_falta"),
                        rs.getString("caso"),
                        rs.getString("lugar"),
                        rs.getString("docente"),
                        rs.getString("descargo"),
                        rs.getString("accion_restaurativa")
                ));
            }
        } catch (SQLException e) {
            System.err.println("Error al consultar faltas: " + e.getMessage());
        } finally {
            try {
                if (rs != null) rs.close();
                if (pstmt != null) pstmt.close();
            } catch (SQLException e) {
                System.err.println("Error al cerrar recursos de consulta faltas: " + e.getMessage());
            }
            ConexionSQLite.cerrarConexion();
        }

        return filas;
    }

    public int obtenerTotalFaltas() {
        String sql = "SELECT COUNT(*) total FROM faltas";
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = ConexionSQLite.conectar();
            if (conn == null) return 0;

            pstmt = conn.prepareStatement(sql);
            rs = pstmt.executeQuery();

            if (rs.next()) {
                return rs.getInt("total");
            }
        } catch (SQLException e) {
            System.err.println("Error al contar total de faltas: " + e.getMessage());
        } finally {
            try {
                if (rs != null) rs.close();
                if (pstmt != null) pstmt.close();
            } catch (SQLException e) {
                System.err.println("Error al cerrar recursos: " + e.getMessage());
            }
            ConexionSQLite.cerrarConexion();
        }

        return 0;
    }

    public Integer obtenerTipoFaltaMasComun() {
        String sql = "SELECT tipo_falta FROM faltas GROUP BY tipo_falta ORDER BY COUNT(*) DESC LIMIT 1";
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = ConexionSQLite.conectar();
            if (conn == null) return null;

            pstmt = conn.prepareStatement(sql);
            rs = pstmt.executeQuery();

            if (rs.next()) {
                return rs.getInt("tipo_falta");
            }
        } catch (SQLException e) {
            System.err.println("Error al obtener tipo falta más común: " + e.getMessage());
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

    public String obtenerCasoMasComun() {
        String sql = "SELECT c.nombre_caso FROM faltas f " +
                "INNER JOIN casos c ON c.id = f.id_caso " +
                "GROUP BY f.id_caso ORDER BY COUNT(*) DESC LIMIT 1";
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = ConexionSQLite.conectar();
            if (conn == null) return "N/A";

            pstmt = conn.prepareStatement(sql);
            rs = pstmt.executeQuery();

            if (rs.next()) {
                return rs.getString("nombre_caso");
            }
        } catch (SQLException e) {
            System.err.println("Error al obtener caso más común: " + e.getMessage());
        } finally {
            try {
                if (rs != null) rs.close();
                if (pstmt != null) pstmt.close();
            } catch (SQLException e) {
                System.err.println("Error al cerrar recursos: " + e.getMessage());
            }
            ConexionSQLite.cerrarConexion();
        }

        return "N/A";
    }

    public String obtenerLugarMasFaltas() {
        String sql = "SELECT l.nombre_lugar FROM faltas f " +
                "INNER JOIN lugares l ON l.id = f.id_lugar " +
                "GROUP BY f.id_lugar ORDER BY COUNT(*) DESC LIMIT 1";
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = ConexionSQLite.conectar();
            if (conn == null) return "N/A";

            pstmt = conn.prepareStatement(sql);
            rs = pstmt.executeQuery();

            if (rs.next()) {
                return rs.getString("nombre_lugar");
            }
        } catch (SQLException e) {
            System.err.println("Error al obtener lugar con más faltas: " + e.getMessage());
        } finally {
            try {
                if (rs != null) rs.close();
                if (pstmt != null) pstmt.close();
            } catch (SQLException e) {
                System.err.println("Error al cerrar recursos: " + e.getMessage());
            }
            ConexionSQLite.cerrarConexion();
        }

        return "N/A";
    }

    public Map<String, Integer> obtenerFaltasPorCaso(String fechaDesde, String fechaHasta) {
        Map<String, Integer> resultado = new LinkedHashMap<>();
        StringBuilder sql = new StringBuilder(
                "SELECT c.nombre_caso, COUNT(*) as cantidad FROM faltas f " +
                "INNER JOIN casos c ON c.id = f.id_caso WHERE 1 = 1"
        );
        if (fechaDesde != null && !fechaDesde.isEmpty()) sql.append(" AND f.fecha >= ?");
        if (fechaHasta != null && !fechaHasta.isEmpty()) sql.append(" AND f.fecha <= ?");
        sql.append(" GROUP BY f.id_caso ORDER BY cantidad DESC");

        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = ConexionSQLite.conectar();
            if (conn == null) return resultado;

            pstmt = conn.prepareStatement(sql.toString());
            int idx = 1;
            if (fechaDesde != null && !fechaDesde.isEmpty()) pstmt.setString(idx++, fechaDesde);
            if (fechaHasta != null && !fechaHasta.isEmpty()) pstmt.setString(idx++, fechaHasta);

            rs = pstmt.executeQuery();
            while (rs.next()) {
                resultado.put(rs.getString("nombre_caso"), rs.getInt("cantidad"));
            }
        } catch (SQLException e) {
            System.err.println("Error al obtener faltas por caso: " + e.getMessage());
        } finally {
            try {
                if (rs != null) rs.close();
                if (pstmt != null) pstmt.close();
            } catch (SQLException e) {
                System.err.println("Error al cerrar recursos: " + e.getMessage());
            }
            ConexionSQLite.cerrarConexion();
        }
        return resultado;
    }

    public Map<Integer, Integer> obtenerFaltasPorTipo(String fechaDesde, String fechaHasta) {
        Map<Integer, Integer> resultado = new LinkedHashMap<>();
        StringBuilder sql = new StringBuilder("SELECT tipo_falta, COUNT(*) as cantidad FROM faltas WHERE 1 = 1");
        if (fechaDesde != null && !fechaDesde.isEmpty()) sql.append(" AND fecha >= ?");
        if (fechaHasta != null && !fechaHasta.isEmpty()) sql.append(" AND fecha <= ?");
        sql.append(" GROUP BY tipo_falta ORDER BY tipo_falta");

        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = ConexionSQLite.conectar();
            if (conn == null) return resultado;

            pstmt = conn.prepareStatement(sql.toString());
            int idx = 1;
            if (fechaDesde != null && !fechaDesde.isEmpty()) pstmt.setString(idx++, fechaDesde);
            if (fechaHasta != null && !fechaHasta.isEmpty()) pstmt.setString(idx++, fechaHasta);

            rs = pstmt.executeQuery();
            while (rs.next()) {
                resultado.put(rs.getInt("tipo_falta"), rs.getInt("cantidad"));
            }
        } catch (SQLException e) {
            System.err.println("Error al obtener faltas por tipo: " + e.getMessage());
        } finally {
            try {
                if (rs != null) rs.close();
                if (pstmt != null) pstmt.close();
            } catch (SQLException e) {
                System.err.println("Error al cerrar recursos: " + e.getMessage());
            }
            ConexionSQLite.cerrarConexion();
        }
        return resultado;
    }

    public Map<String, Integer> obtenerFaltasPorLugar(String fechaDesde, String fechaHasta) {
        Map<String, Integer> resultado = new LinkedHashMap<>();
        StringBuilder sql = new StringBuilder(
                "SELECT l.nombre_lugar, COUNT(*) as cantidad FROM faltas f " +
                "INNER JOIN lugares l ON l.id = f.id_lugar WHERE 1 = 1"
        );
        if (fechaDesde != null && !fechaDesde.isEmpty()) sql.append(" AND f.fecha >= ?");
        if (fechaHasta != null && !fechaHasta.isEmpty()) sql.append(" AND f.fecha <= ?");
        sql.append(" GROUP BY f.id_lugar ORDER BY cantidad DESC");

        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = ConexionSQLite.conectar();
            if (conn == null) return resultado;

            pstmt = conn.prepareStatement(sql.toString());
            int idx = 1;
            if (fechaDesde != null && !fechaDesde.isEmpty()) pstmt.setString(idx++, fechaDesde);
            if (fechaHasta != null && !fechaHasta.isEmpty()) pstmt.setString(idx++, fechaHasta);

            rs = pstmt.executeQuery();
            while (rs.next()) {
                resultado.put(rs.getString("nombre_lugar"), rs.getInt("cantidad"));
            }
        } catch (SQLException e) {
            System.err.println("Error al obtener faltas por lugar: " + e.getMessage());
        } finally {
            try {
                if (rs != null) rs.close();
                if (pstmt != null) pstmt.close();
            } catch (SQLException e) {
                System.err.println("Error al cerrar recursos: " + e.getMessage());
            }
            ConexionSQLite.cerrarConexion();
        }
        return resultado;
    }

    public Map<Integer, Integer> obtenerFaltasPorGrado(String fechaDesde, String fechaHasta) {
        Map<Integer, Integer> resultado = new LinkedHashMap<>();
        StringBuilder sql = new StringBuilder(
                "SELECT e.grado, COUNT(*) as cantidad FROM faltas f " +
                "INNER JOIN estudiantes e ON e.id = f.id_estudiante WHERE 1 = 1"
        );
        if (fechaDesde != null && !fechaDesde.isEmpty()) sql.append(" AND f.fecha >= ?");
        if (fechaHasta != null && !fechaHasta.isEmpty()) sql.append(" AND f.fecha <= ?");
        sql.append(" GROUP BY e.grado ORDER BY e.grado");

        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = ConexionSQLite.conectar();
            if (conn == null) return resultado;

            pstmt = conn.prepareStatement(sql.toString());
            int idx = 1;
            if (fechaDesde != null && !fechaDesde.isEmpty()) pstmt.setString(idx++, fechaDesde);
            if (fechaHasta != null && !fechaHasta.isEmpty()) pstmt.setString(idx++, fechaHasta);

            rs = pstmt.executeQuery();
            while (rs.next()) {
                resultado.put(rs.getInt("grado"), rs.getInt("cantidad"));
            }
        } catch (SQLException e) {
            System.err.println("Error al obtener faltas por grado: " + e.getMessage());
        } finally {
            try {
                if (rs != null) rs.close();
                if (pstmt != null) pstmt.close();
            } catch (SQLException e) {
                System.err.println("Error al cerrar recursos: " + e.getMessage());
            }
            ConexionSQLite.cerrarConexion();
        }
        return resultado;
    }

    public Map<String, Integer> obtenerFaltasPorMes(String fechaDesde, String fechaHasta) {
        Map<String, Integer> resultado = new LinkedHashMap<>();
        StringBuilder sql = new StringBuilder(
                "SELECT strftime('%Y-%m', f.fecha) as mes, COUNT(*) as cantidad FROM faltas f WHERE 1 = 1"
        );
        if (fechaDesde != null && !fechaDesde.isEmpty()) sql.append(" AND f.fecha >= ?");
        if (fechaHasta != null && !fechaHasta.isEmpty()) sql.append(" AND f.fecha <= ?");
        sql.append(" GROUP BY mes ORDER BY mes");

        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = ConexionSQLite.conectar();
            if (conn == null) return resultado;

            pstmt = conn.prepareStatement(sql.toString());
            int idx = 1;
            if (fechaDesde != null && !fechaDesde.isEmpty()) pstmt.setString(idx++, fechaDesde);
            if (fechaHasta != null && !fechaHasta.isEmpty()) pstmt.setString(idx++, fechaHasta);

            rs = pstmt.executeQuery();
            while (rs.next()) {
                resultado.put(rs.getString("mes"), rs.getInt("cantidad"));
            }
        } catch (SQLException e) {
            System.err.println("Error al obtener faltas por mes: " + e.getMessage());
        } finally {
            try {
                if (rs != null) rs.close();
                if (pstmt != null) pstmt.close();
            } catch (SQLException e) {
                System.err.println("Error al cerrar recursos: " + e.getMessage());
            }
            ConexionSQLite.cerrarConexion();
        }
        return resultado;
    }

    public Map<String, Integer> obtenerTop10Estudiantes(String fechaDesde, String fechaHasta) {
        Map<String, Integer> resultado = new LinkedHashMap<>();
        StringBuilder sql = new StringBuilder(
                "SELECT TRIM(COALESCE(e.nombre_1,'') || ' ' || COALESCE(e.apellido_1,'')) as nombre, COUNT(*) as cantidad " +
                "FROM faltas f INNER JOIN estudiantes e ON e.id = f.id_estudiante WHERE 1 = 1"
        );
        if (fechaDesde != null && !fechaDesde.isEmpty()) sql.append(" AND f.fecha >= ?");
        if (fechaHasta != null && !fechaHasta.isEmpty()) sql.append(" AND f.fecha <= ?");
        sql.append(" GROUP BY f.id_estudiante ORDER BY cantidad DESC LIMIT 10");

        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = ConexionSQLite.conectar();
            if (conn == null) return resultado;

            pstmt = conn.prepareStatement(sql.toString());
            int idx = 1;
            if (fechaDesde != null && !fechaDesde.isEmpty()) pstmt.setString(idx++, fechaDesde);
            if (fechaHasta != null && !fechaHasta.isEmpty()) pstmt.setString(idx++, fechaHasta);

            rs = pstmt.executeQuery();
            while (rs.next()) {
                resultado.put(rs.getString("nombre"), rs.getInt("cantidad"));
            }
        } catch (SQLException e) {
            System.err.println("Error al obtener top 10 estudiantes: " + e.getMessage());
        } finally {
            try {
                if (rs != null) rs.close();
                if (pstmt != null) pstmt.close();
            } catch (SQLException e) {
                System.err.println("Error al cerrar recursos: " + e.getMessage());
            }
            ConexionSQLite.cerrarConexion();
        }
        return resultado;
    }

    public Map<String, Integer> obtenerFaltasPorGenero(String fechaDesde, String fechaHasta) {
        Map<String, Integer> resultado = new LinkedHashMap<>();
        StringBuilder sql = new StringBuilder(
                "SELECT e.genero, COUNT(*) as cantidad FROM faltas f " +
                "INNER JOIN estudiantes e ON e.id = f.id_estudiante WHERE 1 = 1"
        );
        if (fechaDesde != null && !fechaDesde.isEmpty()) sql.append(" AND f.fecha >= ?");
        if (fechaHasta != null && !fechaHasta.isEmpty()) sql.append(" AND f.fecha <= ?");
        sql.append(" GROUP BY e.genero");

        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = ConexionSQLite.conectar();
            if (conn == null) return resultado;

            pstmt = conn.prepareStatement(sql.toString());
            int idx = 1;
            if (fechaDesde != null && !fechaDesde.isEmpty()) pstmt.setString(idx++, fechaDesde);
            if (fechaHasta != null && !fechaHasta.isEmpty()) pstmt.setString(idx++, fechaHasta);

            rs = pstmt.executeQuery();
            while (rs.next()) {
                resultado.put(rs.getString("genero"), rs.getInt("cantidad"));
            }
        } catch (SQLException e) {
            System.err.println("Error al obtener faltas por género: " + e.getMessage());
        } finally {
            try {
                if (rs != null) rs.close();
                if (pstmt != null) pstmt.close();
            } catch (SQLException e) {
                System.err.println("Error al cerrar recursos: " + e.getMessage());
            }
            ConexionSQLite.cerrarConexion();
        }
        return resultado;
    }

    public Map<String, Integer> obtenerTop10DocentesAula(String fechaDesde, String fechaHasta) {
        Map<String, Integer> resultado = new LinkedHashMap<>();
        StringBuilder sql = new StringBuilder(
                "SELECT TRIM(COALESCE(d.nombre_1,'') || ' ' || COALESCE(d.apellido_1,'')) as nombre, COUNT(*) as cantidad " +
                "FROM faltas f INNER JOIN docentes d ON d.id = f.id_docente " +
                "INNER JOIN lugares l ON l.id = f.id_lugar WHERE l.id = 1"
        );
        if (fechaDesde != null && !fechaDesde.isEmpty()) sql.append(" AND f.fecha >= ?");
        if (fechaHasta != null && !fechaHasta.isEmpty()) sql.append(" AND f.fecha <= ?");
        sql.append(" GROUP BY f.id_docente ORDER BY cantidad DESC LIMIT 10");

        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = ConexionSQLite.conectar();
            if (conn == null) return resultado;

            pstmt = conn.prepareStatement(sql.toString());
            int idx = 1;
            if (fechaDesde != null && !fechaDesde.isEmpty()) pstmt.setString(idx++, fechaDesde);
            if (fechaHasta != null && !fechaHasta.isEmpty()) pstmt.setString(idx++, fechaHasta);

            rs = pstmt.executeQuery();
            while (rs.next()) {
                resultado.put(rs.getString("nombre"), rs.getInt("cantidad"));
            }
        } catch (SQLException e) {
            System.err.println("Error al obtener top docentes aula: " + e.getMessage());
        } finally {
            try {
                if (rs != null) rs.close();
                if (pstmt != null) pstmt.close();
            } catch (SQLException e) {
                System.err.println("Error al cerrar recursos: " + e.getMessage());
            }
            ConexionSQLite.cerrarConexion();
        }
        return resultado;
    }
}

