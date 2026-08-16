package xaos.panels;

import java.nio.ByteBuffer;
import java.util.ArrayList;

import org.lwjgl.opengl.GL11;

import xaos.main.Game;
import xaos.main.World;
import xaos.tiles.Cell;
import xaos.tiles.Tile;
import xaos.tiles.entities.Entity;
import xaos.tiles.entities.living.LivingEntity;
import xaos.tiles.terrain.Terrain;
import xaos.tiles.terrain.TerrainManager;
import xaos.utils.ColorGL;
import xaos.utils.ImageData;
import xaos.utils.Point3D;
import xaos.utils.Point3DShort;
import xaos.utils.TextureData;
import xaos.utils.UtilFont;
import xaos.utils.UtilsGL;

public final class MiniMapPanel {

    private static TextureData[] minimapTextures;
    private static final Tile YELLOW_TILE = new Tile("ui_yellow"); //$NON-NLS-1$

    public static int renderX;
    public static int renderY;
    public static int renderWidth;
    public static int renderHeight;

    private static boolean[] texturesReload;
    private static int textureRefreshRate;

    public static void initialize(int x, int y, int width, int height) {
        renderX = x;
        renderY = y;
        renderWidth = width;
        renderHeight = height;
        textureRefreshRate = Game.FPS_INGAME;

        if (minimapTextures != null) {
            for (TextureData textureData : minimapTextures) {
                UtilsGL.deleteTexture(textureData);
            }
        }
        minimapTextures = new TextureData[World.MAP_DEPTH];
        texturesReload = null;
    }

    public static void render() {
        if (texturesReload == null) {
            loadTextures();
        }

        Point3D pointView = Game.getWorld().getView();

        if (texturesReload[pointView.z] && textureRefreshRate == 0) {
            texturesReload[pointView.z] = false;
            reloadTexture(pointView.z);
        }

        textureRefreshRate--;
        if (textureRefreshRate < 0) {
            textureRefreshRate = Game.FPS_INGAME;
        }

        // 1. Medieval backing frame
        UtilsGL.drawMedievalBox(renderX - 6, renderY - 6, renderX + renderWidth + 6, renderY + renderHeight + 6);

        // 2. Diamond terrain map texture
        GL11.glEnable(GL11.GL_TEXTURE_2D);
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GL11.glColor4f(1.0f, 1.0f, 1.0f, 1.0f);
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, minimapTextures[pointView.z].getTextureID());
        GL11.glTexEnvf(GL11.GL_TEXTURE_ENV, GL11.GL_TEXTURE_ENV_MODE, GL11.GL_MODULATE);
        UtilsGL.glBegin(GL11.GL_QUADS);
        UtilsGL.drawTexture(renderX, renderY + renderHeight / 2,
                renderX + renderWidth / 2, renderY + renderHeight,
                renderX + renderWidth,    renderY + renderHeight / 2,
                renderX + renderWidth / 2, renderY,
                0, 0, 1, 1);
        UtilsGL.glEnd();

        // 3. Viewport tracking outline (gold box)
        int iSquareX = (pointView.x + pointView.y - (World.MAP_WIDTH - World.MAP_HEIGHT) / 2) / 2;
        int iSquareY = (pointView.y - pointView.x + (World.MAP_WIDTH + World.MAP_HEIGHT) / 2) / 2;
        int iSquareWidth  = ((MainPanel.renderWidth  / Tile.TERRAIN_ICON_WIDTH)  * renderWidth)  / World.MAP_WIDTH;
        int iSquareHeight = ((MainPanel.renderHeight / Tile.TERRAIN_ICON_HEIGHT) * renderHeight) / World.MAP_HEIGHT;
        iSquareX = (iSquareX * renderWidth)  / World.MAP_WIDTH  + renderX - iSquareWidth  / 2;
        iSquareY = (iSquareY * renderHeight) / World.MAP_HEIGHT + renderY - iSquareHeight / 2;

        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glColor4f(0.96f, 0.78f, 0.35f, 0.95f);
        GL11.glLineWidth(1.5f);
        GL11.glBegin(GL11.GL_LINE_LOOP);
        GL11.glVertex2f(iSquareX, iSquareY);
        GL11.glVertex2f(iSquareX + iSquareWidth, iSquareY);
        GL11.glVertex2f(iSquareX + iSquareWidth, iSquareY + iSquareHeight);
        GL11.glVertex2f(iSquareX, iSquareY + iSquareHeight);
        GL11.glEnd();
        GL11.glLineWidth(1.0f);

