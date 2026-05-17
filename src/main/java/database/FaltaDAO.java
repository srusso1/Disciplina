package database;

import database.models.Falta;
import database.models.FaltaConsultaRow;
import utils.Fechas;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
public class FaltaDAO {

    public boolean registrar(Falta falta) {
        String sql = "INSERT INTO faltas (id_estudiante, id_caso, id_lugar, id_docente, tipo_falta, descargo, accion_restaurativa, grado_estudiante, fecha) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
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
            pstmt.setInt(8, falta.getGradoEstudiante());
            pstmt.setString(9, Fechas.fechaActualISO());

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
                        "f.grado_estudiante, e.identificacion, f.tipo_falta, c.nombre_caso AS caso, l.nombre_lugar AS lugar, " +
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
                        rs.getInt("grado_estudiante"),
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
                "SELECT f.grado_estudiante, COUNT(*) as cantidad FROM faltas f " +
                "WHERE 1 = 1"
        );
        if (fechaDesde != null && !fechaDesde.isEmpty()) sql.append(" AND f.fecha >= ?");
        if (fechaHasta != null && !fechaHasta.isEmpty()) sql.append(" AND f.fecha <= ?");
        sql.append(" GROUP BY f.grado_estudiante ORDER BY f.grado_estudiante");

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
                resultado.put(rs.getInt("grado_estudiante"), rs.getInt("cantidad"));
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

