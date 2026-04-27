package database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class ConexionSQLite {

    private static final String DB_PATH = "src/main/java/database/DisciplinaDB.db";
    private static final String URL = "jdbc:sqlite:" + DB_PATH;
    private static Connection conexion = null;

    public static Connection conectar(){
        try {
            // Cargar explícitamente el driver para evitar "No suitable driver found"
            Class.forName("org.sqlite.JDBC");
            
            String metodoLlamador = "desconocido";
            StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
            if (stackTrace.length >= 3) {
                metodoLlamador = stackTrace[2].getMethodName();
            }
            conexion = DriverManager.getConnection(URL);
            System.out.println("Conexion establecida con la base de datos - " + metodoLlamador);
        } catch (ClassNotFoundException e) {
            System.err.println("Driver SQLite no encontrado: " + e.getMessage());
        } catch (SQLException e) {
            System.err.println("Error al conectar a la base de datos: " + e.getMessage());
        }
        return conexion;
    }

    public static void cerrarConexion() {
        try {
            if (conexion != null && !conexion.isClosed()) {
                conexion.close();
                String metodoLlamador = "desconocido";
                StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
                if (stackTrace.length >= 3) {
                    metodoLlamador = stackTrace[2].getMethodName();
                }

                System.out.println("Se cerro la conexion a la base de datos - " + metodoLlamador);
            }
        } catch (SQLException e) {
            System.err.println("Error al cerrar la conexion a la base de datos: " + e.getMessage());
        }
    }
}