        // 4. Citizen dots (bright green indicators)
        GL11.glColor4f(0.20f, 0.95f, 0.40f, 1.0f);
        GL11.glBegin(GL11.GL_QUADS);
        ArrayList<Integer> citizens = World.getCitizenIDs();
        if (citizens != null) {
            for (int i = 0; i < citizens.size(); i++) {
                LivingEntity living = World.getLivingEntityByID(citizens.get(i));
                if (living != null && living.getZ() == pointView.z) {
                    int posX = (living.getX() + living.getY() - (World.MAP_WIDTH - World.MAP_HEIGHT) / 2) / 2;
                    int posY = (living.getY() - living.getX() + (World.MAP_WIDTH + World.MAP_HEIGHT) / 2) / 2;
                    posX = (posX * renderWidth)  / World.MAP_WIDTH  + renderX;
                    posY = (posY * renderHeight) / World.MAP_HEIGHT + renderY;
                    GL11.glVertex2f(posX - 1, posY - 1);
                    GL11.glVertex2f(posX + 2, posY - 1);
                    GL11.glVertex2f(posX + 2, posY + 2);
                    GL11.glVertex2f(posX - 1, posY + 2);
                }
            }
        }
        GL11.glEnd();

        // 5. Header title above minimap
        GL11.glEnable(GL11.GL_TEXTURE_2D);
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, Game.TEXTURE_FONT_ID);
        GL11.glTexEnvf(GL11.GL_TEXTURE_ENV, GL11.GL_TEXTURE_ENV_MODE, GL11.GL_MODULATE);
        UtilsGL.glBegin(GL11.GL_QUADS);
        UtilsGL.drawStringWithBorder("Realm Map", renderX + 4, renderY - 14,
                new ColorGL(0.96f, 0.78f, 0.35f), ColorGL.BLACK);
        UtilsGL.glEnd();

        GL11.glColor4f(1.0f, 1.0f, 1.0f, 1.0f);
        GL11.glDisable(GL11.GL_BLEND);
    }

    private static void loadTextures() {
        texturesReload = new boolean[World.MAP_DEPTH];
        for (int i = 0; i < World.MAP_DEPTH; i++) {
            texturesReload[i] = false;
            reloadTexture(i);
        }
    }

    private static void reloadTexture(int level) {
        Cell cell;
        Entity entity;
        ColorGL color;

        boolean newTexture = false;
        ImageData imageData = minimapTextures[level];
        if (imageData == null) {
            ByteBuffer buffer = ByteBuffer.allocateDirect(3 * World.MAP_WIDTH * World.MAP_HEIGHT);
            imageData = new ImageData(World.MAP_WIDTH, World.MAP_HEIGHT, buffer, GL11.GL_RGB);
            newTexture = true;
        }

        final ByteBuffer buffer = imageData.getImagePixels();
        buffer.rewind();
        for (int y = 0; y < World.MAP_HEIGHT; y++) {
            for (int x = 0; x < World.MAP_WIDTH; x++) {
                cell = World.getCell(x, y, level);
                entity = cell.getEntity();
                if (entity != null && entity.getColorMiniMap() != null) {
                    color = entity.getColorMiniMap();
                } else {
                    color = getCellColor(cell);
                }

                if (color == null) {
                    buffer.put((byte) 0);
                    buffer.put((byte) 0);
                    buffer.put((byte) 0);
                } else {
                    buffer.put((byte) (color.r * 255));
                    buffer.put((byte) (color.g * 255));
                    buffer.put((byte) (color.b * 255));
                }
            }
        }
        buffer.rewind();
        buffer.limit(buffer.capacity());

        if (newTexture) {
            minimapTextures[level] = UtilsGL.loadTexture(imageData, GL11.GL_REPLACE);
        } else {
            UtilsGL.reloadTexture(minimapTextures[level]);
        }
    }

    public static void setMinimapReload(int level) {
        if (texturesReload != null && level < texturesReload.length) {
            for (int i = 0; i <= level; i++) {
                texturesReload[i] = true;
            }
        }
    }

    private static ColorGL getCellColor(Cell cell) {
        if (cell.isDiscovered()) {
            if (cell.getTerrain().hasFluids()) {
                if (cell.getTerrain().getFluidType() == Terrain.FLUIDS_WATER) {
                    return World.getTileWater(Terrain.FLUIDS_COUNT_MAX).getColorMiniMap();
                } else {
                    return World.getTileLava(Terrain.FLUIDS_COUNT_MAX).getColorMiniMap();
                }
            } else {
                if (cell.isMined()) {
                    Point3DShort p3d = cell.getCoordinates();
                    if (p3d.z < (World.MAP_DEPTH - 1)) {
                        Cell cellUnder;
                        float fColor = 256f - 24f;
                        for (int i = p3d.z + 1; i < World.MAP_DEPTH; i++) {
                            cellUnder = World.getCell(p3d.x, p3d.y, i);
                            if (!cellUnder.isMined() || cellUnder.getTerrain().hasFluids()) {
                                if (fColor < 56f) {
                                    fColor = 56f;
                                }
                                fColor /= 256f;
                                ColorGL color = getCellColor(cellUnder);
                                if (color != null) {
                                    color = new ColorGL(color.r * fColor, color.g * fColor, color.b * fColor);
                                }
                                return color;
                            }
                            fColor -= 8f;
                        }
                        return null;
                    } else {
                        return null;
                    }
                } else if (cell.hasStockPile()) {
                    return World.getTileStockpile().getColorMiniMap();
                } else {
                    return TerrainManager.getTileByTileID(cell.getTerrain().getTerrainTileID()).getColorMiniMap();
                }
            }
        } else {
            return World.getTileUnknown().getColorMiniMap();
        }
    }

    public static boolean isMouseOver(int x, int y) {
        int x200 = (x * World.MAP_WIDTH) / renderWidth;
        int y200 = (y * World.MAP_HEIGHT) / renderHeight;
        int mapX = (x200 - y200) + World.MAP_WIDTH / 2;
        int mapY = (x200 + y200) - World.MAP_HEIGHT / 2;

        return (mapX >= 0 && mapX < World.MAP_WIDTH && mapY >= 0 && mapY < World.MAP_HEIGHT);
    }

    public static void mousePressed(int x, int y, int mouseButton) {
        int x200 = (x * World.MAP_WIDTH) / renderWidth;
        int y200 = (y * World.MAP_HEIGHT) / renderHeight;
        int mapX = (x200 - y200) + World.MAP_WIDTH / 2;
        int mapY = (x200 + y200) - World.MAP_HEIGHT / 2;

        if (mapX >= 0 && mapX < World.MAP_WIDTH && mapY >= 0 && mapY < World.MAP_HEIGHT) {
            Game.getWorld().setView(mapX, mapY);
            textureRefreshRate = 0;
        }
    }

    public static void resize(int renderX, int renderY, int renderWidth, int renderHeight) {
        MiniMapPanel.renderX = renderX;
        MiniMapPanel.renderY = renderY;
        MiniMapPanel.renderWidth = renderWidth;
        MiniMapPanel.renderHeight = renderHeight;
    }

    public static void clear() {
        texturesReload = null;
    }
}
