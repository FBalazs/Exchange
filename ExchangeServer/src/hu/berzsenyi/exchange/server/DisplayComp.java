package hu.berzsenyi.exchange.server;

import hu.berzsenyi.exchange.Model;

import java.awt.Component;
import java.awt.Graphics;

public class DisplayComp extends Component {
	private static final long serialVersionUID = 1L;
	
	public static void drawStringCentered(String str, int x, int y, Graphics g) {
		g.drawString(str, x-g.getFontMetrics().stringWidth(str)/2, y);
	}
	
	public ServerDisplay display;
	public Model model;
	
	public DisplayComp(ServerDisplay display) {
		this.display = display;
		this.model = display.server.model;
	}
	
	@Override
	public void paint(Graphics g) {
		super.paint(g);
		
		if(this.model == null) {
			this.model = this.display.server.model;
			if(this.model == null)
				return;
		}
	}
}