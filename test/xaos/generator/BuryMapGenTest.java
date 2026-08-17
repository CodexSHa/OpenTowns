package xaos.generator;

import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import xaos.main.Game;
import xaos.main.World;
import xaos.test.HeadlessRunner;
import xaos.tiles.Cell;
import xaos.utils.AStarQueue;
import xaos.utils.Utils;
import xaos.utils.UtilsFiles;

import static org.junit.jupiter.api.Assertions.assertTrue;

class BuryMapGenTest {

    private static final long SEED = 12345;
    private static Path userFolder;

    @BeforeAll
    static void bootWorldAndGenerateBury() throws Exception {
        if (!Files.exists(Path.of("towns.ini"))) {
            throw new IllegalStateException("Working directory must be src/ (run via gradlew test)");
        }
        userFolder = HeadlessRunner.newUserFolder();
        Game.initHeadless(userFolder.toString());
        AStarQueue.setSynchronousMode(true);
        Utils.setRandomSeed(SEED);

        // Pre-provision starter ruins in user bury folder
        UtilsFiles.provisionStarterBuryFiles(userFolder.resolve("bury").toFile());

        Game.startGame("c1", "normal");
        Game.setAllowBury(true);

        // Generate buried ruins into map cells
        Game.generateBury(World.getCells());
    }

    @AfterAll
    static void cleanup() {
        HeadlessRunner.deleteRecursively(userFolder);
    }

    @Test
    void undergroundRuinsGeneratedInWorld() {
        Cell[][][] cells = World.getCells();
        int buriedCellsCount = 0;
        int undergroundMinedCount = 0;

        for (int x = 0; x < World.MAP_WIDTH; x++) {
            for (int y = 0; y < World.MAP_HEIGHT; y++) {
                for (int z = World.MAP_NUM_LEVELS_OUTSIDE + 1; z < World.MAP_DEPTH - 1; z++) {
                    Cell cell = cells[x][y][z];
                    if (cell.isBury()) {
                        buriedCellsCount++;
                    }
                    if (cell.isMined()) {
                        undergroundMinedCount++;
                    }
                }
            }
        }

        assertTrue(buriedCellsCount > 0, "Map generation should have placed buried ruin cells underground");
        assertTrue(undergroundMinedCount > 0, "Underground ruin space should be excavated");
    }
}
