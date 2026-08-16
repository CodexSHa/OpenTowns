package xaos.panels;

import java.awt.Point;
import java.util.ArrayList;
import org.lwjgl.opengl.GL11;
import xaos.panels.menus.SmartMenu;
import xaos.tiles.Tile;
import xaos.utils.UtilsGL;


public final class BottomMenuUIPanel {

	public final static int BOTTOM_PANEL_SCROLL_WIDTH = 32;

	public final static int BOTTOM_PANEL_WIDTH = 1024 - (BOTTOM_PANEL_SCROLL_WIDTH * 2);

	public final static int BOTTOM_PANEL_HEIGHT = 64;

	public final static int BOTTOM_PANEL_NUM_ITEMS = 10;

	private static int BOTTOM_SUBPANEL_WIDTH;

	private static int BOTTOM_SUBPANEL_HEIGHT;

	private static int BOTTOM_SUBPANEL_NUM_ITEMS_X;

	private static int BOTTOM_SUBPANEL_NUM_ITEMS_Y;

	private static int BOTTOM_SUBITEM_WIDTH = 64;

	private static int BOTTOM_SUBITEM_HEIGHT = 64;

	public static SmartMenu currentMenu;

	private static boolean bottomMenuPanelActive = true;

	private static boolean bottomMenuPanelLocked = true;

	// BOTTOM panel
	public static ArrayList<Point> bottomPanelItemsPosition; // Array de sólo BOTTOM_PANEL_NUM_ITEMS posiciones (9) con las coordenadas de los items que caben

	public static int bottomPanelItemIndex;

	public static int bottomPanelX;

	public static int bottomPanelY;

	public static int bottomPanelLeftScrollX;

	public static int bottomPanelRightScrollX;

	public static Tile tileBottomScrollLeft;

	public static Tile tileBottomScrollLeftON;

	public static Tile tileBottomScrollRight;

	public static Tile tileBottomScrollRightON;

	public static Tile tileBottomPanel;

	public static boolean tileBottomScrollLeftAlpha[][];

	public static boolean tileBottomScrollRightAlpha[][];

	public static boolean tileBottomPanelAlpha[][];

	public static Tile tileOpenBottomMenu;

	public static Point tileOpenCloseBottomMenuPoint = new Point (0, 0);

	private static ArrayList<Point> bottomSubPanelItemsPosition; // Array de BOTTOM_SUBPANEL_NUM_ITEMS_X x BOTTOM_SUBPANEL_NUM_ITEMS_Y posiciones con las coordenadas de los subitems

	public static Point bottomSubPanelPoint = new Point (0, 0);

	public static Tile[] tileBottomSubPanel;

	public static SmartMenu bottomSubPanelMenu;

	public static boolean tileBottomSubItemAlpha[][];

	// Menu Blinks
	public static boolean checkBlinkBottom = false;

