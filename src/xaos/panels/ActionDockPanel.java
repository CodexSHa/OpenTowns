package xaos.panels;

import java.util.ArrayList;
import java.util.List;

import org.lwjgl.opengl.GL11;

import xaos.main.Game;
import xaos.panels.menus.SmartMenu;
import xaos.utils.ColorGL;
import xaos.utils.UtilFont;
import xaos.utils.UtilsAL;
import xaos.utils.UtilsGL;
import xaos.tiles.Tile;

/**
 * Bottom action dock panel — organized tab-based command bar.
 * Properly wraps all sprite and font drawing in GL begin/end blocks.
 */
public class ActionDockPanel {

    // -------------------------------------------------------------------------
    // Category tabs
    // -------------------------------------------------------------------------

    public enum CategoryTab {
        ORDERS("Orders"),
        ZONES("Zones"),
        STRUCTURES("Structures"),
        PRODUCTION("Production"),
        MILITARY("Military");

        public final String title;
        CategoryTab(String t) { this.title = t; }
    }

    // -------------------------------------------------------------------------
    // ActionButton
    // -------------------------------------------------------------------------

    public static class ActionButton {
        public final String label;
        public final String command;
        public final String parameter;
        public final String shortcut;
        public final String tooltip;
        private final String tileName;
        public Tile iconTile;
        private boolean tileResolved = false;

        public ActionButton(String label, String command, String shortcut,
                            String tooltip, String tileName) {
            this(label, command, null, shortcut, tooltip, tileName);
        }

        public ActionButton(String label, String command, String parameter,
                            String shortcut, String tooltip, String tileName) {
            this.label     = label;
            this.command   = command;
            this.parameter = parameter;
            this.shortcut  = shortcut;
            this.tooltip   = tooltip;
            this.tileName  = tileName;
        }

        public void resolveTile() {
            if (tileResolved) return;
            tileResolved = true;
            if (tileName == null) return;
            try {
                Tile t = new Tile(tileName);
                if (t.getTextureID() > 0) {
                    iconTile = t;
                }
            } catch (Exception e) {
                iconTile = null;
            }
        }
    }

    // -------------------------------------------------------------------------
    // Layout constants
    // -------------------------------------------------------------------------

    private static final int DOCK_WIDTH    = 700;
    private static final int DOCK_HEIGHT   = 82;
    private static final int TAB_HEIGHT    = 24;
    private static final int BUTTON_HEIGHT = 64;

    // -------------------------------------------------------------------------
    // Button lists
    // -------------------------------------------------------------------------

    private static final List<ActionButton> ordersActions     = new ArrayList<>();
    private static final List<ActionButton> zonesActions      = new ArrayList<>();
    private static final List<ActionButton> structuresActions = new ArrayList<>();
    private static final List<ActionButton> productionActions = new ArrayList<>();
    private static final List<ActionButton> militaryActions   = new ArrayList<>();

