package xaos.launcher;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LauncherConfigModTest {

    @TempDir
    Path tmp;

    @Test
    void loadDiscoversInstalledModsAndPreservesConfiguredOrder() throws IOException {
        Path userFolder = tmp.resolve(".towns");
        Path modsFolder = userFolder.resolve("mods");
        Files.createDirectories(modsFolder.resolve("ModAlpha"));
        Files.createDirectories(modsFolder.resolve("ModBeta"));
        Files.createDirectories(modsFolder.resolve("ModGamma"));

        Files.writeString(modsFolder.resolve("ModAlpha").resolve("mod.properties"),
                "name = Alpha Mod\nversion = 1.0\nauthor = DevA\ndescription = First mod.\n");

        Files.writeString(userFolder.resolve("towns.ini"),
                "MODS = ModBeta,ModAlpha\n");

        System.setProperty("towns.home", tmp.toString());
        // Create base towns.ini
        Files.writeString(tmp.resolve("towns.ini"), "USER_FOLDER = " + tmp.toString().replace('\\', '/') + "\n");

        LauncherConfig config = LauncherConfig.load();

        assertEquals(List.of("ModBeta", "ModAlpha"), config.enabledMods);
        assertTrue(config.availableMods.contains("ModAlpha"));
        assertTrue(config.availableMods.contains("ModBeta"));
        assertTrue(config.availableMods.contains("ModGamma"));

        ModInfo alphaInfo = config.getModInfo("ModAlpha");
        assertEquals("Alpha Mod", alphaInfo.getName());
        assertEquals("1.0", alphaInfo.getVersion());
        assertEquals("DevA", alphaInfo.getAuthor());
        assertEquals("First mod.", alphaInfo.getDescription());

        ModInfo betaInfo = config.getModInfo("ModBeta");
        assertEquals("ModBeta", betaInfo.getName());
    }

    @Test
    void loadOrderReorderingAndSaving() throws IOException {
        Path userFolder = tmp.resolve(".towns");
        Path modsFolder = userFolder.resolve("mods");
        Files.createDirectories(modsFolder.resolve("Mod1"));
        Files.createDirectories(modsFolder.resolve("Mod2"));
        Files.createDirectories(modsFolder.resolve("Mod3"));

        LauncherConfig config = new LauncherConfig(userFolder);
        config.availableMods.addAll(List.of("Mod1", "Mod2", "Mod3"));

        config.setModEnabled("Mod1", true);
        config.setModEnabled("Mod2", true);
        config.setModEnabled("Mod3", true);
        assertEquals(List.of("Mod1", "Mod2", "Mod3"), config.enabledMods);

        // Boundary tests for Mod1 (top)
        assertFalse(config.canMoveUp("Mod1"));
        assertTrue(config.canMoveDown("Mod1"));

        // Boundary tests for Mod3 (bottom)
        assertTrue(config.canMoveUp("Mod3"));
        assertFalse(config.canMoveDown("Mod3"));

        // Move Mod2 up -> should become [Mod2, Mod1, Mod3]
        assertTrue(config.canMoveUp("Mod2"));
        config.moveModUp("Mod2");
        assertEquals(List.of("Mod2", "Mod1", "Mod3"), config.enabledMods);

        // Move Mod2 down -> back to [Mod1, Mod2, Mod3]
        config.moveModDown("Mod2");
        assertEquals(List.of("Mod1", "Mod2", "Mod3"), config.enabledMods);

        // Move Mod3 up twice -> [Mod3, Mod1, Mod2]
        config.moveModUp("Mod3");
        config.moveModUp("Mod3");
        assertEquals(List.of("Mod3", "Mod1", "Mod2"), config.enabledMods);

        // Save to towns.ini and verify exact MODS string
        config.save();

        String iniContent = Files.readString(userFolder.resolve("towns.ini"));
        assertTrue(iniContent.contains("MODS = Mod3,Mod1,Mod2"));
    }

    @Test
    void missingModKeptInAvailableListWhenConfigured() throws IOException {
        Path userFolder = tmp.resolve(".towns");
        Files.createDirectories(userFolder);
        Files.writeString(userFolder.resolve("towns.ini"), "MODS = MissingMod\n");

        System.setProperty("towns.home", tmp.toString());
        Files.writeString(tmp.resolve("towns.ini"), "USER_FOLDER = " + tmp.toString().replace('\\', '/') + "\n");

        LauncherConfig config = LauncherConfig.load();

        assertTrue(config.enabledMods.contains("MissingMod"));
        assertTrue(config.availableMods.contains("MissingMod"));
        assertFalse(config.modExistsOnDisk("MissingMod"));
    }
}
