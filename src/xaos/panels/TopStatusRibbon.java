package xaos.panels;

import org.lwjgl.opengl.GL11;

import xaos.compat.input.Keyboard;
import xaos.main.Game;
import xaos.main.World;
import xaos.tiles.entities.living.Citizen;
import xaos.utils.ColorGL;
import xaos.utils.UtilFont;
import xaos.utils.UtilsAL;
import xaos.utils.UtilsGL;
import xaos.utils.UtilsKeyboard;

/**
 * Top status ribbon — displays colony metrics, elevation, game speed, and settings.
 * All text rendering is properly wrapped in OpenGL begin/end calls.
 */
public class TopStatusRibbon {

    private static int cachedWood  = 0;
    private static int cachedStone = 0;
    private static int cachedOre   = 0;
    private static int cachedFood  = 0;
    private static int refreshCounter = 0;

    private static final int RIBBON_X = 16;
    private static final int RIBBON_Y = 6;
    private static final int RIBBON_H = 34;

    public static boolean handleClick(int mouseX, int mouseY, int screenWidth) {
        int ribbonW = screenWidth - 32;

        if (mouseX < RIBBON_X || mouseX > RIBBON_X + ribbonW
                || mouseY < RIBBON_Y || mouseY > RIBBON_Y + RIBBON_H) {
            return false;
        }

        int rightControlsX = RIBBON_X + ribbonW - 275;
        int speedX         = rightControlsX + 60;
        int settingsX      = speedX + 118;

        // Level Down [v]
        if (hitBtn(mouseX, mouseY, rightControlsX, RIBBON_Y + 4, 22, 26)) {
            if (Game.getWorld() != null) {
                Game.getWorld().keyPressed(Keyboard.KEY_NONE, UtilsKeyboard.FN_LEVEL_DOWN);
                UtilsAL.play(UtilsAL.SOURCE_FX_CLICK, 0);
            }
            return true;
        }
        // Level Up [^]
        if (hitBtn(mouseX, mouseY, rightControlsX + 26, RIBBON_Y + 4, 22, 26)) {
            if (Game.getWorld() != null) {
                Game.getWorld().keyPressed(Keyboard.KEY_NONE, UtilsKeyboard.FN_LEVEL_UP);
                UtilsAL.play(UtilsAL.SOURCE_FX_CLICK, 0);
            }
            return true;
        }
        // Pause [||]
        if (hitBtn(mouseX, mouseY, speedX, RIBBON_Y + 4, 28, 26)) {
            if (Game.getWorld() != null) {
                Game.getWorld().keyPressed(Keyboard.KEY_NONE, UtilsKeyboard.FN_PAUSE);
                UtilsAL.play(UtilsAL.SOURCE_FX_CLICK, 0);
            }
            return true;
        }
        // 1x speed
        if (hitBtn(mouseX, mouseY, speedX + 32, RIBBON_Y + 4, 38, 26)) {
            if (Game.isPaused()) Game.setPaused(false);
            World.SPEED = 1;
            UtilsAL.play(UtilsAL.SOURCE_FX_CLICK, 0);
            return true;
        }
        // 3x speed
        if (hitBtn(mouseX, mouseY, speedX + 74, RIBBON_Y + 4, 38, 26)) {
            if (Game.isPaused()) Game.setPaused(false);
            World.SPEED = 3;
            UtilsAL.play(UtilsAL.SOURCE_FX_CLICK, 0);
            return true;
        }
        // Settings [Opt]
        if (hitBtn(mouseX, mouseY, settingsX, RIBBON_Y + 4, 36, 26)) {
            if (Game.getWorld() != null) {
                Game.getWorld().keyPressed(Keyboard.KEY_ESCAPE, UtilsKeyboard.FN_NONE);
                UtilsAL.play(UtilsAL.SOURCE_FX_CLICK, 0);
            }
            return true;
        }

        return true;
    }

    private static boolean hitBtn(int mx, int my, int x, int y, int w, int h) {
        return mx >= x && mx <= x + w && my >= y && my <= y + h;
    }