    static {
        // Orders
        ordersActions.add(new ActionButton("Mine",      CommandPanel.COMMAND_MINE,           "M", "Mine solid rock and ore veins",        "ui_mine"));
        ordersActions.add(new ActionButton("Ladder",    CommandPanel.COMMAND_MINE_LADDER,    "L", "Carve downward mine ladder",           "ui_dig"));
        ordersActions.add(new ActionButton("Chop/Dig",  CommandPanel.COMMAND_DIG,            "D", "Chop trees & dig surface ground",      "ui_chop"));
        ordersActions.add(new ActionButton("Harvest",   CommandPanel.COMMAND_AUTOEQUIP,      "H", "Harvest crops and gather resources",   "ui_harvest"));
        ordersActions.add(new ActionButton("Cancel",    CommandPanel.COMMAND_CANCEL_ORDER,   "C", "Cancel pending orders on tiles",       "ui_cancel"));

        // Zones
        zonesActions.add(new ActionButton("Stockpile",  CommandPanel.COMMAND_STOCKPILE,                       "Z", "Designate resource storage area",      "ui_stockpile"));
        zonesActions.add(new ActionButton("Carpentry",  CommandPanel.COMMAND_CREATE_ZONE, "zcarpentry",       "C", "Designate carpentry workshop zone",    "ui_zonescarpentry"));
        zonesActions.add(new ActionButton("Masonry",    CommandPanel.COMMAND_CREATE_ZONE, "zmasonry",         "M", "Designate masonry workshop zone",      "ui_zonesmasonry"));
        zonesActions.add(new ActionButton("Bakery",     CommandPanel.COMMAND_CREATE_ZONE, "zbakery",          "B", "Designate bakery workshop zone",       "ui_zonesbakery"));
        zonesActions.add(new ActionButton("Forge",      CommandPanel.COMMAND_CREATE_ZONE, "zforge",           "G", "Designate forge / blacksmith zone",    "ui_zonesforge"));
        zonesActions.add(new ActionButton("Kitchen",    CommandPanel.COMMAND_CREATE_ZONE, "zkitchen",         "K", "Designate kitchen cooking zone",       "ui_zoneskitchen"));
        zonesActions.add(new ActionButton("Bedroom",    CommandPanel.COMMAND_CREATE_ZONE, "zpersonal",        "R", "Designate settler living quarters",    "ui_zonespersonal"));
        zonesActions.add(new ActionButton("Del Zone",   CommandPanel.COMMAND_DELETE_ZONE,                     "X", "Remove designated zone",               "ui_cancel"));

        // Structures
        structuresActions.add(new ActionButton("Walls",     "OPEN_RIGHT_CATEGORY", "walls",       "W", "Build log, wood & stone walls",        "ui_rwalls"));
        structuresActions.add(new ActionButton("Roofs",     "OPEN_RIGHT_CATEGORY", "roof",        "R", "Build tile, straw & dome roofs",       "ui_roofs"));
        structuresActions.add(new ActionButton("Doors",     "OPEN_RIGHT_CATEGORY", "doors",       "G", "Place wooden and iron doors",          "ui_rdoors"));
        structuresActions.add(new ActionButton("Furniture", "OPEN_RIGHT_CATEGORY", "furniture",   "U", "Place beds, tables, chests & chairs",  "ui_sfurniture"));
        structuresActions.add(new ActionButton("Dismantle", CommandPanel.COMMAND_DESTROY_BUILDING, "X", "Dismantle target structure",          "ui_destroyscaffold"));

        // Production
        productionActions.add(new ActionButton("Food",       "OPEN_PRODUCTION_CATEGORY", "food",       "F", "Craft bakery, pies, meat & rations",   "ui_rfood"));
        productionActions.add(new ActionButton("Military",   "OPEN_PRODUCTION_CATEGORY", "militaries", "A", "Craft swords, shields & armor gear",   "ui_rmilitary"));
        productionActions.add(new ActionButton("Utilities",  "OPEN_PRODUCTION_CATEGORY", "utils",      "U", "Craft tongs, carvers & torches",       "ui_rutils"));
        productionActions.add(new ActionButton("Containers", "OPEN_PRODUCTION_CATEGORY", "containers", "C", "Craft barrels, chests & cabinets",     "ui_rcontainers"));
        productionActions.add(new ActionButton("Materials",  "OPEN_PRODUCTION_CATEGORY", "materials",  "M", "Refine wood, bamboo, glass & wool",    "ui_rmaterials"));
    }

    // -------------------------------------------------------------------------
    // State
    // -------------------------------------------------------------------------

    private static CategoryTab currentTab    = CategoryTab.ORDERS;
    private static int hoveredTabIndex    = -1;
    private static int hoveredActionIndex = -1;

    public static List<ActionButton> getCurrentActions() {
        switch (currentTab) {
            case ZONES:      return zonesActions;
            case STRUCTURES: return structuresActions;
            case PRODUCTION: return productionActions;
            case MILITARY:   return militaryActions;
            case ORDERS: default: return ordersActions;
        }
    }

    public static boolean isMouseOverDock(int mx, int my, int sw, int sh) {
        int dockX = (sw - DOCK_WIDTH) / 2;
        int dockY = sh - DOCK_HEIGHT - 12;
        return mx >= dockX && mx <= dockX + DOCK_WIDTH
            && my >= dockY - TAB_HEIGHT && my <= dockY + DOCK_HEIGHT;
    }

