package hu.berzsenyi.exchange.net.msg;

import hu.berzsenyi.exchange.game.Stock;

public class MsgServerConnAccept extends Msg {
	private static final long serialVersionUID = 5764574461133362668L;
	
	public int gameMode;
	public String[] stockNames;
	public double[] stockPrices;
	public int[] stocksIngame;
	public double playerMoney;
	public int[] playerStocks;
	
	public MsgServerConnAccept(int gameMode, Stock[] stocks, double playerMoney, int[] playerStocks) {
		this.gameMode = gameMode;
		stockNames = new String[stocks.length];
		stockPrices = new double[stocks.length];
		stocksIngame = new int[stocks.length];
		for(int i = 0; i < stocks.length; i++) {
			stockNames[i] = stocks[i].getName();
			stockPrices[i] = stocks[i].getPrice();
			stocksIngame[i] = stocks[i].getIngame();
		}
		this.playerMoney = playerMoney;
		this.playerStocks = playerStocks;
	}
}
