package hu.berzsenyi.exchange.net.msg;

import hu.berzsenyi.exchange.Stock;
import hu.berzsenyi.exchange.Team;

import java.io.Serializable;
import java.util.List;

public class MsgConnAccept implements Serializable {
	private static final long serialVersionUID = -9078610768612892336L;
	
	public String[] stockNames;
	public double[] stockPrices;
	public String[] teamNames;
	
	public MsgConnAccept(List<Team> teams) {
		stockNames = new String[Stock.stockList.length];
		stockPrices = new double[stockNames.length];
		for(int s = 0; s < stockNames.length; s++) {
			stockNames[s] = Stock.stockList[s].name;
			stockPrices[s] = Stock.stockList[s].price;
		}
		teamNames = new String[teams.size()];
		for(int t = 0; t < teamNames.length; t++)
			teamNames[t] = teams.get(t).name;
	}
}