    public static boolean handleClick(int mouseX, int mouseY, int screenWidth, int screenHeight) {
        int dockX = (screenWidth - DOCK_WIDTH) / 2;
        int dockY = screenHeight - DOCK_HEIGHT - 12;

        // Tab click
        if (mouseY >= dockY - TAB_HEIGHT && mouseY < dockY) {
            CategoryTab[] tabs = CategoryTab.values();
            int tabWidth = DOCK_WIDTH / tabs.length;
            for (int t = 0; t < tabs.length; t++) {
                int tx = dockX + t * tabWidth;
                if (mouseX >= tx && mouseX < tx + tabWidth) {
                    currentTab = tabs[t];
                    UtilsAL.play(UtilsAL.SOURCE_FX_CLICK, 0);
                    return true;
                }
            }
        }

        // Action button click
        if (mouseY >= dockY && mouseY <= dockY + DOCK_HEIGHT) {
            List<ActionButton> actions = getCurrentActions();
            int numActions = actions.size();
            int btnWidth = computeBtnWidth(numActions);
            for (int i = 0; i < numActions; i++) {
                int bx = dockX + 10 + i * (btnWidth + 6);
                int by = dockY + 8;
                if (mouseX >= bx && mouseX <= bx + btnWidth
                        && mouseY >= by && mouseY <= by + BUTTON_HEIGHT) {
                    ActionButton btn = actions.get(i);
                    if (btn.command != null) {
                        if (btn.command.equals("OPEN_PRODUCTION")) {
                            boolean wasActive = ProductionUIPanel.isProductionPanelActive();
                            ProductionUIPanel.setProductionPanelActive(!wasActive);
                            ProductionUIPanel.setProductionPanelLocked(!wasActive);
                        } else if (btn.command.equals("OPEN_PRODUCTION_CATEGORY")) {
                            ProductionUIPanel.setProductionPanelActive(true);
                            ProductionUIPanel.setProductionPanelLocked(true);
                            if (ProductionUIPanel.productionPanelMenu != null && btn.parameter != null) {
                                SmartMenu root = ProductionUIPanel.productionPanelMenu;
                                while (root.getParent() != null) {
                                    root = root.getParent();
                                }
                                SmartMenu targetMenu = findMenu(root, btn.parameter);
                                if (targetMenu != null) {
                                    ProductionUIPanel.productionPanelMenu = targetMenu;
                                    ProductionUIPanel.createProductionPanel(targetMenu);
                                }
                            }
                        } else if (btn.command.equals("OPEN_RIGHT_CATEGORY")) {
                            RightMenuUIPanel.setMenuPanelActive(true);
                            RightMenuUIPanel.setMenuPanelLocked(true);
                            if (RightMenuUIPanel.menuPanelMenu != null && btn.parameter != null) {
                                SmartMenu root = RightMenuUIPanel.menuPanelMenu;
                                while (root.getParent() != null) {
                                    root = root.getParent();
                                }
                                SmartMenu targetMenu = findMenu(root, btn.parameter);
                                if (targetMenu != null) {
                                    RightMenuUIPanel.menuPanelMenu = targetMenu;
                                    UIPanel.createMenuPanel(targetMenu);
                                }
                            }
                        } else {
                            CommandPanel.executeCommand(btn.command, btn.parameter, null, null, null, 0);
                        }
                        UtilsAL.play(UtilsAL.SOURCE_FX_CLICK, 0);
                    }
                    return true;
                }
            }
            return true;
        }

        return false;
    }