	public static int renderBottomMenuPanel (int mouseX, int mouseY, int mousePanel, int iCurrentTexture) {
		/*
		 * BOTTOM PANEL (Textured bar from ui.png)
		 */
		GL11.glEnable (GL11.GL_TEXTURE_2D);
		GL11.glEnable (GL11.GL_BLEND);
		GL11.glBlendFunc (GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
		GL11.glColor4f (1.0f, 1.0f, 1.0f, 1.0f);
		iCurrentTexture = UtilsGL.setTexture (tileBottomPanel, iCurrentTexture);
		UtilsGL.glBegin (GL11.GL_QUADS);
		UtilsGL.drawTexture (bottomPanelLeftScrollX, bottomPanelY, bottomPanelRightScrollX + BOTTOM_PANEL_SCROLL_WIDTH, bottomPanelY + BOTTOM_PANEL_HEIGHT, tileBottomPanel.getTileSetTexX0 (), tileBottomPanel.getTileSetTexY0 (), tileBottomPanel.getTileSetTexX1 (), tileBottomPanel.getTileSetTexY1 ());
		UtilsGL.glEnd ();

		// BOTTOM PANEL Items
		int iItemBottomPanel;
		if (mousePanel == UIPanel.MOUSE_BOTTOM_ITEMS) {
			iItemBottomPanel = isMouseOnBottomItems (mouseX, mouseY);
		} else {
			iItemBottomPanel = -1;
		}

		// UI TEXTURE bottom panel
		Point point;
		for (int i = bottomPanelItemIndex; i < bottomPanelItemIndex + BOTTOM_PANEL_NUM_ITEMS; i++) {
			if (i > currentMenu.getItems ().size ()) {
				break;
			}

			point = bottomPanelItemsPosition.get (i - bottomPanelItemIndex);

			// Textured button frame from ui.png
			boolean hovered = (iItemBottomPanel == (i - bottomPanelItemIndex));
			GL11.glEnable (GL11.GL_TEXTURE_2D);
			GL11.glColor4f (hovered ? 1.0f : 0.85f, hovered ? 1.0f : 0.85f, hovered ? 1.0f : 0.85f, 1.0f);
			iCurrentTexture = UtilsGL.setTexture (UIPanel.tileBottomItem, iCurrentTexture);
			UtilsGL.glBegin (GL11.GL_QUADS);
			UIPanel.drawTile (UIPanel.tileBottomItem, point, UIPanel.BOTTOM_ITEM_WIDTH, UIPanel.BOTTOM_ITEM_HEIGHT, hovered);
			UtilsGL.glEnd ();

			GL11.glColor4f (1.0f, 1.0f, 1.0f, 1.0f);
			GL11.glEnable (GL11.GL_TEXTURE_2D);

			// Icono (UI or Item)
			Tile tile = currentMenu.getItems ().get (i).getIcon ();
			if (tile != null) {
				GL11.glBindTexture (GL11.GL_TEXTURE_2D, tile.getTextureID ());
				GL11.glTexEnvf (GL11.GL_TEXTURE_ENV, GL11.GL_TEXTURE_ENV_MODE, GL11.GL_MODULATE);
				UtilsGL.glBegin (GL11.GL_QUADS);
				UIPanel.drawTile (tile, point, UIPanel.BOTTOM_ITEM_WIDTH, UIPanel.BOTTOM_ITEM_HEIGHT, (iItemBottomPanel == (i - bottomPanelItemIndex)));
				UtilsGL.glEnd ();
			}
		}

		/*
		 * BOTTOM SUBPANEL
		 */
		int iItemBottomSubPanel;
		if (mousePanel == UIPanel.MOUSE_BOTTOM_SUBITEMS) {
			iItemBottomSubPanel = isMouseOnBottomSubItems (mouseX, mouseY);
		} else {
			iItemBottomSubPanel = -1;
		}
		if (bottomSubPanelMenu != null) {
			// Pintamos el panel
			int iCurrentTextureSub = tileBottomSubPanel[0].getTextureID ();
			GL11.glBindTexture (GL11.GL_TEXTURE_2D, iCurrentTextureSub);
			GL11.glTexEnvf (GL11.GL_TEXTURE_ENV, GL11.GL_TEXTURE_ENV_MODE, GL11.GL_MODULATE);
			UtilsGL.glBegin (GL11.GL_QUADS);
			UIPanel.renderBackground (tileBottomSubPanel, bottomSubPanelPoint, BOTTOM_SUBPANEL_WIDTH, BOTTOM_SUBPANEL_HEIGHT);
			UtilsGL.glEnd ();

			// Pintamos los items
			int iMenu;
			bucle1: for (int y = 0; y < BOTTOM_SUBPANEL_NUM_ITEMS_Y; y++) {
				for (int x = 0; x < BOTTOM_SUBPANEL_NUM_ITEMS_X; x++) {
					iMenu = (y * BOTTOM_SUBPANEL_NUM_ITEMS_X) + x;
					if (iMenu >= bottomSubPanelMenu.getItems ().size ()) {
						break bucle1;
					}

					point = bottomSubPanelItemsPosition.get (iMenu);
					boolean subHovered = (iItemBottomSubPanel == iMenu);

					GL11.glDisable (GL11.GL_TEXTURE_2D);
					GL11.glEnable (GL11.GL_BLEND);
					GL11.glBlendFunc (GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

					if (subHovered) {
						GL11.glColor4f (0.28f, 0.20f, 0.14f, 0.98f);
					} else {
						GL11.glColor4f (0.14f, 0.10f, 0.07f, 0.92f);
					}
					GL11.glBegin (GL11.GL_QUADS);
					GL11.glVertex2f (point.x + 2, point.y + 2);
					GL11.glVertex2f (point.x + UIPanel.BOTTOM_ITEM_WIDTH - 2, point.y + 2);
					GL11.glVertex2f (point.x + UIPanel.BOTTOM_ITEM_WIDTH - 2, point.y + UIPanel.BOTTOM_ITEM_HEIGHT - 2);
					GL11.glVertex2f (point.x + 2, point.y + UIPanel.BOTTOM_ITEM_HEIGHT - 2);
					GL11.glEnd ();

					// 1px Brass/Gold Outline
					GL11.glLineWidth (1.0f);
					if (subHovered) {
						GL11.glColor4f (0.96f, 0.78f, 0.35f, 1.00f);
					} else {
						GL11.glColor4f (0.70f, 0.55f, 0.27f, 0.85f);
					}
					GL11.glBegin (GL11.GL_LINE_LOOP);
					GL11.glVertex2f (point.x + 2, point.y + 2);
					GL11.glVertex2f (point.x + UIPanel.BOTTOM_ITEM_WIDTH - 2, point.y + 2);
					GL11.glVertex2f (point.x + UIPanel.BOTTOM_ITEM_WIDTH - 2, point.y + UIPanel.BOTTOM_ITEM_HEIGHT - 2);
					GL11.glVertex2f (point.x + 2, point.y + UIPanel.BOTTOM_ITEM_HEIGHT - 2);
					GL11.glEnd ();

					GL11.glColor4f (1.0f, 1.0f, 1.0f, 1.0f);
					GL11.glEnable (GL11.GL_TEXTURE_2D);

					// Icono
					Tile subTile = bottomSubPanelMenu.getItems ().get (iMenu).getIcon ();
					if (subTile != null) {
						GL11.glBindTexture (GL11.GL_TEXTURE_2D, subTile.getTextureID ());
						GL11.glTexEnvf (GL11.GL_TEXTURE_ENV, GL11.GL_TEXTURE_ENV_MODE, GL11.GL_MODULATE);
						UtilsGL.glBegin (GL11.GL_QUADS);
						UIPanel.drawTile (subTile, point, UIPanel.BOTTOM_ITEM_WIDTH, UIPanel.BOTTOM_ITEM_HEIGHT, (iItemBottomSubPanel == iMenu));
						UtilsGL.glEnd ();
					}
				}
			}
		}

		return iCurrentTexture;
	}

	public static boolean isMouseCloseToOpenCloseBottomIcon (int x, int y) {
		return UIPanel.isMouseCloseToIcon (x, y, tileOpenCloseBottomMenuPoint, tileOpenBottomMenu, UIPanel.CLOSE_PIXELS);
	}

	public static boolean isMouseOnBottomPanel (int x, int y) {
		if (y >= bottomPanelY && y < (bottomPanelY + BOTTOM_PANEL_HEIGHT)) {
			// Dentro del panel "virtual", miramos los paneles internos con sus transparencias

			if (x >= bottomPanelX && x < (bottomPanelX + BOTTOM_PANEL_WIDTH)) {
				return (!tileBottomPanelAlpha[x - bottomPanelX][y - bottomPanelY]);
			}
		}

		return false;
	}

	public static boolean isMouseOnBottomLeftScroll (int x, int y) {
		if ((y >= bottomPanelY && y < (bottomPanelY + BOTTOM_PANEL_HEIGHT)) && (x >= bottomPanelLeftScrollX && x < (bottomPanelLeftScrollX + BOTTOM_PANEL_SCROLL_WIDTH))) {
			return !tileBottomScrollLeftAlpha[x - bottomPanelLeftScrollX][y - bottomPanelY];
		}

		return false;
	}

	public static boolean isMouseOnBottomRightScroll (int x, int y) {
		if ((y >= bottomPanelY && y < (bottomPanelY + BOTTOM_PANEL_HEIGHT)) && (x >= bottomPanelRightScrollX && x < (bottomPanelRightScrollX + BOTTOM_PANEL_SCROLL_WIDTH))) {
			return !tileBottomScrollRightAlpha[x - bottomPanelRightScrollX][y - bottomPanelY];
		}

		return false;
	}

	public static int isMouseOnBottomItems (int x, int y) {
		if (y >= bottomPanelY && y < (bottomPanelY + BOTTOM_PANEL_HEIGHT)) {
			Point point;
			for (int i = 0; i < BOTTOM_PANEL_NUM_ITEMS; i++) {
				point = bottomPanelItemsPosition.get (i);
				if (x >= point.x && x < (point.x + UIPanel.BOTTOM_ITEM_WIDTH)) {
					if (!UIPanel.tileBottomItemAlpha[x - point.x][y - point.y]) {
						return i;
					}
				}
			}
		}

		return -1;
	}

	public static boolean isMouseOnBottomSubPanel (int x, int y) {
		if (x >= bottomSubPanelPoint.x && x < (bottomSubPanelPoint.x + BOTTOM_SUBPANEL_WIDTH) && y >= bottomSubPanelPoint.y && y < (bottomSubPanelPoint.y + BOTTOM_SUBPANEL_HEIGHT)) {
			return true;
		}

		return false;
	}

	public static int isMouseOnBottomSubItems (int x, int y) {
		if (bottomSubPanelMenu != null && y >= bottomSubPanelPoint.y && y < (bottomSubPanelPoint.y + BOTTOM_SUBPANEL_HEIGHT) && x >= bottomSubPanelPoint.x && x < (bottomSubPanelPoint.x + BOTTOM_SUBPANEL_WIDTH)) {
			Point point;
			bucle1: for (int y1 = 0; y1 < BOTTOM_SUBPANEL_NUM_ITEMS_Y; y1++) {
				for (int x1 = 0; x1 < BOTTOM_SUBPANEL_NUM_ITEMS_X; x1++) {
					int i = (y1 * BOTTOM_SUBPANEL_NUM_ITEMS_X) + x1;
					if (i >= bottomSubPanelMenu.getItems ().size ()) {
						break bucle1;
					}
					point = bottomSubPanelItemsPosition.get (i);
					if (x >= point.x && x < (point.x + BOTTOM_SUBITEM_WIDTH) && y >= point.y && y < (point.y + BOTTOM_SUBITEM_HEIGHT)) {
						if (!tileBottomSubItemAlpha[x - point.x][y - point.y]) {
							return i;
						}
					}
				}
			}
		}

		return -1;
	}

	public static void createBottomSubPanel (SmartMenu smItem) {
		int iMaxItems = smItem.getItems ().size ();
		BOTTOM_SUBPANEL_WIDTH = (RightMenuUIPanel.menuPanelPoint.x - bottomPanelX) - 2 * UIPanel.PIXELS_TO_BORDER;

		BOTTOM_SUBPANEL_NUM_ITEMS_X = (BOTTOM_SUBPANEL_WIDTH - UIPanel.PIXELS_TO_BORDER) / (BOTTOM_SUBITEM_WIDTH + UIPanel.PIXELS_TO_BORDER);
		if (BOTTOM_SUBPANEL_NUM_ITEMS_X < 1) {
			BOTTOM_SUBPANEL_NUM_ITEMS_X = 1;
		} else if (BOTTOM_SUBPANEL_NUM_ITEMS_X > iMaxItems) {
			BOTTOM_SUBPANEL_NUM_ITEMS_X = iMaxItems;
		}
		BOTTOM_SUBPANEL_WIDTH = BOTTOM_SUBPANEL_NUM_ITEMS_X * (BOTTOM_SUBITEM_WIDTH + UIPanel.PIXELS_TO_BORDER) + UIPanel.PIXELS_TO_BORDER;

		BOTTOM_SUBPANEL_NUM_ITEMS_Y = iMaxItems / BOTTOM_SUBPANEL_NUM_ITEMS_X;
		if (iMaxItems % BOTTOM_SUBPANEL_NUM_ITEMS_X != 0) {
			BOTTOM_SUBPANEL_NUM_ITEMS_Y++;
		}
		BOTTOM_SUBPANEL_HEIGHT = BOTTOM_SUBPANEL_NUM_ITEMS_Y * (BOTTOM_SUBITEM_HEIGHT + UIPanel.PIXELS_TO_BORDER) + UIPanel.PIXELS_TO_BORDER;

		bottomSubPanelPoint.setLocation (bottomPanelX, bottomPanelY - UIPanel.PIXELS_TO_BORDER - BOTTOM_SUBPANEL_HEIGHT);
		bottomSubPanelItemsPosition = new ArrayList<Point> ();
		bucle1: for (int y1 = 0; y1 < BOTTOM_SUBPANEL_NUM_ITEMS_Y; y1++) {
			for (int x1 = 0; x1 < BOTTOM_SUBPANEL_NUM_ITEMS_X; x1++) {
				if ((y1 * BOTTOM_SUBPANEL_NUM_ITEMS_X + x1) < smItem.getItems ().size ()) {
					bottomSubPanelItemsPosition.add (new Point (bottomSubPanelPoint.x + UIPanel.PIXELS_TO_BORDER + (x1 * (BOTTOM_SUBITEM_WIDTH + UIPanel.PIXELS_TO_BORDER)), bottomSubPanelPoint.y + UIPanel.PIXELS_TO_BORDER + (y1 * (BOTTOM_SUBITEM_HEIGHT + UIPanel.PIXELS_TO_BORDER))));
				} else {
					break bucle1;
				}
			}
		}

		ProductionUIPanel.createProductionPanel (ProductionUIPanel.productionPanelMenu);
	}

	public static boolean isBottomMenuPanelActive () {
		return BottomMenuUIPanel.bottomMenuPanelActive;
	}

	public static void setBottomMenuPanelActive (boolean bottomMenuPanelActive) {
		setBottomMenuPanelActive (bottomMenuPanelActive, false);
	}

	public static void setBottomMenuPanelActive (boolean bottomMenuPanelActive, boolean bInitializing) {
		BottomMenuUIPanel.bottomMenuPanelActive = bottomMenuPanelActive;
		if (!bInitializing) {
			ProductionUIPanel.createProductionPanel (ProductionUIPanel.productionPanelMenu);
		}
	}

	public static void setBottomMenuPanelLocked (boolean bottomMenuPanelLocked) {
		BottomMenuUIPanel.bottomMenuPanelLocked = bottomMenuPanelLocked;
	}

	public static boolean isBottomMenuPanelLocked () {
		return bottomMenuPanelLocked;
	}
}
