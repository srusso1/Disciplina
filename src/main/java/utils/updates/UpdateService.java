package utils.updates;

import com.google.gson.Gson;
import javafx.application.Platform;
import utils.Alertas;
import utils.AppVersion;

import java.awt.Desktop;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.StandardCopyOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Locale;

public final class UpdateService {

    private static final Gson GSON = new Gson();

    private UpdateService() {
    }

    public static void notificarActualizacionSiExiste() {
        UpdateConfig config = UpdateConfig.cargar();
        if (!config.isUpdatesEnabled() || config.getManifestUri().isBlank()) {
            return;
        }

        UpdateManifest manifest = cargarManifest(config.getManifestUri());
        if (manifest == null || manifest.getLatestVersion() == null || manifest.getLatestVersion().isBlank()) {
            return;
        }

        String versionActual = AppVersion.obtenerVersionActual();
        if (compararVersiones(manifest.getLatestVersion(), versionActual) <= 0) {
            return;
        }

        String mensaje = "Hay una nueva versión de Disciplina disponible (" + manifest.getLatestVersion() + ").";
        if (manifest.getNotes() != null && !manifest.getNotes().isBlank()) {
            mensaje += "\n\n" + manifest.getNotes();
        }

        final String mensajeFinal = mensaje;
        Platform.runLater(() -> {
            if (Alertas.mostrarConfirmacion(mensajeFinal)) {
                abrirArtefacto(manifest);
            }
        });
    }

    public static void buscarActualizacionManual() {
        UpdateConfig config = UpdateConfig.cargar();
        if (!config.isUpdatesEnabled() || config.getManifestUri().isBlank()) {
            Alertas.mostrarInfo("Las actualizaciones automáticas están desactivadas o no configuradas en este equipo.");
            return;
        }

        UpdateManifest manifest = cargarManifest(config.getManifestUri());
        if (manifest == null || manifest.getLatestVersion() == null || manifest.getLatestVersion().isBlank()) {
            Alertas.mostrarError("No fue posible leer el manifiesto de actualizaciones.");
            return;
        }

        String versionActual = AppVersion.obtenerVersionActual();
        if (manifest.getMinSupportedVersion() != null
                && !manifest.getMinSupportedVersion().isBlank()
                && compararVersiones(versionActual, manifest.getMinSupportedVersion()) < 0) {
            Alertas.mostrarInfo("Tu versión actual (" + versionActual + ") está por debajo de la mínima recomendada ("
                    + manifest.getMinSupportedVersion() + "). Se recomienda actualizar pronto.");
        }

        if (compararVersiones(manifest.getLatestVersion(), versionActual) <= 0) {
            Alertas.mostrarInfo("Tu aplicación ya está actualizada (versión " + versionActual + ").");
            return;
        }

        String mensaje = "Hay una nueva versión de Disciplina disponible (" + manifest.getLatestVersion() + ").";
        if (manifest.getNotes() != null && !manifest.getNotes().isBlank()) {
            mensaje += "\n\n" + manifest.getNotes();
        }

        if (Alertas.mostrarConfirmacion(mensaje + "\n\n¿Deseas abrir el paquete de actualización ahora?")) {
            abrirArtefacto(manifest);
        }
    }

    private static UpdateManifest cargarManifest(String manifestUri) {
        try {
            String json;
            if (manifestUri.startsWith("http://") || manifestUri.startsWith("https://") || manifestUri.startsWith("file://")) {
                try (InputStream inputStream = URI.create(manifestUri).toURL().openStream()) {
                    json = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
                }
            } else {
                Path path = Paths.get(manifestUri);
                json = Files.readString(path);
            }
            return GSON.fromJson(json, UpdateManifest.class);
        } catch (Exception e) {
            return null;
        }
    }

