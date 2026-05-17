package utils.updates;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

public class UpdateConfig {

    private final boolean updatesEnabled;
    private final String manifestUri;

    private UpdateConfig(boolean updatesEnabled, String manifestUri) {
        this.updatesEnabled = updatesEnabled;
        this.manifestUri = manifestUri;
    }

    public boolean isUpdatesEnabled() {
        return updatesEnabled;
    }

    public String getManifestUri() {
        return manifestUri;
    }

    public static UpdateConfig cargar() {
        Properties properties = new Properties();

        // Carga primero los defaults embebidos en la app.
        try (InputStream in = UpdateConfig.class.getResourceAsStream("/updates/updates.properties")) {
            if (in != null) {
                properties.load(in);
                normalizarBOM(properties);
            }
        } catch (IOException ignored) {
        }

        // Permite override local por equipo en %LOCALAPPDATA%\Disciplina\config.
        String localAppData = System.getenv("LOCALAPPDATA");
        if (localAppData != null && !localAppData.isBlank()) {
            Path configPath = Paths.get(localAppData, "Disciplina", "config", "updates.properties");
            if (Files.exists(configPath)) {
                try (InputStream in = Files.newInputStream(configPath)) {
                    Properties override = new Properties();
                    override.load(in);
                    normalizarBOM(override);
                    properties.putAll(override);
                } catch (IOException ignored) {
                }
            }
        }

        boolean enabled = Boolean.parseBoolean(properties.getProperty("updates.enabled", "false"));
        String manifest = properties.getProperty("updates.manifest.uri", "").trim();

        return new UpdateConfig(enabled, manifest);
    }

    private static void normalizarBOM(Properties properties) {
        Object valorConBOM = properties.remove("\uFEFFupdates.enabled");
        if (valorConBOM != null && properties.getProperty("updates.enabled") == null) {
            properties.setProperty("updates.enabled", String.valueOf(valorConBOM));
        }

        Object manifestConBOM = properties.remove("\uFEFFupdates.manifest.uri");
        if (manifestConBOM != null && properties.getProperty("updates.manifest.uri") == null) {
            properties.setProperty("updates.manifest.uri", String.valueOf(manifestConBOM));
        }
    }
}

