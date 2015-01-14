package hu.berzsenyi.exchange.server;

import java.awt.Color;
import java.awt.Graphics;

public class CompStocks extends DisplayComp {
	private static final long serialVersionUID = 1L;

	public CompStocks(ServerDisplay display) {
		super(display);
	}

	@Override
	public void paint(Graphics g) {
		super.paint(g);
		
		double maxPrice = 0;
		for(int s = 0; s < this.model.stockList.length; s++)
			if(maxPrice < this.model.stockList[s].value)
				maxPrice = this.model.stockList[s].value;
		maxPrice *= 1.1;
		
		for(int s = 0; s < this.model.stockList.length; s++) {
			int x = this.getWidth()*s/this.model.stockList.length;
			int w = this.getWidth()/this.model.stockList.length/2;
			int h = (int) (this.getHeight()*this.model.stockList[s].value/maxPrice);
			
			g.setColor(new Color(0.5F, 0.75F, 1F));
			g.fillRect(x, this.getHeight()-h, w, h);
			
			g.setColor(Color.black);
			g.drawRect(x, this.getHeight()-h, w, h);
			
			drawStringCentered(this.model.stockList[s].name, x+w/2, this.getHeight()-h-w/10-g.getFontMetrics().getHeight(), g);
			drawStringCentered(""+this.model.stockList[s].value, x+w/2, this.getHeight()-h-w/10, g);
		}
		
		g.setColor(Color.black);
		g.drawLine(0, this.getHeight()-1, this.getWidth(), this.getHeight()-1);
		g.drawLine(0, this.getHeight(), 0, 0);
	}
}