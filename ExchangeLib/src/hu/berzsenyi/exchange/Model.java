package hu.berzsenyi.exchange;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public abstract class Model {
	public int round = 0;
	public double startMoney = 10000;
	public Stock[] stocks = new Stock[0];
	public List<Team> teams = new ArrayList<Team>();

	/**
	 * Loads the stocks from the data files.
	 * 
	 * @param stockFolder
	 *            The folder where the files are located.
	 */
	public void loadStocks(String stockFolder) {
		File[] files = new File(stockFolder).listFiles();
		this.stocks = new Stock[files.length];
		for (int i = 0; i < files.length; i++) {
			try {
				DatParser parser = new DatParser(files[i].getAbsolutePath());
				parser.parse();
				this.stocks[i] = new Stock(files[i].getName().substring(0,
						files[i].getName().lastIndexOf('.')),
						parser.getValue("name"), Double.parseDouble(parser
								.getValue("initvalue")));
				this.stocks[i].sellOffers = new ArrayList<>();
				this.stocks[i].buyOffers = new ArrayList<>();
			} catch (Exception e) {
				e.printStackTrace();
				System.err.println("Failed to parse stock: "
						+ files[i].getName());
			}
		}
	}

	public void nextRound(double[] multipliers) {
		for (int i = 0; i < multipliers.length; i++) {
			this.stocks[i].value *= multipliers[i];
			this.stocks[i].change = multipliers[i];
		}
	}

	public Team getTeamById(String id) {
		for (Team team : this.teams)
			if (team.id.equals(id))
				return team;
		return null;
	}

	public Team getTeamByName(String name) {
		for (Team team : this.teams)
			if (team.name.equals(name))
				return team;
		return null;
	}

	public void removeTeam(String id) {
		for (int i = 0; i < this.teams.size(); i++)
			if (this.teams.get(i).id.equals(id))
				this.teams.remove(i--);
	}

	/**
	 * Only in the zeroth round!
	 * 
	 * @param amounts
	 * @return
	 */
	public double calculateMoneyAfterPurchase(int[] amounts) {
		double sum = 0.0;
		for (int i = 0; i < amounts.length; i++)
			sum += amounts[i] * stocks[i].value;
		return this.startMoney - sum;
	}
}
