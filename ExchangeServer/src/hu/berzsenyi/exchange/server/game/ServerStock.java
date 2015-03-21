package hu.berzsenyi.exchange.server.game;

import java.util.Vector;

import hu.berzsenyi.exchange.Offer;
import hu.berzsenyi.exchange.game.Stock;

public class ServerStock extends Stock {
	public String id;
	private long tradeAmount;
	private double tradeMoney;
	private Vector<Offer> offers;
	
	public ServerStock(String id, String name, double price) {
		super(name, price);
		this.id = id;
		tradeAmount = 10;
		tradeMoney = tradeAmount*price;
	}
	
	public void updatePrice(double multiplier) {
		setPrice((getPrice()*multiplier + tradeMoney/tradeAmount)/2);
	}
}
