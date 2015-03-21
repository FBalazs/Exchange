package hu.berzsenyi.exchange;

public class Offer {
	public String sender, target;
	public int stockId, amount;
	public double price;
	public boolean sell;
	
	public Offer(String sender, String target, int stockId, int amount, double price, boolean sell) {
		this.sender = sender;
		this.target = target;
		this.stockId = stockId;
		this.amount = amount;
		this.price = price;
		this.sell = sell;
	}
}
