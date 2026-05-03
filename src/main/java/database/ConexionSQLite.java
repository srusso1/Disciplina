package database;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class ConexionSQLite {

    private static Connection conexion = null;

    /**
     * Resuelve la ruta de la DB según el entorno:
     *  - Producción (exe): %LOCALAPPDATA%\Disciplina\data\DisciplinaDB.db
     *  - Desarrollo (IDE): src/main/java/database/DisciplinaDB.db
     */
    private static String resolverRutaDB() {
        // En producción el JAR corre desde dist/app/, no existe src/
        File devPath = new File("src/main/java/database/DisciplinaDB.db");
        if (devPath.exists()) {
            return devPath.getAbsolutePath();
        }

        // Producción: AppData\Local\Disciplina\data\
        String localAppData = System.getenv("LOCALAPPDATA");
        if (localAppData == null || localAppData.isEmpty()) {
            // Fallback si por alguna razón LOCALAPPDATA no está (Linux/Mac en pruebas)
            localAppData = System.getProperty("user.home");
        }

        File dbDir = new File(localAppData, "Disciplina" + File.separator + "data");
        File dbFile = new File(dbDir, "DisciplinaDB.db");

        // Si no existe aún (el instalador no la copió), extraerla del JAR
        if (!dbFile.exists()) {
            try {
                dbDir.mkdirs();
                try (InputStream is = ConexionSQLite.class.getResourceAsStream("/database/DisciplinaDB.db")) {
                    if (is != null) {
                        Files.copy(is, dbFile.toPath());
                        System.out.println("Base de datos copiada a: " + dbFile.getAbsolutePath());
                    } else {
                        System.err.println("No se encontro DisciplinaDB.db en los recursos del JAR.");
                    }
                }
            } catch (IOException e) {
                System.err.println("Error al extraer la base de datos: " + e.getMessage());
            }
        }

        return dbFile.getAbsolutePath();
    }

    public static Connection conectar() {
        try {
            Class.forName("org.sqlite.JDBC");

            String metodoLlamador = "desconocido";
            StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
            if (stackTrace.length >= 3) {
                metodoLlamador = stackTrace[2].getMethodName();
            }

            String dbPath = resolverRutaDB();
            String url = "jdbc:sqlite:" + dbPath;
            conexion = DriverManager.getConnection(url);
            System.out.println("Conexion establecida [" + dbPath + "] - " + metodoLlamador);

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

                System.out.println("Conexion cerrada - " + metodoLlamador);
            }
        } catch (SQLException e) {
            System.err.println("Error al cerrar la conexion: " + e.getMessage());
        }
    }
}