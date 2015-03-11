package hu.berzsenyi.exchange.net.msg;

import hu.berzsenyi.exchange.Stock;
import hu.berzsenyi.exchange.Team;

import java.util.List;

public class MsgConnAccept extends Msg {
	private static final long serialVersionUID = -9078610768612892336L;
	
	public double teamMoney;
	public int[] teamStocks;
	public String[] stockNames;
	public double[] stockValues;
	public int[] stockCirc;
	public String[] teamNames;
	public boolean zerothRound;
	
	public MsgConnAccept(double teamMoney, int[] teamStocks, List<Team> teams, Stock[] stocks, boolean firstRound) {
		this.teamMoney = teamMoney;
		this.teamStocks = teamStocks;
		stockNames = new String[stocks.length];
		stockValues = new double[stocks.length];
		stockCirc = new int[stocks.length];
		for(int s = 0; s < stocks.length; s++) {
			stockNames[s] = stocks[s].name;
			stockValues[s] = stocks[s].value;
			stockCirc[s] = stocks[s].circulated;
		}
		zerothRound = firstRound;
		teamNames = new String[teams.size()];
		for(int t = 0; t < teamNames.length; t++)
			teamNames[t] = teams.get(t).name;
	}
}
