package hu.berzsenyi.exchange.net.msg;

import hu.berzsenyi.exchange.Stock;
import hu.berzsenyi.exchange.Team;

import java.util.List;

public class MsgConnAccept extends Msg {
	private static final long serialVersionUID = -9078610768612892336L;
	
	public double teamMoney;
	public int[] teamStocks;
	public Stock[] stocks;
	public String[] teamNames;
	public boolean zerothRound;
	
	public MsgConnAccept(double teamMoney, int[] teamStocks, List<Team> teams, Stock[] stocks, boolean firstRound) {
		this.teamMoney = teamMoney;
		this.teamStocks = teamStocks;
		this.stocks = stocks;
		this.zerothRound = firstRound;
		teamNames = new String[teams.size()];
		for(int t = 0; t < teamNames.length; t++)
			teamNames[t] = teams.get(t).name;
	}
}
