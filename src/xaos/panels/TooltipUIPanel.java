package xaos.panels;

import java.util.ArrayList;
import org.lwjgl.opengl.GL11;

import xaos.data.EventData;
import xaos.events.EventManager;
import xaos.events.EventManagerItem;
import xaos.main.Game;
import xaos.utils.ColorGL;
import xaos.utils.Messages;
import xaos.utils.UtilFont;
import xaos.utils.UtilsGL;

/**
 * Tooltip UI Panel - handles formatting, positioning, and rendering of all in-game tooltips.
 * Encapsulates OpenGL state to prevent leaks.
 */
public final class TooltipUIPanel {

    private TooltipUIPanel() {}

    /**
     * Renders a modern glassmorphic tooltip box with text.
     */
    public static void drawTooltipBox(String text, int x, int y, int screenWidth, int screenHeight) {
        if (text == null || text.trim().isEmpty()) return;

        int textW = UtilFont.getWidth(text);
        int tipW = textW + 16;
        int tipH = UtilFont.MAX_HEIGHT + 10;

        int tipX = Math.max(8, Math.min(x, screenWidth - tipW - 8));
        int tipY = Math.max(8, Math.min(y, screenHeight - tipH - 8));

        // Background
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

        GL11.glColor4f(0.10f, 0.08f, 0.05f, 0.94f);
        GL11.glBegin(GL11.GL_QUADS);
        GL11.glVertex2f(tipX, tipY);
        GL11.glVertex2f(tipX + tipW, tipY);
        GL11.glVertex2f(tipX + tipW, tipY + tipH);
        GL11.glVertex2f(tipX, tipY + tipH);
        GL11.glEnd();

        // 1px Gold border
        GL11.glColor4f(0.85f, 0.70f, 0.35f, 0.90f);
        GL11.glLineWidth(1.0f);
        GL11.glBegin(GL11.GL_LINE_LOOP);
        GL11.glVertex2f(tipX, tipY);
        GL11.glVertex2f(tipX + tipW, tipY);
        GL11.glVertex2f(tipX + tipW, tipY + tipH);
        GL11.glVertex2f(tipX, tipY + tipH);
        GL11.glEnd();

        // Text
        GL11.glEnable(GL11.GL_TEXTURE_2D);
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, Game.TEXTURE_FONT_ID);
        GL11.glTexEnvf(GL11.GL_TEXTURE_ENV, GL11.GL_TEXTURE_ENV_MODE, GL11.GL_MODULATE);

        UtilsGL.glBegin(GL11.GL_QUADS);
        UtilsGL.drawStringWithBorder(text, tipX + 8, tipY + 5, new ColorGL(0.96f, 0.90f, 0.78f), ColorGL.BLACK);
        UtilsGL.glEnd();
    }

    /**
     * Renders global town events tooltip with icons and descriptions.
     */
    public static void renderEventsTooltip(int x, int y, int screenWidth, int screenHeight) {
        ArrayList<EventData> alEvents = Game.getWorld().getEvents();
        if (alEvents == null || alEvents.isEmpty()) {
            String emptyTip = Messages.getString("UIPanel.83");
            drawTooltipBox(emptyTip, x, y, screenWidth, screenHeight);
            return;
        }

        String title = Messages.getString("UIPanel.84");
        int tooltipWidth = UtilFont.getWidth(title);
        int tooltipHeight = UtilFont.MAX_HEIGHT + 8;

        for (EventData ed : alEvents) {
            EventManagerItem emi = EventManager.getItem(ed.getEventID());
            if (emi != null) {
                int itemW = UtilFont.getWidth(emi.getName()) + (emi.getIcon() != null ? emi.getIcon().getTileWidth() + 6 : 0);
                if (itemW > tooltipWidth) tooltipWidth = itemW;
                tooltipHeight += (emi.getIcon() != null ? emi.getIcon().getTileHeight() + 4 : UtilFont.MAX_HEIGHT + 4);
            }
        }

        int tipW = tooltipWidth + 20;
        int tipH = tooltipHeight + 10;
        int tipX = Math.max(8, Math.min(x - tipW / 2, screenWidth - tipW - 8));
        int tipY = Math.max(8, Math.min(y, screenHeight - tipH - 8));

        // Background box
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

        GL11.glColor4f(0.10f, 0.08f, 0.05f, 0.96f);
        GL11.glBegin(GL11.GL_QUADS);
        GL11.glVertex2f(tipX, tipY);
        GL11.glVertex2f(tipX + tipW, tipY);
        GL11.glVertex2f(tipX + tipW, tipY + tipH);
        GL11.glVertex2f(tipX, tipY + tipH);
        GL11.glEnd();

        GL11.glColor4f(0.96f, 0.78f, 0.35f, 1.0f);
        GL11.glLineWidth(1.0f);
        GL11.glBegin(GL11.GL_LINE_LOOP);
        GL11.glVertex2f(tipX, tipY);
        GL11.glVertex2f(tipX + tipW, tipY);
        GL11.glVertex2f(tipX + tipW, tipY + tipH);
        GL11.glVertex2f(tipX, tipY + tipH);
        GL11.glEnd();

        // Render Icons
        GL11.glEnable(GL11.GL_TEXTURE_2D);
        int curTexture = -1;
        int curY = tipY + UtilFont.MAX_HEIGHT + 8;
        for (EventData ed : alEvents) {
            EventManagerItem emi = EventManager.getItem(ed.getEventID());
            if (emi != null && emi.getIcon() != null) {
                curTexture = UtilsGL.setTexture(emi.getIcon(), curTexture);
                UtilsGL.glBegin(GL11.GL_QUADS);
                UIPanel.drawTile(emi.getIcon(), tipX + 10, curY, false);
                UtilsGL.glEnd();
                curY += emi.getIcon().getTileHeight() + 4;
            }
        }

        // Render Text
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, Game.TEXTURE_FONT_ID);
        GL11.glTexEnvf(GL11.GL_TEXTURE_ENV, GL11.GL_TEXTURE_ENV_MODE, GL11.GL_MODULATE);

        UtilsGL.glBegin(GL11.GL_QUADS);
        UtilsGL.drawStringWithBorder(title, tipX + 10, tipY + 6, new ColorGL(0.96f, 0.78f, 0.35f), ColorGL.BLACK);

        curY = tipY + UtilFont.MAX_HEIGHT + 8;
        for (EventData ed : alEvents) {
            EventManagerItem emi = EventManager.getItem(ed.getEventID());
            if (emi != null) {
                int textOffsetX = (emi.getIcon() != null) ? emi.getIcon().getTileWidth() + 16 : 10;
                UtilsGL.drawStringWithBorder(emi.getName(), tipX + textOffsetX, curY + 2, new ColorGL(0.96f, 0.90f, 0.78f), ColorGL.BLACK);
                curY += (emi.getIcon() != null ? emi.getIcon().getTileHeight() + 4 : UtilFont.MAX_HEIGHT + 4);
            }
        }
        UtilsGL.glEnd();
    }
}
