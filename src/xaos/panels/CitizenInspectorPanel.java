package xaos.panels;

import org.lwjgl.opengl.GL11;
import xaos.compat.opengl.Display;
import xaos.data.LivingEntityData;
import xaos.main.Game;
import xaos.tiles.Tile;
import xaos.tiles.entities.living.Citizen;
import xaos.tiles.entities.living.LivingEntity;
import xaos.utils.ColorGL;
import xaos.utils.Point3DShort;
import xaos.utils.UtilsGL;

public class CitizenInspectorPanel {

    private static LivingEntity inspectedEntity = null;
    private static boolean visible = false;
    private static final int PANEL_WIDTH = 310;
    private static final int PANEL_HEIGHT = 330;

    private static final ColorGL COLOR_PARCHMENT = new ColorGL(0.96f, 0.90f, 0.78f);
    private static final ColorGL COLOR_GOLD = new ColorGL(0.96f, 0.78f, 0.35f);

    public static void setInspectedEntity(LivingEntity entity) {
        inspectedEntity = entity;
        visible = (entity != null);
    }

    public static LivingEntity getInspectedEntity() {
        return inspectedEntity;
    }

    public static boolean isVisible() {
        return visible;
    }

    public static void close() {
        inspectedEntity = null;
        visible = false;
    }

    public static boolean handleClick(int mouseX, int mouseY) {
        if (!visible || inspectedEntity == null) return false;

        int posX = Display.getWidth() - PANEL_WIDTH - 20;
        int posY = 60;

        // Close button click [X]
        int closeX = posX + PANEL_WIDTH - 28;
        int closeY = posY + 8;
        if (mouseX >= closeX && mouseX <= closeX + 20 && mouseY >= closeY && mouseY <= closeY + 20) {
            close();
            return true;
        }

        // Click inside panel eats the click event
        if (mouseX >= posX && mouseX <= posX + PANEL_WIDTH && mouseY >= posY && mouseY <= posY + PANEL_HEIGHT) {
            return true;
        }

        return false;
    }

    public static void render() {
        if (!visible || inspectedEntity == null) return;

        LivingEntityData led = inspectedEntity.getLivingEntityData();
        if (led == null) return;

        int posX = Display.getWidth() - PANEL_WIDTH - 20;
        int posY = 60;

        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

        // 1. Dark Walnut Card Background
        GL11.glColor4f(0.11f, 0.08f, 0.05f, 0.94f);
        drawRoundedRect(posX, posY, PANEL_WIDTH, PANEL_HEIGHT, 8);

        // 2. Header Accent Strip (Inner parchment)
        GL11.glColor4f(0.18f, 0.13f, 0.09f, 0.96f);
        drawRoundedRect(posX + 4, posY + 4, PANEL_WIDTH - 8, 38, 6);

        // 3. Brass / Bronze 1px Outer Outline
        GL11.glColor4f(0.70f, 0.55f, 0.27f, 0.90f);
        drawRectOutline(posX, posY, PANEL_WIDTH, PANEL_HEIGHT, 1);
        drawRectOutline(posX + 4, posY + 4, PANEL_WIDTH - 8, 38, 1);

        // Close Button [X]
        int closeX = posX + PANEL_WIDTH - 28;
        int closeY = posY + 8;
        GL11.glColor4f(0.75f, 0.20f, 0.20f, 0.90f);
        drawRoundedRect(closeX, closeY, 20, 20, 4);
        GL11.glColor4f(0.96f, 0.78f, 0.35f, 1.00f);
        drawRectOutline(closeX, closeY, 20, 20, 1);

        // Character / Role Badge from sprite sheets
        try {
            Tile roleTile = new Tile(inspectedEntity.getEquippedData() != null ? "ui_sweapon" : "ui_zonespersonal");
            GL11.glEnable(GL11.GL_TEXTURE_2D);
            GL11.glColor4f(1.0f, 1.0f, 1.0f, 1.0f);
            GL11.glBindTexture(GL11.GL_TEXTURE_2D, roleTile.getTextureID());
            GL11.glTexEnvf(GL11.GL_TEXTURE_ENV, GL11.GL_TEXTURE_ENV_MODE, GL11.GL_MODULATE);
            UtilsGL.glBegin(GL11.GL_QUADS);
            UIPanel.drawTile(roleTile, posX + PANEL_WIDTH - 65, posY + 9, 26, 26, false);
            UtilsGL.glEnd();
        } catch (Exception e) {
            // Ignore if tile not found
        }

        GL11.glEnable(GL11.GL_TEXTURE_2D);
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, Game.TEXTURE_FONT_ID);
        GL11.glTexEnvf(GL11.GL_TEXTURE_ENV, GL11.GL_TEXTURE_ENV_MODE, GL11.GL_MODULATE);

        // Header Title (Name & Title)
        String title = led.getName();
        if (title == null || title.isEmpty()) {
            title = "Citizen Inspector";
        }

        UtilsGL.glBegin(GL11.GL_QUADS);
        UtilsGL.drawStringWithBorder(title, posX + 14, posY + 12, COLOR_GOLD, ColorGL.BLACK);
        UtilsGL.drawStringWithBorder("X", closeX + 5, closeY + 3, COLOR_PARCHMENT, ColorGL.BLACK);

        int curY = posY + 52;

