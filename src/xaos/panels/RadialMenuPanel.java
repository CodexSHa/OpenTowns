package xaos.panels;

import java.util.ArrayList;
import java.util.List;

import org.lwjgl.opengl.GL11;

import xaos.main.Game;
import xaos.tiles.Tile;
import xaos.utils.ColorGL;
import xaos.utils.Point3D;
import xaos.utils.UtilFont;
import xaos.utils.UtilsGL;

public class RadialMenuPanel {

    public static class RadialItem {
        public String label;
        public String command;
        public String tooltip;
        public Tile iconTile;

        public RadialItem(String label, String command, String tooltip, String tileName) {
            this.label = label;
            this.command = command;
            this.tooltip = tooltip;
            if (tileName != null) {
                try {
                    this.iconTile = new Tile(tileName);
                } catch (Exception e) {
                    this.iconTile = null;
                }
            }
        }
    }

    private static boolean active = false;
    private static int centerX = 0;
    private static int centerY = 0;
    private static Point3D targetTile = null;
    private static int hoveredIndex = -1;
    private static final List<RadialItem> items = new ArrayList<RadialItem>();
    private static final int RING_RADIUS = 68;
    private static final int BUTTON_RADIUS = 22;

    static {
        items.add(new RadialItem("Mine", CommandPanel.COMMAND_MINE, "Mine solid rock and ore", "ui_mine"));
        items.add(new RadialItem("Chop/Dig", CommandPanel.COMMAND_DIG, "Chop trees & dig surface ground", "ui_chop"));
        items.add(new RadialItem("Ladder", CommandPanel.COMMAND_MINE_LADDER, "Carve mine ladder downward", "ui_ladders"));
        items.add(new RadialItem("Auto Equip", CommandPanel.COMMAND_AUTOEQUIP, "Equip best gear", "ui_sarmor"));
        items.add(new RadialItem("Cancel", CommandPanel.COMMAND_CANCEL_ORDER, "Cancel pending orders", "ui_cancel"));
    }

    public static boolean isActive() {
        return active;
    }

    public static void open(int x, int y, Point3D tile) {
        centerX = x;
        centerY = y;
        targetTile = tile;
        active = true;
        hoveredIndex = -1;
    }

    public static void close() {
        active = false;
        hoveredIndex = -1;
        targetTile = null;
    }

    public static void update(int mouseX, int mouseY) {
        if (!active) return;

        hoveredIndex = -1;
        int numItems = items.size();
        for (int i = 0; i < numItems; i++) {
            double angle = i * (2.0 * Math.PI / numItems) - (Math.PI / 2.0);
            int btnX = centerX + (int) (RING_RADIUS * Math.cos(angle));
            int btnY = centerY + (int) (RING_RADIUS * Math.sin(angle));

            double distSq = (mouseX - btnX) * (mouseX - btnX) + (mouseY - btnY) * (mouseY - btnY);
            if (distSq <= (BUTTON_RADIUS + 6) * (BUTTON_RADIUS + 6)) {
                hoveredIndex = i;
                break;
            }
        }
    }

    public static boolean handleClick(int mouseX, int mouseY) {
        if (!active) return false;

        update(mouseX, mouseY);
        if (hoveredIndex >= 0 && hoveredIndex < items.size()) {
            RadialItem item = items.get(hoveredIndex);
            if (item.command != null) {
                CommandPanel.executeCommand(item.command, null, null, targetTile, null, 0);
            }
            close();
            return true;
        }

        // Click outside closes menu
        close();
        return true;
    }

