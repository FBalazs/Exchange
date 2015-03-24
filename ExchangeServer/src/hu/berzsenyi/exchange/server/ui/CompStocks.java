package hu.berzsenyi.exchange.server.ui;

import hu.berzsenyi.exchange.server.game.ServerExchange;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;

public class CompStocks extends Component {
	private static final long serialVersionUID = 3320516758880742920L;
	
	@Override
	public void paint(Graphics g) {
		super.paint(g);
		Graphics2D g2 = (Graphics2D)g;
		
		double maxPrice = 0;
		for (int s = 0; s < ServerExchange.INSTANCE.getStockNumber(); s++)
			if (maxPrice < ServerExchange.INSTANCE.getStock(s).getPrice())
				maxPrice = ServerExchange.INSTANCE.getStock(s).getPrice();
		maxPrice *= 1.1;
		
		g2.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
		
		int[] theights = new int[ServerExchange.INSTANCE.getStockNumber()];
		for(int s = 0; s < theights.length; s++) {
			theights[s] = (int) (this.getHeight() * ServerExchange.INSTANCE.getStock(s).getPrice() / maxPrice);
			if(0 < s && Math.abs(theights[s-1]-theights[s]) < g2.getFont().getSize()*2) {
				theights[s] = theights[s-1]+g2.getFont().getSize()*2;
			}
		}

		for (int s = 0; s < theights.length; s++) {
			int x = this.getWidth() * s / ServerExchange.INSTANCE.getStockNumber();
			int w = this.getWidth() / ServerExchange.INSTANCE.getStockNumber() / 2;
			int h = (int) (this.getHeight() * ServerExchange.INSTANCE.getStock(s).getPrice() / maxPrice);

			g2.setColor(new Color(0.5F, 0.75F, 1F));
			g2.fillRect(x, this.getHeight() - h, w, h);

			g2.setColor(Color.black);
			g2.drawRect(x, this.getHeight() - h, w, h);
		}
		
		for (int s = 0; s < theights.length; s++) {
			int x = this.getWidth() * s / ServerExchange.INSTANCE.getStockNumber();
			int w = this.getWidth() / ServerExchange.INSTANCE.getStockNumber() / 2;
			
			drawStringCentered(ServerExchange.INSTANCE.getStock(s).getName(), x + w / 2,
					this.getHeight() - theights[s] - g2.getFontMetrics().getHeight(), g2);
			drawStringCentered(
					/*ServerDisplay.DECIMAL_FORMAT
							.format(this.model.stocks[s].value)*/ServerExchange.INSTANCE.getStock(s).getPrice(),
					x + w / 2, this.getHeight() - theights[s], g2);
		}

		g2.setColor(Color.black);
		g2.drawLine(0, this.getHeight() - 1, this.getWidth(),
				this.getHeight() - 1);
		g2.drawLine(0, this.getHeight(), 0, 0);
	}
}
