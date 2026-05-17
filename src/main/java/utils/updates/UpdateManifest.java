package utils.updates;

public class UpdateManifest {

    private String channel;
    private String publishedAt;
    private String latestVersion;
    private String minSupportedVersion;
    private String artifactPath;
    private String sha256;
    private String notes;

    public String getChannel() {
        return channel;
    }

    public String getPublishedAt() {
        return publishedAt;
    }

    public String getLatestVersion() {
        return latestVersion;
    }

    public String getMinSupportedVersion() {
        return minSupportedVersion;
    }

    public String getArtifactPath() {
        return artifactPath;
    }

    public String getSha256() {
        return sha256;
    }

    public String getNotes() {
        return notes;
    }
}