    public static void render(int mouseX, int mouseY, int screenWidth, int screenHeight) {
        int ribbonW = screenWidth - 32;

        refreshCounter++;
        if (refreshCounter >= 60) {
            refreshCounter = 0;
            cachedWood  = xaos.tiles.entities.items.Item.getNumItemsTotal("rmwood",   World.MAP_DEPTH - 1);
            cachedStone = xaos.tiles.entities.items.Item.getNumItemsTotal("rmstone",  World.MAP_DEPTH - 1);
            cachedOre   = xaos.tiles.entities.items.Item.getNumItemsTotal("rmiron",   World.MAP_DEPTH - 1)
                        + xaos.tiles.entities.items.Item.getNumItemsTotal("rmcopper", World.MAP_DEPTH - 1)
                        + xaos.tiles.entities.items.Item.getNumItemsTotal("rmcoal",   World.MAP_DEPTH - 1);
            cachedFood  = xaos.tiles.entities.items.Item.getNumItemsTotal("apple",       World.MAP_DEPTH - 1)
                        + xaos.tiles.entities.items.Item.getNumItemsTotal("bread",        World.MAP_DEPTH - 1)
                        + xaos.tiles.entities.items.Item.getNumItemsTotal("cookedfish",   World.MAP_DEPTH - 1)
                        + xaos.tiles.entities.items.Item.getNumItemsTotal("cookedsteak",  World.MAP_DEPTH - 1);
        }

        // 1. Ribbon background frame
        UtilsGL.drawMedievalBox(RIBBON_X, RIBBON_Y, RIBBON_X + ribbonW, RIBBON_Y + RIBBON_H);

        // 2. Control button backgrounds on the right
        int rightControlsX = RIBBON_X + ribbonW - 275;
        int speedX         = rightControlsX + 60;
        int settingsX      = speedX + 118;

        drawButtonBg(rightControlsX,      RIBBON_Y + 4, 22, 26, mouseX, mouseY);
        drawButtonBg(rightControlsX + 26, RIBBON_Y + 4, 22, 26, mouseX, mouseY);
        drawButtonBg(speedX,              RIBBON_Y + 4, 28, 26, mouseX, mouseY);
        drawButtonBg(speedX + 32,         RIBBON_Y + 4, 38, 26, mouseX, mouseY);
        drawButtonBg(speedX + 74,         RIBBON_Y + 4, 38, 26, mouseX, mouseY);
        drawButtonBg(settingsX,           RIBBON_Y + 4, 36, 26, mouseX, mouseY);

        // 3. Render all status text inside GL Begin/End
        GL11.glEnable(GL11.GL_TEXTURE_2D);
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, Game.TEXTURE_FONT_ID);
        GL11.glTexEnvf(GL11.GL_TEXTURE_ENV, GL11.GL_TEXTURE_ENV_MODE, GL11.GL_MODULATE);
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

        ColorGL cream  = new ColorGL(0.96f, 0.90f, 0.78f);
        ColorGL gold   = new ColorGL(0.96f, 0.78f, 0.35f);
        ColorGL red    = ColorGL.RED;
        ColorGL orange = ColorGL.ORANGE;

        int curX  = RIBBON_X + 14;
        int textY = RIBBON_Y + 9;

        UtilsGL.glBegin(GL11.GL_QUADS);

        // Colony Date
        String dateText = (Game.getWorld() != null && Game.getWorld().getDate() != null)
                ? Game.getWorld().getDate().toString() : "Year 1";
        UtilsGL.drawStringWithBorder(dateText, curX, textY, gold, ColorGL.BLACK);
        curX += UtilFont.getWidth(dateText) + 12;

        UtilsGL.drawStringWithBorder("|", curX, textY, gold, ColorGL.BLACK);
        curX += 12;

        // Wood
        String woodStr = "Wood: " + cachedWood;
        UtilsGL.drawStringWithBorder(woodStr, curX, textY, cream, ColorGL.BLACK);
        curX += UtilFont.getWidth(woodStr) + 14;

        // Stone
        String stoneStr = "Stone: " + cachedStone;
        UtilsGL.drawStringWithBorder(stoneStr, curX, textY, cream, ColorGL.BLACK);
        curX += UtilFont.getWidth(stoneStr) + 14;

        // Ore
        String oreStr = "Ore: " + cachedOre;
        UtilsGL.drawStringWithBorder(oreStr, curX, textY, cream, ColorGL.BLACK);
        curX += UtilFont.getWidth(oreStr) + 14;

        // Food
        String foodStr = "Food: " + cachedFood;
        UtilsGL.drawStringWithBorder(foodStr, curX, textY, cachedFood < 10 ? red : cream, ColorGL.BLACK);
        curX += UtilFont.getWidth(foodStr) + 14;

