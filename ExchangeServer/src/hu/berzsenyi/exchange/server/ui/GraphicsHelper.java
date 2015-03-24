package hu.berzsenyi.exchange.server.ui;

import java.awt.Graphics2D;

public class GraphicsHelper {
	public static void drawStringCentered(Graphics2D g2, String str, int x, int y) {
		g2.drawString(str, x-g2.getFontMetrics().stringWidth(str)/2, y);
	}
}
