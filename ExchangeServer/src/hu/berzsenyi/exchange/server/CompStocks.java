package hu.berzsenyi.exchange.server;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;

public class CompStocks extends DisplayComp {
	private static final long serialVersionUID = 1L;

	public CompStocks(ServerDisplay display) {
		super(display);
	}

	@Override
	public void paint(Graphics g) {
		super.paint(g);
		Graphics2D g2 = (Graphics2D)g;

		double maxPrice = 0;
		for (int s = 0; s < this.model.stocks.length; s++)
			if (maxPrice < this.model.stocks[s].value)
				maxPrice = this.model.stocks[s].value;
		maxPrice *= 1.1;
		
		g2.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
		
		int[] theights = new int[this.model.stocks.length];
		for(int s = 0; s < theights.length; s++) {
			theights[s] = (int) (this.getHeight() * this.model.stocks[s].value / maxPrice);
			if(0 < s && Math.abs(theights[s-1]-theights[s]) < g2.getFont().getSize()*2) {
				theights[s] = theights[s-1]+g2.getFont().getSize()*2;
			}
		}

		for (int s = 0; s < theights.length; s++) {
			int x = this.getWidth() * s / this.model.stocks.length;
			int w = this.getWidth() / this.model.stocks.length / 2;
			int h = (int) (this.getHeight() * this.model.stocks[s].value / maxPrice);

			g2.setColor(new Color(0.5F, 0.75F, 1F));
			g2.fillRect(x, this.getHeight() - h, w, h);

			g2.setColor(Color.black);
			g2.drawRect(x, this.getHeight() - h, w, h);
		}
		
		for (int s = 0; s < theights.length; s++) {
			int x = this.getWidth() * s / this.model.stocks.length;
			int w = this.getWidth() / this.model.stocks.length / 2;
			
			drawStringCentered(this.model.stocks[s].name, x + w / 2,
					this.getHeight() - theights[s] - g2.getFontMetrics().getHeight(), g2);
			drawStringCentered(
					ServerDisplay.DECIMAL_FORMAT
							.format(this.model.stocks[s].value),
					x + w / 2, this.getHeight() - theights[s], g2);
		}

		g2.setColor(Color.black);
		g2.drawLine(0, this.getHeight() - 1, this.getWidth(),
				this.getHeight() - 1);
		g2.drawLine(0, this.getHeight(), 0, 0);
	}
}