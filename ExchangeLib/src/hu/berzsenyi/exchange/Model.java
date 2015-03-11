package hu.berzsenyi.exchange;

import java.util.ArrayList;
import java.util.List;

public abstract class Model {
	public double startMoney = 10000;
	public Stock[] stocks = new Stock[0];
	public List<Team> teams = new ArrayList<Team>();


	public void nextRound(double[] multipliers) {
		for (int i = 0; i < multipliers.length; i++) {
			this.stocks[i].value *= multipliers[i];
			this.stocks[i].change = multipliers[i];
		}
	}

	public Team getTeamById(String id) {
		for (Team team : this.teams)
			if (id.equals(team.id))
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
			if (id.equals(teams.get(i).id))
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
