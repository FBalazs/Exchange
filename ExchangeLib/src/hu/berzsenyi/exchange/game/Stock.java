package hu.berzsenyi.exchange.game;

import java.util.LinkedList;

public class Stock {
	protected final String name;
	protected double price, minSellOffer, maxBuyOffer;
	protected int ingame;
	protected LinkedList<Double> oldPrices;
	
	public Stock(String name, double price) {
		this.name = name;
		this.price = price;
		minSellOffer = maxBuyOffer = -1;
		ingame = 0;
		oldPrices = new LinkedList<>();
	}
	
	public String getName() {
		return name;
	}
	
	public double getPrice() {
		return price;
	}
	
	public void setPrice(double price) {
		this.price = price;
	}
	
	public double getMinSellOffer() {
		return minSellOffer;
	}
	
	public double getMaxBuyOffer() {
		return maxBuyOffer;
	}
	
	public void setMinMaxOffers(double minSell, double maxBuy) {
		minSellOffer = minSell;
		maxBuyOffer = maxBuy;
	}
	
	public int getIngame() {
		return ingame;
	}
	
	public void setIngame(int value) {
		ingame = value;
	}
	
	public LinkedList<Double> getOldPrices() {
		return oldPrices;
	}
	
	public void savePrice() {
		oldPrices.add(price);
	}
}