    public static void render() {
        if (!active) return;
        if (Game.getCurrentState() == Game.STATE_SHOWING_CONTEXT_MENU && Game.getCurrentContextMenu() != null) {
            return;
        }

        int mouseX = xaos.compat.input.Mouse.getX ();
        int mouseY = xaos.compat.opengl.Display.getHeight () - xaos.compat.input.Mouse.getY () - 1;
        update(mouseX, mouseY);

        int numItems = items.size();
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

        // 1. Center backdrop disc
        GL11.glColor4f(0.11f, 0.08f, 0.05f, 0.70f);
        drawCircle(centerX, centerY, RING_RADIUS + BUTTON_RADIUS + 8);

        GL11.glColor4f(0.70f, 0.55f, 0.27f, 0.40f);
        drawCircleOutline(centerX, centerY, RING_RADIUS + BUTTON_RADIUS + 8, 1.0f);
        drawCircleOutline(centerX, centerY, RING_RADIUS - BUTTON_RADIUS - 4, 1.0f);

        // 2. Circular Buttons (Carved dark walnut with brass / gold borders)
        for (int i = 0; i < numItems; i++) {
            double angle = i * (2.0 * Math.PI / numItems) - (Math.PI / 2.0);
            int btnX = centerX + (int) (RING_RADIUS * Math.cos(angle));
            int btnY = centerY + (int) (RING_RADIUS * Math.sin(angle));

            boolean isHovered = (i == hoveredIndex);
            int r = isHovered ? BUTTON_RADIUS + 4 : BUTTON_RADIUS;

            // Outer dark walnut fill
            if (isHovered) {
                GL11.glColor4f(0.28f, 0.20f, 0.14f, 0.98f);
            } else {
                GL11.glColor4f(0.14f, 0.10f, 0.07f, 0.94f);
            }
            drawCircle(btnX, btnY, r);

            // Inner fill
            GL11.glColor4f(0.18f, 0.13f, 0.09f, 0.88f);
            drawCircle(btnX, btnY, r - 3);

            // 1px Brass / 2px Gold Illuminated Border
            if (isHovered) {
                GL11.glColor4f(0.96f, 0.78f, 0.35f, 1.00f);
                drawCircleOutline(btnX, btnY, r, 2.0f);
            } else {
                GL11.glColor4f(0.70f, 0.55f, 0.27f, 0.90f);
                drawCircleOutline(btnX, btnY, r, 1.0f);
            }
        }

        // 3. Render Action Sprite Icons centered inside each circular slot
        int curTexture = -1;
        for (int i = 0; i < numItems; i++) {
            RadialItem item = items.get(i);
            if (item.iconTile != null) {
                double angle = i * (2.0 * Math.PI / numItems) - (Math.PI / 2.0);
                int btnX = centerX + (int) (RING_RADIUS * Math.cos(angle));
                int btnY = centerY + (int) (RING_RADIUS * Math.sin(angle));
                boolean isHovered = (i == hoveredIndex);

                GL11.glEnable(GL11.GL_TEXTURE_2D);
                GL11.glColor4f(1.0f, 1.0f, 1.0f, 1.0f);
                curTexture = UtilsGL.setTexture(item.iconTile, curTexture);
                UIPanel.drawTile(item.iconTile, btnX - 14, btnY - 14, 28, 28, isHovered);
            }
        }
        UtilsGL.glEnd();

        // 4. Floating Tooltip for Hovered Item (Rendered on top of all radial elements)
        if (hoveredIndex >= 0 && hoveredIndex < items.size()) {
            RadialItem hoveredItem = items.get(hoveredIndex);
            double angle = hoveredIndex * (2.0 * Math.PI / numItems) - (Math.PI / 2.0);
            int btnX = centerX + (int) (RING_RADIUS * Math.cos(angle));
            int btnY = centerY + (int) (RING_RADIUS * Math.sin(angle));

            String headerText = hoveredItem.label;
            String descText = hoveredItem.tooltip;

            int tw1 = UtilFont.getWidth(headerText);
            int tw2 = UtilFont.getWidth(descText);
            int tipW = Math.max(tw1, tw2) + 20;
            int tipH = 32;

            int tipX = btnX - tipW / 2;
            int tipY = (btnY > centerY) ? btnY + BUTTON_RADIUS + 10 : btnY - BUTTON_RADIUS - tipH - 10;

            GL11.glDisable(GL11.GL_TEXTURE_2D);
            GL11.glColor4f(0.11f, 0.08f, 0.05f, 0.96f);
            drawRoundedRect(tipX, tipY, tipW, tipH, 4);

            GL11.glColor4f(0.96f, 0.78f, 0.35f, 1.00f);
            drawRectOutline(tipX, tipY, tipW, tipH, 1.0f);

            GL11.glEnable(GL11.GL_TEXTURE_2D);
            GL11.glBindTexture(GL11.GL_TEXTURE_2D, Game.TEXTURE_FONT_ID);
            GL11.glTexEnvf(GL11.GL_TEXTURE_ENV, GL11.GL_TEXTURE_ENV_MODE, GL11.GL_MODULATE);
            GL11.glColor4f(1.0f, 1.0f, 1.0f, 1.0f);

            ColorGL goldAccent = new ColorGL(0.96f, 0.78f, 0.35f);
            ColorGL creamColor = new ColorGL(0.96f, 0.90f, 0.78f);
            UtilsGL.glBegin(GL11.GL_QUADS);
            UtilsGL.drawStringWithBorder(headerText, tipX + 10, tipY + 4, goldAccent, ColorGL.BLACK);
            UtilsGL.drawStringWithBorder(descText, tipX + 10, tipY + 17, creamColor, ColorGL.BLACK);
            UtilsGL.glEnd();
        }

        GL11.glColor4f(1.0f, 1.0f, 1.0f, 1.0f);
        GL11.glEnable(GL11.GL_TEXTURE_2D);
        GL11.glDisable(GL11.GL_BLEND);
    }

    private static void drawCircle(float cx, float cy, float radius) {
        int segments = 28;
        GL11.glBegin(GL11.GL_TRIANGLE_FAN);
        GL11.glVertex2f(cx, cy);
        for (int i = 0; i <= segments; i++) {
            double angle = i * (2.0 * Math.PI / segments);
            GL11.glVertex2f((float) (cx + radius * Math.cos(angle)), (float) (cy + radius * Math.sin(angle)));
        }
        GL11.glEnd();
    }

    private static void drawCircleOutline(float cx, float cy, float radius, float lineWidth) {
        int segments = 28;
        GL11.glLineWidth(lineWidth);
        GL11.glBegin(GL11.GL_LINE_LOOP);
        for (int i = 0; i <= segments; i++) {
            double angle = i * (2.0 * Math.PI / segments);
            GL11.glVertex2f((float) (cx + radius * Math.cos(angle)), (float) (cy + radius * Math.sin(angle)));
        }
        GL11.glEnd();
    }

    private static void drawRoundedRect(int x, int y, int width, int height, int radius) {
        GL11.glBegin(GL11.GL_QUADS);
        GL11.glVertex2f(x, y);
        GL11.glVertex2f(x + width, y);
        GL11.glVertex2f(x + width, y + height);
        GL11.glVertex2f(x, y + height);
        GL11.glEnd();
    }

    private static void drawRectOutline(int x, int y, int width, int height, float lineWidth) {
        GL11.glLineWidth(lineWidth);
        GL11.glBegin(GL11.GL_LINE_LOOP);
        GL11.glVertex2f(x, y);
        GL11.glVertex2f(x + width, y);
        GL11.glVertex2f(x + width, y + height);
        GL11.glVertex2f(x, y + height);
        GL11.glEnd();
    }
}