    public static void render(int mouseX, int mouseY, int screenWidth, int screenHeight) {
        int dockX = (screenWidth - DOCK_WIDTH) / 2;
        int dockY = screenHeight - DOCK_HEIGHT - 12;

        hoveredTabIndex    = -1;
        hoveredActionIndex = -1;

        CategoryTab[] tabs = CategoryTab.values();
        int tabWidth = DOCK_WIDTH / tabs.length;

        // Hover detection
        if (mouseY >= dockY - TAB_HEIGHT && mouseY < dockY) {
            for (int t = 0; t < tabs.length; t++) {
                if (mouseX >= dockX + t * tabWidth && mouseX < dockX + (t + 1) * tabWidth) {
                    hoveredTabIndex = t; break;
                }
            }
        }

        // 1. Category Tab Strip Backgrounds
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

        for (int t = 0; t < tabs.length; t++) {
            int tx = dockX + t * tabWidth;
            boolean sel = (currentTab == tabs[t]);
            boolean hov = (hoveredTabIndex == t);

            GL11.glColor4f(sel ? 0.30f : (hov ? 0.22f : 0.13f),
                           sel ? 0.22f : (hov ? 0.16f : 0.09f),
                           sel ? 0.15f : (hov ? 0.11f : 0.06f),
                           sel ? 0.98f : (hov ? 0.92f : 0.82f));
            drawRect(tx + 2, dockY - TAB_HEIGHT, tabWidth - 4, TAB_HEIGHT);

            GL11.glColor4f(sel ? 0.96f : 0.65f, sel ? 0.78f : 0.50f,
                           sel ? 0.35f : 0.22f, sel ? 1.00f : 0.65f);
            drawRectOutline(tx + 2, dockY - TAB_HEIGHT, tabWidth - 4, TAB_HEIGHT, sel ? 1.5f : 1.0f);
        }

        // 2. Main Dock Solid Frame
        UtilsGL.drawMedievalBox(dockX, dockY, dockX + DOCK_WIDTH, dockY + DOCK_HEIGHT);

        // 3. Action Button Slots Backgrounds
        List<ActionButton> actions = getCurrentActions();
        int numActions = actions.size();
        int btnWidth   = computeBtnWidth(numActions);

        for (int i = 0; i < numActions; i++) {
            int bx = dockX + 10 + i * (btnWidth + 6);
            int by = dockY + 8;

            boolean hov = (mouseX >= bx && mouseX <= bx + btnWidth
                        && mouseY >= by && mouseY <= by + BUTTON_HEIGHT);
            if (hov) hoveredActionIndex = i;

            GL11.glDisable(GL11.GL_TEXTURE_2D);
            GL11.glEnable(GL11.GL_BLEND);
            GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

            GL11.glColor4f(hov ? 0.34f : 0.18f, hov ? 0.24f : 0.12f,
                           hov ? 0.16f : 0.08f, hov ? 0.98f : 0.90f);
            drawRect(bx, by, btnWidth, BUTTON_HEIGHT);

            GL11.glColor4f(hov ? 0.96f : 0.68f, hov ? 0.78f : 0.52f,
                           hov ? 0.35f : 0.24f, hov ? 1.00f : 0.75f);
            drawRectOutline(bx, by, btnWidth, BUTTON_HEIGHT, hov ? 1.5f : 1.0f);
        }

        // 4. Render Action Button Icons (Properly wrapped in texture & quad block)
        GL11.glEnable(GL11.GL_TEXTURE_2D);
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

        for (int i = 0; i < numActions; i++) {
            ActionButton btn = actions.get(i);
            btn.resolveTile();
            if (btn.iconTile != null) {
                int bx = dockX + 10 + i * (btnWidth + 6);
                int by = dockY + 8;
                boolean hov = (i == hoveredActionIndex);

                int iconSize = 36;
                int ix = bx + (btnWidth - iconSize) / 2;
                int iy = by + 6;

                GL11.glColor4f(1.0f, 1.0f, 1.0f, hov ? 1.0f : 0.92f);
                GL11.glBindTexture(GL11.GL_TEXTURE_2D, btn.iconTile.getTextureID());
                GL11.glTexEnvf(GL11.GL_TEXTURE_ENV, GL11.GL_TEXTURE_ENV_MODE, GL11.GL_MODULATE);
                UtilsGL.glBegin(GL11.GL_QUADS);
                UIPanel.drawTile(btn.iconTile, ix, iy, iconSize, iconSize, hov);
                UtilsGL.glEnd();
            }
        }

        // 5. Render Font Texts (Tab Titles, Shortcuts, Labels)
        GL11.glEnable(GL11.GL_TEXTURE_2D);
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, Game.TEXTURE_FONT_ID);
        GL11.glTexEnvf(GL11.GL_TEXTURE_ENV, GL11.GL_TEXTURE_ENV_MODE, GL11.GL_MODULATE);

        ColorGL cream = new ColorGL(0.96f, 0.90f, 0.78f);
        ColorGL gold  = new ColorGL(0.96f, 0.78f, 0.35f);

        UtilsGL.glBegin(GL11.GL_QUADS);

