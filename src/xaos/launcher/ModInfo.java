package xaos.launcher;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;
import java.util.Properties;

/**
 * Metadata for a discovered mod.
 *
 * Mods may optionally supply a {@code mod.properties} file in their root
 * folder with properties such as:
 * <pre>
 * name = My Custom Mod
 * version = 1.0.0
 * author = ModderName
 * description = A short description of what this mod does.
 * </pre>
 *
 * If {@code mod.properties} is omitted, the directory name is used as the
 * fallback display name.
 */
public final class ModInfo {

    private final String folderName;
    private final String name;
    private final String version;
    private final String author;
    private final String description;
    private final boolean existsOnDisk;

    public ModInfo(String folderName, String name, String version, String author, String description, boolean existsOnDisk) {
        this.folderName = Objects.requireNonNull(folderName, "folderName");
        this.name = (name == null || name.trim().isEmpty()) ? folderName : name.trim();
        this.version = version == null ? "" : version.trim();
        this.author = author == null ? "" : author.trim();
        this.description = description == null ? "" : description.trim();
        this.existsOnDisk = existsOnDisk;
    }

    public String getFolderName() {
        return folderName;
    }

    public String getName() {
        return name;
    }

    public String getVersion() {
        return version;
    }

    public String getAuthor() {
        return author;
    }

    public String getDescription() {
        return description;
    }

    public boolean existsOnDisk() {
        return existsOnDisk;
    }

    /**
     * Compact label summarizing version and author (e.g. "v1.2.0 by Codex" or "v1.0").
     */
    public String getBadge() {
        StringBuilder sb = new StringBuilder();
        if (!version.isEmpty()) {
            if (!version.toLowerCase().startsWith("v")) {
                sb.append("v");
            }
            sb.append(version);
        }
        if (!author.isEmpty()) {
            if (sb.length() > 0) {
                sb.append(" by ");
            } else {
                sb.append("by ");
            }
            sb.append(author);
        }
        return sb.toString();
    }

    /**
     * Reads metadata from the specified mod directory, falling back to folder name defaults.
     */
    public static ModInfo load(Path modFolder, String folderName) {
        if (modFolder == null || !Files.isDirectory(modFolder)) {
            return new ModInfo(folderName, folderName, "", "", "", false);
        }

        Path propsFile = modFolder.resolve("mod.properties");
        if (Files.isRegularFile(propsFile)) {
            Properties props = new Properties();
            try (InputStream in = Files.newInputStream(propsFile);
                 InputStreamReader reader = new InputStreamReader(in, StandardCharsets.UTF_8)) {
                props.load(reader);
                String name = props.getProperty("name", folderName);
                String version = props.getProperty("version", "");
                String author = props.getProperty("author", "");
                String desc = props.getProperty("description", "");
                return new ModInfo(folderName, name, version, author, desc, true);
            } catch (IOException e) {
                // Return fallback on read failure
            }
        }

        return new ModInfo(folderName, folderName, "", "", "", true);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ModInfo modInfo = (ModInfo) o;
        return existsOnDisk == modInfo.existsOnDisk &&
                folderName.equals(modInfo.folderName) &&
                name.equals(modInfo.name) &&
                version.equals(modInfo.version) &&
                author.equals(modInfo.author) &&
                description.equals(modInfo.description);
    }

    @Override
    public int hashCode() {
        return Objects.hash(folderName, name, version, author, description, existsOnDisk);
    }

    @Override
    public String toString() {
        return "ModInfo{" +
                "folderName='" + folderName + '\'' +
                ", name='" + name + '\'' +
                ", version='" + version + '\'' +
                ", author='" + author + '\'' +
                ", existsOnDisk=" + existsOnDisk +
                '}';
    }
}
