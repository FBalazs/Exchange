package hu.berzsenyi.exchange.server;

import java.util.Vector;

import hu.berzsenyi.exchange.Offer;
import hu.berzsenyi.exchange.Stock;

public class StockServer extends Stock {
	protected final String id;
	protected long tradeAmount;
	protected double tradeMoney;
	protected Vector<Offer> offers;
	
	public StockServer(String id, String name, double price) {
		super(name, price);
		this.id = id;
		tradeAmount = 10;
		tradeMoney = tradeAmount*price;
	}
	
	public void updatePrice(double multiplier) {
		price = (price*multiplier + tradeMoney/tradeAmount)/2;
	}
}