        // Coins / Gold
        int coins = (Game.getWorld() != null) ? Game.getWorld().getCoins() : 0;
        String coinStr = "Coins: " + coins;
        UtilsGL.drawStringWithBorder(coinStr, curX, textY, gold, ColorGL.BLACK);
        curX += UtilFont.getWidth(coinStr) + 14;

        // Population
        int totalCit = (World.getCitizenIDs() != null) ? World.getCitizenIDs().size() : 0;
        int idleCit  = countIdle(totalCit);
        String popStr = "Pop: " + totalCit + (idleCit > 0 ? " (" + idleCit + " idle)" : "");
        UtilsGL.drawStringWithBorder(popStr, curX, textY, idleCit > 0 ? orange : cream, ColorGL.BLACK);

        // Right-side elevation info
        int curZ = (Game.getWorld() != null && Game.getWorld().getView() != null)
                ? (World.MAP_NUM_LEVELS_OUTSIDE - Game.getWorld().getView().z) : 0;
        String zStr = "Z: " + ((curZ >= 0) ? "+" : "") + curZ;
        UtilsGL.drawStringWithBorder(zStr, rightControlsX - 52, textY, gold, ColorGL.BLACK);

        // Level Up/Down buttons
        UtilsGL.drawStringWithBorder("v", rightControlsX + 7,  textY, cream, ColorGL.BLACK);
        UtilsGL.drawStringWithBorder("^", rightControlsX + 33, textY, cream, ColorGL.BLACK);

        // Speed buttons
        boolean paused = Game.isPaused();
        UtilsGL.drawStringWithBorder("||", speedX + 8,  textY, paused                      ? gold : cream, ColorGL.BLACK);
        UtilsGL.drawStringWithBorder("1x", speedX + 42, textY, (!paused && World.SPEED == 1) ? gold : cream, ColorGL.BLACK);
        UtilsGL.drawStringWithBorder("3x", speedX + 84, textY, (!paused && World.SPEED >  1) ? gold : cream, ColorGL.BLACK);

        // Options button
        UtilsGL.drawStringWithBorder("Opt", settingsX + 4, textY, cream, ColorGL.BLACK);

        UtilsGL.glEnd();

        GL11.glColor4f(1.0f, 1.0f, 1.0f, 1.0f);
        GL11.glDisable(GL11.GL_BLEND);
    }

    private static int countIdle(int totalCit) {
        int idle = 0;
        if (World.getCitizenIDs() == null) return 0;
        for (int c = 0; c < totalCit; c++) {
            Citizen cit = (Citizen) World.getLivingEntityByID(World.getCitizenIDs().get(c));
            if (cit != null && cit.getCurrentTask() == null) idle++;
        }
        return idle;
    }

    private static void drawButtonBg(int x, int y, int w, int h, int mx, int my) {
        boolean hov = (mx >= x && mx <= x + w && my >= y && my <= y + h);
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

        GL11.glColor4f(hov ? 0.34f : 0.18f, hov ? 0.24f : 0.12f,
                       hov ? 0.16f : 0.08f, hov ? 0.95f : 0.88f);
        drawRect(x, y, w, h);

        GL11.glColor4f(hov ? 0.96f : 0.65f, hov ? 0.78f : 0.50f,
                       hov ? 0.35f : 0.22f, hov ? 1.00f : 0.70f);
        drawRectOutline(x, y, w, h, hov ? 1.5f : 1.0f);
    }

    private static void drawRect(int x, int y, int w, int h) {
        GL11.glBegin(GL11.GL_QUADS);
        GL11.glVertex2f(x,     y);
        GL11.glVertex2f(x + w, y);
        GL11.glVertex2f(x + w, y + h);
        GL11.glVertex2f(x,     y + h);
        GL11.glEnd();
    }

    private static void drawRectOutline(int x, int y, int w, int h, float lw) {
        GL11.glLineWidth(lw);
        GL11.glBegin(GL11.GL_LINE_LOOP);
        GL11.glVertex2f(x,     y);
        GL11.glVertex2f(x + w, y);
        GL11.glVertex2f(x + w, y + h);
        GL11.glVertex2f(x,     y + h);
        GL11.glEnd();
        GL11.glLineWidth(1.0f);
    }
}