        // Health Bar label
        int hp = led.getHealthPoints();
        int maxHp = Math.max(1, led.getHealthPointsMAXCurrent());
        float hpPct = Math.max(0.0f, Math.min(1.0f, (float) hp / (float) maxHp));

        UtilsGL.drawStringWithBorder("Health:", posX + 16, curY, COLOR_PARCHMENT, ColorGL.BLACK);
        UtilsGL.drawStringWithBorder(hp + " / " + maxHp, posX + 210, curY, COLOR_GOLD, ColorGL.BLACK);
        UtilsGL.glEnd();

        curY += 18;

        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glColor4f(0.18f, 0.13f, 0.09f, 0.90f);
        drawRoundedRect(posX + 16, curY, PANEL_WIDTH - 32, 14, 4);

        if (hpPct > 0.5f) {
            GL11.glColor4f(0.25f, 0.75f, 0.30f, 0.95f);
        } else if (hpPct > 0.25f) {
            GL11.glColor4f(0.95f, 0.70f, 0.20f, 0.95f);
        } else {
            GL11.glColor4f(0.85f, 0.20f, 0.20f, 0.95f);
        }
        drawRoundedRect(posX + 16, curY, (int) ((PANEL_WIDTH - 32) * hpPct), 14, 4);

        curY += 22;

        // Remaining Stats Text
        GL11.glEnable(GL11.GL_TEXTURE_2D);
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, Game.TEXTURE_FONT_ID);
        GL11.glTexEnvf(GL11.GL_TEXTURE_ENV, GL11.GL_TEXTURE_ENV_MODE, GL11.GL_MODULATE);

        UtilsGL.glBegin(GL11.GL_QUADS);

        // Morale / Happiness Bar
        int moral = led.getMoral();
        UtilsGL.drawStringWithBorder("Morale / Happiness:", posX + 16, curY, COLOR_PARCHMENT, ColorGL.BLACK);
        UtilsGL.drawStringWithBorder(moral + "%", posX + 230, curY, COLOR_GOLD, ColorGL.BLACK);
        curY += 22;

        // Position & Coordinates
        Point3DShort pos = inspectedEntity.getCoordinates();
        UtilsGL.drawStringWithBorder("Location: [" + pos.x + ", " + pos.y + ", Z:" + pos.z + "]", posX + 16, curY, COLOR_PARCHMENT, ColorGL.BLACK);
        curY += 22;

        // Combat Stats Grid
        UtilsGL.drawStringWithBorder("Attack: " + led.getAttackCurrent(), posX + 16, curY, COLOR_GOLD, ColorGL.BLACK);
        UtilsGL.drawStringWithBorder("Defense: " + led.getDefenseCurrent(), posX + 160, curY, COLOR_GOLD, ColorGL.BLACK);
        curY += 20;

        UtilsGL.drawStringWithBorder("Speed: " + led.getWalkSpeedCurrent(), posX + 16, curY, COLOR_PARCHMENT, ColorGL.BLACK);
        UtilsGL.drawStringWithBorder("LOS: " + led.getLOSCurrent(), posX + 160, curY, COLOR_PARCHMENT, ColorGL.BLACK);
        curY += 24;

        // Active Task / Activity
        String activityDesc = "Idle / Resting";
        if (inspectedEntity instanceof Citizen) {
            Citizen citizen = (Citizen) inspectedEntity;
            if (citizen.getCurrentTask() != null) {
                activityDesc = "Task #" + citizen.getCurrentTask().getTask();
            }
        }
        UtilsGL.drawStringWithBorder("Activity: " + activityDesc, posX + 16, curY, COLOR_PARCHMENT, ColorGL.BLACK);
        curY += 24;

        // Inventory / Carried Equipment
        UtilsGL.drawStringWithBorder("Equipment / Status:", posX + 16, curY, COLOR_GOLD, ColorGL.BLACK);
        curY += 18;

        if (inspectedEntity.getEquippedData() == null) {
            UtilsGL.drawStringWithBorder("  • Standard Peasant Garb", posX + 16, curY, COLOR_PARCHMENT, ColorGL.BLACK);
        } else {
            UtilsGL.drawStringWithBorder("  • Battle Equipped & Armored", posX + 16, curY, COLOR_PARCHMENT, ColorGL.BLACK);
        }

        UtilsGL.glEnd();

        GL11.glColor4f(1.0f, 1.0f, 1.0f, 1.0f);
        GL11.glDisable(GL11.GL_BLEND);
    }

    private static void drawRoundedRect(int x, int y, int width, int height, int radius) {
        GL11.glBegin(GL11.GL_QUADS);
        GL11.glVertex2f(x, y);
        GL11.glVertex2f(x + width, y);
        GL11.glVertex2f(x + width, y + height);
        GL11.glVertex2f(x, y + height);
        GL11.glEnd();
    }

    private static void drawRectOutline(int x, int y, int width, int height, int lineWidth) {
        GL11.glLineWidth(lineWidth);
        GL11.glBegin(GL11.GL_LINE_LOOP);
        GL11.glVertex2f(x, y);
        GL11.glVertex2f(x + width, y);
        GL11.glVertex2f(x + width, y + height);
        GL11.glVertex2f(x, y + height);
        GL11.glEnd();
        GL11.glLineWidth(1.0f);
    }
}
