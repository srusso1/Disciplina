package utils;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class Fechas {
    private static final DateTimeFormatter FORMATO_UI = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter FORMATO_UI_GUION = DateTimeFormatter.ofPattern("dd-MM-yyyy");
    private static final DateTimeFormatter FORMATO_ISO = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    // Devuelve fecha actual en formato dd/MM/yyyy para UI
    public static String fechaActual(){
        return LocalDate.now().format(FORMATO_UI);
    }

    // Devuelve fecha actual en formato YYYY-MM-DD para BD
    public static String fechaActualISO(){
        return LocalDate.now().format(FORMATO_ISO);
    }

    // Convierte LocalDate a formato YYYY-MM-DD para persistencia
    public static String convertirAISO(LocalDate fecha) {
        if (fecha == null) {
            return null;
        }
        return fecha.format(FORMATO_ISO);
    }

    // Convierte de dd/MM/yyyy a YYYY-MM-DD (para almacenamiento)
    public static String convertirAISO(String fechaUI) {
        try {
            LocalDate date = LocalDate.parse(fechaUI, FORMATO_UI);
            return date.format(FORMATO_ISO);
        } catch (Exception e) {
            return null;
        }
    }

    // Convierte de YYYY-MM-DD a dd/MM/yyyy (para presentación)
    public static String convertirAUI(String fechaISO) {
        try {
            LocalDate date = LocalDate.parse(fechaISO, FORMATO_ISO);
            return date.format(FORMATO_UI);
        } catch (Exception e) {
            return null;
        }
    }

    public static int compararFechas(String fecha1, String fecha2) {
        LocalDate f1 = parseLocalDate(fecha1);
        LocalDate f2 = parseLocalDate(fecha2);

        if (f1 == null || f2 == null) {
            return 0;
        }

        return f1.compareTo(f2); // -1 fecha1 es ANTES que fecha2, 0 son iguales, 1 fecha2 es ANTES que fecha1
    }

    // Método privado para parsear fechas en ambos formatos
    private static LocalDate parseLocalDate(String fecha) {
        try {
            return LocalDate.parse(fecha, FORMATO_UI);
        } catch (Exception e) {
            try {
                return LocalDate.parse(fecha, FORMATO_UI_GUION);
            } catch (Exception ex) {
                try {
                    return LocalDate.parse(fecha, FORMATO_ISO);
                } catch (Exception ignored) {
                    return null;
                }
            }
        }
    }

    public static boolean esDespues(String f1, String f2) {
        return compararFechas(f1, f2) > 0;
    }

    public static boolean esAntes(String f1, String f2) {
        return compararFechas(f1, f2) < 0;
    }

}

