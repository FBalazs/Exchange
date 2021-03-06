package hu.berzsenyi.exchange.server;

import java.awt.Color;
import java.awt.Graphics;

public class CompTeams extends DisplayComp {
	private static final long serialVersionUID = 1L;

	public CompTeams(ServerDisplay display) {
		super(display);
	}

	@Override
	public void paint(Graphics g) {
		super.paint(g);

		double maxMoney = 0;
		if (this.model.round != 0)
			for (int t = 0; t < this.model.teams.size(); t++)
				if (maxMoney < this.model.teams.get(t).getMoney()
						+ this.model.teams.get(t).getStockValue())
					maxMoney = this.model.teams.get(t).getMoney()
							+ this.model.teams.get(t).getStockValue();
		maxMoney *= 1.1;

		for (int t = 0; t < this.model.teams.size(); t++) {
			int x = this.getWidth() * t / this.model.teams.size();
			int w = this.getWidth() / this.model.teams.size() / 2;
			int h1 = this.model.round == 0 ? (int) (this.getHeight() / 1.1)
					: (int) (this.getHeight()
							* this.model.teams.get(t).getMoney() / maxMoney);
			int h2 = this.model.round == 0 ? 0 : (int) (this.getHeight()
					* this.model.teams.get(t).getStockValue() / maxMoney);

			g.setColor(new Color(0.5F, 1F, 0.75F));
			g.fillRect(x, this.getHeight() - h1, w, h1);
			g.setColor(new Color(0.5F, 0.75F, 1F));
			g.fillRect(x, this.getHeight() - h1 - h2, w, h2);

			g.setColor(Color.black);
			g.drawRect(x, this.getHeight() - h1 - h2, w, h1 + h2);

			drawStringCentered(
					this.model.teams.get(t).name,
					x + w / 2,
					this.getHeight() - h1 - h2 - g.getFontMetrics().getHeight(),
					g);
			drawStringCentered(
					ServerDisplay.DECIMAL_FORMAT.format(this.model.round == 0 ? this.model.startMoney
							: this.model.teams.get(t).getMoney()
									+ this.model.teams.get(t).getStockValue()),
					x + w / 2, this.getHeight() - h1 - h2, g);
		}

		g.setColor(Color.black);
		g.drawLine(0, this.getHeight() - 1, this.getWidth(),
				this.getHeight() - 1);
		g.drawLine(0, this.getHeight(), 0, 0);
	}
}