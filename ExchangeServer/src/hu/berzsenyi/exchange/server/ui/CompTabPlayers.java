package hu.berzsenyi.exchange.server.ui;

import hu.berzsenyi.exchange.server.game.ServerExchange;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;

public class CompTabPlayers extends Component {
	private static final long serialVersionUID = -1688325221760767894L;

	@Override
	public void paint(Graphics g) {
		super.paint(g);
		Graphics2D g2 = (Graphics2D)g;
		try {
			double maxMoney = 0;
			for (int p = 0; p < ServerExchange.INSTANCE.getPlayerNumber(); p++)
				if (maxMoney < ServerExchange.INSTANCE.getPlayer(p).getMoney()
						+ ServerExchange.INSTANCE.getPlayer(p).getStocksValue())
					maxMoney = ServerExchange.INSTANCE.getPlayer(p).getMoney()
							+ ServerExchange.INSTANCE.getPlayer(p).getStocksValue();
			maxMoney *= 1.1;

			for (int p = 0; p < ServerExchange.INSTANCE.getPlayerNumber(); p++) {
				int w = this.getWidth() / (ServerExchange.INSTANCE.getPlayerNumber()+1) / 2;
				int x = (int)(this.getWidth() * (p+0.5) / (ServerExchange.INSTANCE.getPlayerNumber()+1));
				int h1 = !ServerExchange.INSTANCE.isStarted() ? (int) (this.getHeight() / 1.1)
						: (int) (this.getHeight()
								* ServerExchange.INSTANCE.getPlayer(p).getMoney() / maxMoney);
				int h2 = !ServerExchange.INSTANCE.isStarted() ? 0 : (int) (this.getHeight()
						* ServerExchange.INSTANCE.getPlayer(p).getStocksValue() / maxMoney);

				g2.setColor(new Color(0.5F, 1F, 0.75F));
				g2.fillRect(x, this.getHeight() - h1, w, h1);
				g2.setColor(new Color(0.5F, 0.75F, 1F));
				g2.fillRect(x, this.getHeight() - h1 - h2, w, h2);

				g2.setColor(Color.black);
				g2.drawRect(x, this.getHeight() - h1 - h2, w, h1 + h2);

				GraphicsHelper.drawStringCentered(g2, 
						ServerExchange.INSTANCE.getPlayer(p).name,
						x + w / 2,
						this.getHeight() - h1 - h2 - g.getFontMetrics().getHeight());
				GraphicsHelper.drawStringCentered(g2, ""+ServerExchange.INSTANCE.getPlayer(p).getMoney() + ServerExchange.INSTANCE.getPlayer(p).getStocksValue(),
						x + w / 2, this.getHeight() - h1 - h2);
			}

			g2.setColor(Color.black);
			g2.drawLine(0, this.getHeight() - 1, this.getWidth(),
					this.getHeight() - 1);
			g2.drawLine(0, this.getHeight(), 0, 0);
		} catch(Exception e) {
			//e.printStackTrace();
		}
	}
}
