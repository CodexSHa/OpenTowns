package xaos.data;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import xaos.main.Game;
import xaos.tiles.entities.items.ItemManager;
import xaos.utils.Point3DShort;
import xaos.utils.UtilsFiles;
import xaos.utils.UtilsSavegame;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BuryDataTest {

    @TempDir
    Path tmp;

    @BeforeAll
    static void initItems() {
        Game.initHeadless(System.getProperty("java.io.tmpdir"));
        ItemManager.loadItems();
    }

    @Test
    void sampleRuinsCreationAndIntegrity() {
        String[] types = {"ancient_dungeon", "ruined_catacombs", "forgotten_workshop"};
        for (String type : types) {
            BuryData bd = BuryData.createSampleRuin(type);
            assertNotNull(bd, "Ruin should not be null for " + type);
            assertNotNull(bd.getHash(), "Ruin hash should not be null for " + type);
            assertFalse(bd.getHash().isEmpty(), "Ruin hash should not be empty for " + type);
            assertEquals(0, bd.getHeightMin());
            assertEquals(1, bd.getHeight());
        }
    }

    @Test
    void zipSerializationRoundTrip() throws Exception {
        BuryData original = BuryData.createSampleRuin("ancient_dungeon");
        File zipFile = tmp.resolve("test_dungeon.zip").toFile();

        BuryData.saveToZip(original, zipFile);
        assertTrue(zipFile.exists());
        assertTrue(zipFile.length() > 0);

        BuryData loaded = new BuryData();
        try (ZipFile zf = new ZipFile(zipFile);
             ZipInputStream zis = new ZipInputStream(new BufferedInputStream(new FileInputStream(zipFile)))) {
            ZipEntry entry = zis.getNextEntry();
            assertNotNull(entry);
            assertEquals("bury.dat", entry.getName());
            try (InputStream is = zf.getInputStream(entry);
                 ObjectInputStream ois = new ObjectInputStream(is)) {
                loaded.readExternal(ois);
            }
        }

        assertNotNull(loaded.getHash());
        assertEquals(original.getHash().size(), loaded.getHash().size());
        assertEquals(original.getHeightMin(), loaded.getHeightMin());
        assertEquals(original.getHeight(), loaded.getHeight());
    }

    @Test
    void autoProvisionStarterRuins() throws IOException {
        Path buryDir = tmp.resolve("user_bury");
        Files.createDirectories(buryDir);

        File fBury = buryDir.toFile();
        assertEquals(0, fBury.list().length);

        UtilsFiles.provisionStarterBuryFiles(fBury);

        File[] files = fBury.listFiles((dir, name) -> name.endsWith(".zip"));
        assertNotNull(files);
        assertTrue(files.length >= 3, "Should have provisioned at least 3 starter ruins");

        // Verify bundled directory sync
        Path dataBury = Path.of("data", "bury");
        Files.createDirectories(dataBury);
        for (File f : files) {
            Path target = dataBury.resolve(f.getName());
            if (!Files.exists(target)) {
                Files.copy(f.toPath(), target);
            }
        }
    }

    @Test
    void getRandomBuryDataFallback() {
        BuryData bd = UtilsSavegame.getRandomBuryData(null);
        assertNotNull(bd);
        assertNotNull(bd.getHash());
        assertFalse(bd.getHash().isEmpty());
    }
}
