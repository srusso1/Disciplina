package utils;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public final class AppVersion {

    private static final String VERSION_FALLBACK = "1.0.0";

    private AppVersion() {
    }

    public static String obtenerVersionActual() {
        Properties properties = new Properties();

        try (InputStream inputStream = AppVersion.class.getResourceAsStream("/app.properties")) {
            if (inputStream == null) {
                return VERSION_FALLBACK;
            }
            properties.load(inputStream);
            return properties.getProperty("app.version", VERSION_FALLBACK).trim();
        } catch (IOException e) {
            return VERSION_FALLBACK;
        }
    }
}

