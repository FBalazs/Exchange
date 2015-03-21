package hu.berzsenyi.exchange.server.game;

import hu.berzsenyi.exchange.game.Stock;

public class ServerStock extends Stock {
	public String id;
	private long tradeAmount;
	private double tradeMoney;
	
	public ServerStock(String id, String name, double price) {
		super(name, price);
		this.id = id;
		tradeAmount = 10;
		tradeMoney = tradeAmount*price;
	}
	
	public void updatePrice(double multiplier) {
		price = (price*multiplier + tradeMoney/tradeAmount)/2;
	}
}
