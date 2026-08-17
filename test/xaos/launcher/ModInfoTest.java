package xaos.launcher;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ModInfoTest {

    @TempDir
    Path tmp;

    @Test
    void loadFromValidPropertiesFile() throws IOException {
        Path modDir = tmp.resolve("MyCoolMod");
        Files.createDirectories(modDir);
        Files.writeString(modDir.resolve("mod.properties"),
                "name = Super Farming\n" +
                "version = 1.2.3\n" +
                "author = Codex\n" +
                "description = Adds super crops and high-yield farming tools.\n");

        ModInfo info = ModInfo.load(modDir, "MyCoolMod");
        assertEquals("MyCoolMod", info.getFolderName());
        assertEquals("Super Farming", info.getName());
        assertEquals("1.2.3", info.getVersion());
        assertEquals("Codex", info.getAuthor());
        assertEquals("Adds super crops and high-yield farming tools.", info.getDescription());
        assertEquals("v1.2.3 by Codex", info.getBadge());
        assertTrue(info.existsOnDisk());
    }

    @Test
    void fallbackWhenPropertiesFileOmitted() throws IOException {
        Path modDir = tmp.resolve("PlainFolderMod");
        Files.createDirectories(modDir);

        ModInfo info = ModInfo.load(modDir, "PlainFolderMod");
        assertEquals("PlainFolderMod", info.getFolderName());
        assertEquals("PlainFolderMod", info.getName());
        assertEquals("", info.getVersion());
        assertEquals("", info.getAuthor());
        assertEquals("", info.getDescription());
        assertEquals("", info.getBadge());
        assertTrue(info.existsOnDisk());
    }

    @Test
    void missingDirectoryOnDisk() {
        Path nonExistent = tmp.resolve("NonExistentMod");

        ModInfo info = ModInfo.load(nonExistent, "NonExistentMod");
        assertEquals("NonExistentMod", info.getFolderName());
        assertEquals("NonExistentMod", info.getName());
        assertFalse(info.existsOnDisk());
    }

    @Test
    void badgeFormattingVariants() {
        ModInfo versionOnly = new ModInfo("mod", "Mod", "2.0", "", "", true);
        assertEquals("v2.0", versionOnly.getBadge());

        ModInfo alreadyVPrefixed = new ModInfo("mod", "Mod", "v3.1", "", "", true);
        assertEquals("v3.1", alreadyVPrefixed.getBadge());

        ModInfo authorOnly = new ModInfo("mod", "Mod", "", "Alice", "", true);
        assertEquals("by Alice", authorOnly.getBadge());

        ModInfo both = new ModInfo("mod", "Mod", "1.0", "Bob", "", true);
        assertEquals("v1.0 by Bob", both.getBadge());
    }
}
