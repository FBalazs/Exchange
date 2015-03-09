package hu.berzsenyi.exchange.net.msg;

import hu.berzsenyi.exchange.Stock;
import hu.berzsenyi.exchange.Team;

import java.io.Serializable;
import java.util.List;

public class MsgConnAccept implements Serializable {
	private static final long serialVersionUID = -9078610768612892336L;
	
	public double teamMoney;
	public int[] teamStocks;
	public String[] stockNames;
	public double[] stockPrices;
	public String[] teamNames;
	
	public MsgConnAccept(double teamMoney, int[] teamStocks, List<Team> teams, Stock[] stocks) {
		this.teamMoney = teamMoney;
		this.teamStocks = teamStocks;
		stockNames = new String[stocks.length];
		stockPrices = new double[stockNames.length];
		for(int s = 0; s < stockNames.length; s++) {
			stockNames[s] = stocks[s].name;
			stockPrices[s] = stocks[s].value;
		}
		teamNames = new String[teams.size()];
		for(int t = 0; t < teamNames.length; t++)
			teamNames[t] = teams.get(t).name;
	}
}
