package hu.berzsenyi.exchange;

public class Offer implements Comparable<Offer> {
	public String teamName;
	public int stockId, amount;
	public double price;
	public boolean sell;
	
	public Offer(String teamName, int stockId, int amount, double price, boolean sell) {
		this.teamName = teamName;
		this.stockId = stockId;
		this.amount = amount;
		this.price = price;
		this.sell = sell;
	}
	
	@Override
	public int compareTo(Offer o) {
		if(sell)
			return (int)Math.signum(o.price-price);
		else
			return (int)Math.signum(price-o.price);
	}
}
