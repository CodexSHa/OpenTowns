package xaos.utils;

import java.util.ArrayList;
import java.util.List;

import xaos.main.World;
import xaos.tiles.Cell;
import xaos.tiles.entities.items.Item;
import xaos.tiles.entities.items.ItemManager;
import xaos.tiles.entities.items.ItemManagerItem;
import xaos.tiles.terrain.Terrain;

public class LightingManager {

    private static final List<LightSource> lightSources = new ArrayList<LightSource>();

    public static void clear() {
        synchronized (lightSources) {
            lightSources.clear();
        }
    }

    public static void addLightSource(LightSource light) {
        if (light != null) {
            synchronized (lightSources) {
                lightSources.add(light);
            }
        }
    }

    public static List<LightSource> getLightSources() {
        return lightSources;
    }

    /**
     * Updates dynamic light sources in visible view range
     */
    public static void updateLightSources(int zView, int cellXMin, int cellXMax, int cellYMin, int cellYMax) {
        synchronized (lightSources) {
            lightSources.clear();
            if (World.getCells() == null) {
                return;
            }

            int minX = Math.max(0, cellXMin - 4);
            int maxX = Math.min(World.MAP_WIDTH - 1, cellXMax + 4);
            int minY = Math.max(0, cellYMin - 4);
            int maxY = Math.min(World.MAP_HEIGHT - 1, cellYMax + 4);

            for (int x = minX; x <= maxX; x++) {
                for (int y = minY; y <= maxY; y++) {
                    Cell cell = World.getCell(x, y, zView);
                    if (cell != null) {
                        // Lava light source
                        if (cell.getTerrain().hasFluids() && cell.getTerrain().getFluidType() == Terrain.FLUIDS_WATER == false) {
                            lightSources.add(new LightSource(x, y, zView, 6.0f, 1.0f, 0.45f, 0.1f, 0.85f));
                        }

                        Item item = cell.getItem();
                        if (item != null && item.isOperative()) {
                            ItemManagerItem imi = ItemManager.getItem(item.getIniHeader());
                            if (imi != null && imi.getLightRadius() > 0) {
                                lightSources.add(new LightSource(x, y, zView, Math.max(5.0f, (float) imi.getLightRadius()), 1.0f, 0.80f, 0.35f, 1.0f));
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * Calculates blended radial lighting RGB for a target cell (x, y, z) with torch flicker
     */
    public static float[] calculateRadialLight(int x, int y, int z, float baseShadowLight) {
        float[] color = new float[] { baseShadowLight, baseShadowLight, baseShadowLight };
        synchronized (lightSources) {
            long timeMs = System.currentTimeMillis();
            for (int i = 0; i < lightSources.size(); i++) {
                LightSource ls = lightSources.get(i);
                if (ls.getZ() == z) {
                    float dx = x - ls.getX();
                    float dy = y - ls.getY();
                    float distSq = dx * dx + dy * dy;
                    float radiusSq = ls.getRadius() * ls.getRadius();
                    if (distSq <= radiusSq) {
                        float dist = (float) Math.sqrt(distSq);
                        // Subtle organic flame flicker
                        float flicker = 0.94f + (float) (Math.sin(timeMs * 0.007 + ls.getX() * 1.5 + ls.getY()) * 0.06);
                        float factor = (1.0f - (dist / ls.getRadius())) * ls.getIntensity() * flicker;
                        color[0] += ls.getR() * factor * 0.45f;
                        color[1] += ls.getG() * factor * 0.45f;
                        color[2] += ls.getB() * factor * 0.45f;
                    }
                }
            }
        }
        color[0] = Math.min(1.0f, color[0]);
        color[1] = Math.min(1.0f, color[1]);
        color[2] = Math.min(1.0f, color[2]);
        return color;
    }

    /**
     * Returns circadian ambient color multiplier based on current day hour [0.0 - 24.0]
     */
    public static float[] getCircadianAmbient(float hour) {
        // [0..5] Night
        if (hour < 5.0f || hour >= 21.0f) {
            return new float[] { 0.50f, 0.58f, 0.78f }; // Moonlit indigo
        }
        // [5..8] Sunrise
        else if (hour < 8.0f) {
            float t = (hour - 5.0f) / 3.0f;
            return new float[] {
                0.50f + t * 0.55f, // 0.50 -> 1.05
                0.58f + t * 0.38f, // 0.58 -> 0.96
                0.78f + t * 0.12f  // 0.78 -> 0.90
            };
        }
        // [8..17] Daylight
        else if (hour < 17.0f) {
            return new float[] { 1.0f, 1.0f, 1.0f }; // Full daylight
        }
        // [17..21] Sunset / Dusk
        else {
            float t = (hour - 17.0f) / 4.0f;
            return new float[] {
                1.0f - t * 0.50f,
                1.0f - t * 0.42f,
                1.0f - t * 0.22f
            };
        }
    }
}