    private static boolean abrirArtefacto(UpdateManifest manifest) {
        String artifactPath = manifest.getArtifactPath();
        String sha256Esperado = manifest.getSha256();

        if (artifactPath == null || artifactPath.isBlank()) {
            Alertas.mostrarError("La ruta del paquete de actualización no está configurada en el manifiesto.");
            return false;
        }

        if (sha256Esperado == null || sha256Esperado.isBlank()) {
            Alertas.mostrarError("El manifiesto no incluye el hash SHA-256 del paquete. Por seguridad no se abrirá la actualización.");
            return false;
        }

        if (!Desktop.isDesktopSupported()) {
            Alertas.mostrarError("Este equipo no permite abrir automáticamente el paquete de actualización.\nRuta configurada: " + artifactPath);
            return false;
        }

        try {
            Desktop desktop = Desktop.getDesktop();

            if (artifactPath.startsWith("http://") || artifactPath.startsWith("https://")) {
                Path instaladorTemporal = descargarArtefacto(artifactPath);
                if (instaladorTemporal == null) {
                    Alertas.mostrarError("No fue posible descargar el paquete de actualización desde:\n" + artifactPath);
                    return false;
                }

                if (!validarHashSha256(instaladorTemporal, sha256Esperado)) {
                    return false;
                }

                if (!desktop.isSupported(Desktop.Action.OPEN)) {
                    Alertas.mostrarError("No se puede abrir archivos automáticamente en este equipo.\nAbra manualmente: " + instaladorTemporal);
                    return false;
                }

                desktop.open(instaladorTemporal.toFile());
                return true;
            }

            Path archivo = artifactPath.startsWith("file://")
                    ? Paths.get(URI.create(artifactPath))
                    : Paths.get(artifactPath);

            if (!Files.exists(archivo)) {
                Alertas.mostrarError("No se encontró el paquete de actualización en la ruta configurada:\n" + archivo + "\n\nVerifica el campo 'artifactPath' del manifiesto.");
                return false;
            }

            if (!validarHashSha256(archivo, sha256Esperado)) {
                return false;
            }

            if (!desktop.isSupported(Desktop.Action.OPEN)) {
                Alertas.mostrarError("No se puede abrir archivos automáticamente en este equipo.\nAbra manualmente: " + archivo);
                return false;
            }

            desktop.open(archivo.toFile());
            return true;
        } catch (IllegalArgumentException e) {
            Alertas.mostrarError("La ruta del paquete de actualización es inválida:\n" + artifactPath);
        } catch (IOException | SecurityException | UnsupportedOperationException e) {
            Alertas.mostrarError("No fue posible abrir el paquete de actualización.\nDetalle: " + e.getMessage());
        }

        return false;
    }

    private static Path descargarArtefacto(String url) {
        try (InputStream inputStream = new BufferedInputStream(URI.create(url).toURL().openStream())) {
            Path tempFile = Files.createTempFile("disciplina-update-", ".exe");
            Files.copy(inputStream, tempFile, StandardCopyOption.REPLACE_EXISTING);
            tempFile.toFile().deleteOnExit();
            return tempFile;
        } catch (Exception e) {
            return null;
        }
    }

    private static boolean validarHashSha256(Path archivo, String esperado) {
        try {
            String actual = calcularSha256(archivo);
            String esperadoNormalizado = esperado.trim().toLowerCase(Locale.ROOT);

            if (!actual.equals(esperadoNormalizado)) {
                Alertas.mostrarError("El hash SHA-256 del paquete no coincide.\nEsperado: " + esperadoNormalizado
                        + "\nActual: " + actual + "\n\nLa actualización se canceló por seguridad.");
                return false;
            }

            return true;
        } catch (Exception e) {
            Alertas.mostrarError("No fue posible validar la integridad del paquete de actualización.\nDetalle: " + e.getMessage());
            return false;
        }
    }

    private static String calcularSha256(Path archivo) throws IOException, NoSuchAlgorithmException {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");

        try (InputStream inputStream = Files.newInputStream(archivo)) {
            byte[] buffer = new byte[8192];
            int leidos;
            while ((leidos = inputStream.read(buffer)) != -1) {
                digest.update(buffer, 0, leidos);
            }
        }

        byte[] hash = digest.digest();
        StringBuilder sb = new StringBuilder(hash.length * 2);
        for (byte b : hash) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }

    static int compararVersiones(String versionA, String versionB) {
        String[] a = normalizarVersion(versionA).split("\\.");
        String[] b = normalizarVersion(versionB).split("\\.");
        int max = Math.max(a.length, b.length);

        for (int i = 0; i < max; i++) {
            int n1 = i < a.length ? parseSegment(a[i]) : 0;
            int n2 = i < b.length ? parseSegment(b[i]) : 0;
            if (n1 != n2) {
                return Integer.compare(n1, n2);
            }
        }
        return 0;
    }

    private static String normalizarVersion(String version) {
        String limpia = version == null ? "0" : version.trim().toLowerCase();
        if (limpia.endsWith("-snapshot")) {
            limpia = limpia.substring(0, limpia.length() - "-snapshot".length());
        }
        if (limpia.isBlank()) {
            return "0";
        }
        return limpia;
    }

    private static int parseSegment(String segment) {
        try {
            return Integer.parseInt(segment);
        } catch (NumberFormatException e) {
            return 0;
        }
    }
}