        // Tab titles
        for (int t = 0; t < tabs.length; t++) {
            boolean sel = (currentTab == tabs[t]);
            String title = tabs[t].title;
            int tw = UtilFont.getWidth(title);
            int tx = dockX + t * tabWidth;
            UtilsGL.drawStringWithBorder(title,
                tx + (tabWidth - tw) / 2, dockY - TAB_HEIGHT + 5,
                sel ? gold : cream, ColorGL.BLACK);
        }

        // Action button shortcuts & labels
        for (int i = 0; i < numActions; i++) {
            ActionButton btn = actions.get(i);
            boolean hov = (i == hoveredActionIndex);
            int bx = dockX + 10 + i * (btnWidth + 6);
            int by = dockY + 8;

            // Shortcut [Key] badge (top-left)
            String sc = btn.shortcut;
            UtilsGL.drawStringWithBorder(sc, bx + 4, by + 3,
                hov ? gold : new ColorGL(0.80f, 0.65f, 0.35f), ColorGL.BLACK);

            // Name label (centred at bottom of button)
            String lbl = btn.label;
            int lw = UtilFont.getWidth(lbl);
            int labelY = by + BUTTON_HEIGHT - 15;
            UtilsGL.drawStringWithBorder(lbl, bx + (btnWidth - lw) / 2, labelY,
                hov ? gold : cream, ColorGL.BLACK);
        }

        UtilsGL.glEnd();

        // 6. Floating Tooltip (When hovered)
        if (hoveredActionIndex >= 0 && hoveredActionIndex < actions.size()) {
            ActionButton btn = actions.get(hoveredActionIndex);
            int bx = dockX + 10 + hoveredActionIndex * (btnWidth + 6);
            int tooltipY = dockY - TAB_HEIGHT - 44;

            String hdr  = btn.label + " [" + btn.shortcut + "]";
            String desc = btn.tooltip;
            int tw1  = UtilFont.getWidth(hdr);
            int tw2  = UtilFont.getWidth(desc);
            int tipW = Math.max(tw1, tw2) + 24;
            int tipH = 38;
            int tipX = bx + btnWidth / 2 - tipW / 2;
            tipX = Math.max(dockX, Math.min(tipX, dockX + DOCK_WIDTH - tipW));

            // Tooltip background box
            GL11.glDisable(GL11.GL_TEXTURE_2D);
            GL11.glEnable(GL11.GL_BLEND);
            GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

            GL11.glColor4f(0.10f, 0.07f, 0.04f, 0.96f);
            drawRect(tipX, tooltipY, tipW, tipH);

            GL11.glColor4f(0.96f, 0.78f, 0.35f, 1.00f);
            drawRectOutline(tipX, tooltipY, tipW, tipH, 1.0f);

            // Tooltip text
            GL11.glEnable(GL11.GL_TEXTURE_2D);
            GL11.glBindTexture(GL11.GL_TEXTURE_2D, Game.TEXTURE_FONT_ID);
            GL11.glTexEnvf(GL11.GL_TEXTURE_ENV, GL11.GL_TEXTURE_ENV_MODE, GL11.GL_MODULATE);

            UtilsGL.glBegin(GL11.GL_QUADS);
            UtilsGL.drawStringWithBorder(hdr,  tipX + 12, tooltipY + 5,  gold,  ColorGL.BLACK);
            UtilsGL.drawStringWithBorder(desc, tipX + 12, tooltipY + 20, cream, ColorGL.BLACK);
            UtilsGL.glEnd();
        }

        GL11.glColor4f(1.0f, 1.0f, 1.0f, 1.0f);
        GL11.glEnable(GL11.GL_TEXTURE_2D);
        GL11.glDisable(GL11.GL_BLEND);
    }

    private static int computeBtnWidth(int numActions) {
        if (numActions <= 0) return DOCK_WIDTH - 20;
        return (DOCK_WIDTH - 20 - (numActions - 1) * 6) / numActions;
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

    private static SmartMenu findMenu(SmartMenu root, String target) {
        if (root == null || target == null) return null;
        if ((root.getParameter() != null && root.getParameter().equalsIgnoreCase(target)) ||
            (root.getID() != null && root.getID().equalsIgnoreCase(target))) {
            return root;
        }
        if (root.getItems() != null) {
            for (SmartMenu child : root.getItems()) {
                SmartMenu found = findMenu(child, target);
                if (found != null) return found;
            }
        }
        return null;
    }
}