    /**
     * Obtiene faltas por grado con cantidad total y estudiantes únicos afectados.
     * Retorna Map<Integer grado, Integer[] {cantidad, estudiantesUnicos}>
     */
    public Map<Integer, Integer[]> obtenerFaltasPorGradoConEstudiantes(String fechaDesde, String fechaHasta) {
        Map<Integer, Integer[]> resultado = new LinkedHashMap<>();

        StringBuilder sql = new StringBuilder(
                "SELECT f.grado_estudiante, COUNT(*) as cantidad, COUNT(DISTINCT f.id_estudiante) as estudiantes_unicos " +
                "FROM faltas f WHERE 1 = 1"
        );
        if (fechaDesde != null && !fechaDesde.isEmpty()) sql.append(" AND f.fecha >= ?");
        if (fechaHasta != null && !fechaHasta.isEmpty()) sql.append(" AND f.fecha <= ?");
        sql.append(" GROUP BY f.grado_estudiante ORDER BY f.grado_estudiante");

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
                int grado = rs.getInt("grado_estudiante");
                int cantidad = rs.getInt("cantidad");
                int estudiantesUnicos = rs.getInt("estudiantes_unicos");
                resultado.put(grado, new Integer[]{cantidad, estudiantesUnicos});
            }
        } catch (SQLException e) {
            System.err.println("Error al obtener faltas por grado con estudiantes: " + e.getMessage());
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

    /**
     * Obtiene distribuión de cases por grado específico.
     */
    public Map<String, Integer> obtenerFaltasPorCasoEnGrado(int grado, String fechaDesde, String fechaHasta) {
        Map<String, Integer> resultado = new LinkedHashMap<>();
        StringBuilder sql = new StringBuilder(
                "SELECT c.nombre_caso, COUNT(*) as cantidad FROM faltas f " +
                "INNER JOIN casos c ON c.id = f.id_caso " +
                "WHERE f.grado_estudiante = ?"
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
            pstmt.setInt(idx++, grado);
            if (fechaDesde != null && !fechaDesde.isEmpty()) pstmt.setString(idx++, fechaDesde);
            if (fechaHasta != null && !fechaHasta.isEmpty()) pstmt.setString(idx++, fechaHasta);

            rs = pstmt.executeQuery();
            while (rs.next()) {
                resultado.put(rs.getString("nombre_caso"), rs.getInt("cantidad"));
            }
        } catch (SQLException e) {
            System.err.println("Error al obtener faltas por caso en grado: " + e.getMessage());
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

    public List<Integer> obtenerAniosRegistrados() {
        List<Integer> anios = new ArrayList<>();
        // Obtener años de tabla faltas Y de tabla faltas_historico
        String sql = "SELECT DISTINCT anio FROM (" +
                     "  SELECT CAST(strftime('%Y', fecha) AS INTEGER) AS anio FROM faltas WHERE fecha IS NOT NULL " +
                     "  UNION " +
                     "  SELECT DISTINCT año AS anio FROM faltas_historico" +
                     ") combined ORDER BY anio ASC";

        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = ConexionSQLite.conectar();
            if (conn == null) return anios;

            pstmt = conn.prepareStatement(sql);
            rs = pstmt.executeQuery();

            while (rs.next()) {
                anios.add(rs.getInt("anio"));
            }
        } catch (SQLException e) {
            System.err.println("Error al obtener años registrados: " + e.getMessage());
        } finally {
            try {
                if (rs != null) rs.close();
                if (pstmt != null) pstmt.close();
            } catch (SQLException e) {
                System.err.println("Error al cerrar recursos de años registrados: " + e.getMessage());
            }
            ConexionSQLite.cerrarConexion();
        }

        return anios;
    }

    /**
     * Obtiene faltas con datos históricos incluidos.
     * Combina faltas actuales (tabla faltas) con datos históricos (tabla faltas_historico)
     * Retorna FaltaConsultaRow para mantener compatibilidad con comparativas
     */
    public List<FaltaConsultaRow> consultarFaltasConHistorico(Integer idCaso) {
        List<FaltaConsultaRow> filas = new ArrayList<>();

        // Consultar datos actuales de la tabla faltas
        List<FaltaConsultaRow> faltasActuales = consultarFaltas(null, null, null, null, idCaso, null);
        filas.addAll(faltasActuales);

        // Consultar datos históricos de la tabla faltas_historico
        String sql = "SELECT CAST(fh.año AS TEXT) || '-' || PRINTF('%02d', fh.mes) || '-01' AS fecha, " +
                     "fh.cantidad, c.nombre_caso, fh.id_caso " +
                     "FROM faltas_historico fh " +
                     "INNER JOIN casos c ON c.id = fh.id_caso " +
                     "WHERE fh.cantidad > 0";

        if (idCaso != null) {
            sql += " AND fh.id_caso = ?";
        }
        sql += " ORDER BY fh.año DESC, fh.mes ASC";

        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = ConexionSQLite.conectar();
            if (conn == null) return filas;

            pstmt = conn.prepareStatement(sql);
            if (idCaso != null) {
                pstmt.setInt(1, idCaso);
            }

            rs = pstmt.executeQuery();
            while (rs.next()) {
                String fechaISO = rs.getString("fecha");
                String fechaUI = Fechas.convertirAUI(fechaISO);
                if (fechaUI == null) {
                    fechaUI = fechaISO;
                }

                filas.add(new FaltaConsultaRow(
                        0,  // id (no aplica para históricos)
                        0,  // id_estudiante (no aplica)
                        0,  // id_lugar (no aplica)
                        fechaUI,
                        "Histórico - " + rs.getInt("cantidad") + " caso(s)",  // estudiante (descripción)
                        0,  // grado
                        "",  // identificacion
                        "1",  // tipo_falta
                        rs.getString("nombre_caso"),
                        "",  // lugar
                        "",  // docente
                        "",  // descargo
                        ""   // accion_restaurativa
                ));
            }
        } catch (SQLException e) {
            System.err.println("Error al consultar faltas con histórico: " + e.getMessage());
        } finally {
            try {
                if (rs != null) rs.close();
                if (pstmt != null) pstmt.close();
            } catch (SQLException e) {
                System.err.println("Error al cerrar recursos de consulta con histórico: " + e.getMessage());
            }
            ConexionSQLite.cerrarConexion();
        }

        return filas;
    }

    /**
     * Obtiene faltas para comparativas, combinando datos actuales (faltas) con históricos (faltas_historico)
     * Retorna datos en formato que permite comparativas mes-a-mes entre años
     */
    public List<FaltaConsultaRow> obtenerFaltasComparativa(Integer idCaso) {
        List<FaltaConsultaRow> filas = new ArrayList<>();

        String sql = "SELECT " +
                     "    f.id, " +
                     "    f.id_lugar, " +
                     "    f.fecha, " +
                     "    0 AS id_estudiante, " +
                     "    'Falta registrada' AS estudiante, " +
                     "    0 AS grado, " +
                     "    '' AS identificacion, " +
                     "    '1' AS tipo_falta, " +
                     "    c.nombre_caso AS caso, " +
                     "    l.nombre_lugar AS lugar, " +
                     "    '' AS docente, " +
                     "    '' AS descargo, " +
                     "    '' AS accion_restaurativa, " +
                     "    1 AS cantidad_falta " +
                     "FROM faltas f " +
                     "INNER JOIN casos c ON c.id = f.id_caso " +
                     "INNER JOIN lugares l ON l.id = f.id_lugar " +
                     "WHERE f.fecha IS NOT NULL " +
                     (idCaso != null ? "AND f.id_caso = ? " : "") +
                     "UNION ALL " +
                     "SELECT " +
                     "    fh.id, " +
                     "    0 AS id_lugar, " +
                     "    CAST(fh.año AS TEXT) || '-' || PRINTF('%02d', fh.mes) || '-01' AS fecha, " +
                     "    0 AS id_estudiante, " +
                     "    'Datos históricos' AS estudiante, " +
                     "    0 AS grado, " +
                     "    '' AS identificacion, " +
                     "    '1' AS tipo_falta, " +
                     "    c.nombre_caso AS caso, " +
                     "    '' AS lugar, " +
                     "    '' AS docente, " +
                     "    '' AS descargo, " +
                     "    '' AS accion_restaurativa, " +
                     "    fh.cantidad AS cantidad_falta " +
                     "FROM faltas_historico fh " +
                     "INNER JOIN casos c ON c.id = fh.id_caso " +
                     "WHERE fh.cantidad > 0 " +
                     (idCaso != null ? "AND fh.id_caso = ? " : "") +
                     "ORDER BY fecha DESC";

        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = ConexionSQLite.conectar();
            if (conn == null) return filas;

            pstmt = conn.prepareStatement(sql);

            int paramIndex = 1;
            if (idCaso != null) {
                pstmt.setInt(paramIndex++, idCaso);
                pstmt.setInt(paramIndex, idCaso);
            }

            rs = pstmt.executeQuery();

            while (rs.next()) {
                String fechaISO = rs.getString("fecha");
                String fechaUI = convertirFechaAlFormato(fechaISO);
                int cantidadFalta = rs.getInt("cantidad_falta");

                // Para la visualización, agregar información de cantidad
                String descripcionEstudiante = rs.getString("estudiante");
                if ("Datos históricos".equals(descripcionEstudiante)) {
                    descripcionEstudiante = "Histórico (" + cantidadFalta + ")";
                }

                filas.add(new FaltaConsultaRow(
                        rs.getInt("id"),
                        rs.getInt("id_estudiante"),
                        rs.getInt("id_lugar"),
                        fechaUI,
                        descripcionEstudiante,
                        rs.getInt("grado"),
                        rs.getString("identificacion"),
                        rs.getString("tipo_falta"),
                        rs.getString("caso"),
                        rs.getString("lugar"),
                        rs.getString("docente"),
                        rs.getString("descargo"),
                        rs.getString("accion_restaurativa")
                ));
            }
        } catch (SQLException e) {
            System.err.println("Error al obtener faltas para comparativa: " + e.getMessage());
        } finally {
            try {
                if (rs != null) rs.close();
                if (pstmt != null) pstmt.close();
            } catch (SQLException e) {
                System.err.println("Error al cerrar recursos de comparativa: " + e.getMessage());
            }
            ConexionSQLite.cerrarConexion();
        }

        return filas;
    }

    /**
     * Obtiene conteo por mes/anio para comparativas sin doble conteo.
     * Regla: si existe historico para una clave (id_caso, anio, mes), ese valor prevalece.
     */
    public Map<Integer, Map<Integer, Integer>> obtenerConteoPorMesYAnio(Integer idCaso) {
        Map<Integer, Map<Integer, Integer>> resultado = new HashMap<>();
        Map<String, Integer> conteoPorCasoMesAnio = new HashMap<>();
        Set<String> clavesConHistorico = new HashSet<>();

        String sqlHistorico = "SELECT fh.id_caso, fh.mes, fh.año AS anio, SUM(fh.cantidad) AS cantidad " +
                "FROM faltas_historico fh " +
                "WHERE fh.cantidad > 0 " +
                (idCaso != null ? "AND fh.id_caso = ? " : "") +
                "GROUP BY fh.id_caso, fh.año, fh.mes";

        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = ConexionSQLite.conectar();
            if (conn == null) return resultado;

            pstmt = conn.prepareStatement(sqlHistorico);
            if (idCaso != null) {
                pstmt.setInt(1, idCaso);
            }

            rs = pstmt.executeQuery();
            while (rs.next()) {
                int caso = rs.getInt("id_caso");
                int mes = rs.getInt("mes");
                int anio = rs.getInt("anio");
                int cantidad = rs.getInt("cantidad");

                String clave = caso + "-" + anio + "-" + mes;
                clavesConHistorico.add(clave);
                conteoPorCasoMesAnio.put(clave, cantidad);
            }
        } catch (SQLException e) {
            System.err.println("Error al obtener conteo historico: " + e.getMessage());
        } finally {
            try {
                if (rs != null) rs.close();
                if (pstmt != null) pstmt.close();
            } catch (SQLException e) {
                System.err.println("Error al cerrar recursos historico: " + e.getMessage());
            }
            ConexionSQLite.cerrarConexion();
        }

        String sqlFaltas = "SELECT f.id_caso, " +
                "CAST(strftime('%m', f.fecha) AS INTEGER) AS mes, " +
                "CAST(strftime('%Y', f.fecha) AS INTEGER) AS anio, " +
                "COUNT(*) AS cantidad " +
                "FROM faltas f " +
                "WHERE f.fecha IS NOT NULL " +
                (idCaso != null ? "AND f.id_caso = ? " : "") +
                "GROUP BY f.id_caso, CAST(strftime('%Y', f.fecha) AS INTEGER), CAST(strftime('%m', f.fecha) AS INTEGER)";

        try {
            conn = ConexionSQLite.conectar();
            if (conn == null) return resultado;

            pstmt = conn.prepareStatement(sqlFaltas);
            if (idCaso != null) {
                pstmt.setInt(1, idCaso);
            }

            rs = pstmt.executeQuery();
            while (rs.next()) {
                int caso = rs.getInt("id_caso");
                int mes = rs.getInt("mes");
                int anio = rs.getInt("anio");
                int cantidad = rs.getInt("cantidad");

                String clave = caso + "-" + anio + "-" + mes;
                if (clavesConHistorico.contains(clave)) {
                    continue;
                }

                conteoPorCasoMesAnio.merge(clave, cantidad, Integer::sum);
            }
        } catch (SQLException e) {
            System.err.println("Error al obtener conteo de faltas: " + e.getMessage());
        } finally {
            try {
                if (rs != null) rs.close();
                if (pstmt != null) pstmt.close();
            } catch (SQLException e) {
                System.err.println("Error al cerrar recursos de faltas: " + e.getMessage());
            }
            ConexionSQLite.cerrarConexion();
        }

        for (Map.Entry<String, Integer> entry : conteoPorCasoMesAnio.entrySet()) {
            String[] partes = entry.getKey().split("-");
            if (partes.length != 3) continue;

            int anio;
            int mes;
            try {
                anio = Integer.parseInt(partes[1]);
                mes = Integer.parseInt(partes[2]);
            } catch (NumberFormatException e) {
                continue;
            }

            resultado.putIfAbsent(mes, new HashMap<>());
            int actual = resultado.get(mes).getOrDefault(anio, 0);
            resultado.get(mes).put(anio, actual + entry.getValue());
        }

        return resultado;
    }

    /**
     * Convierte fecha en formato ISO (yyyy-MM-dd) a formato UI (dd/MM/yyyy)
     */
    private String convertirFechaAlFormato(String fechaISO) {
        if (fechaISO == null || fechaISO.isEmpty()) {
            return "";
        }
        try {
            String[] partes = fechaISO.split("-");
            if (partes.length >= 3) {
                return partes[2] + "/" + partes[1] + "/" + partes[0];
            }
        } catch (Exception e) {
            System.err.println("Error al convertir fecha: " + e.getMessage());
        }
        return fechaISO;
    }
}
