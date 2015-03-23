package hu.berzsenyi.exchange;

public class Stock {
	protected final String name;
	protected double price, minSellOffer, maxBuyOffer;
	
	public Stock(String name, double price) {
		this.name = name;
		this.price = price;
		minSellOffer = maxBuyOffer = -1;
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
}
