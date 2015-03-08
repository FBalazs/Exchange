package hu.berzsenyi.exchange;

public class Offer implements Comparable<Offer> {
	private String teamName;
	private int stockId, amount;
	/**
	 * The price on which the team wants to trade with the stocks. Negative if this is a sell offer.
	 */
	private double price;
	
	public Offer(String teamName, int stockId, int amount, double price, boolean sell) {
		this.teamName = teamName;
		this.stockId = stockId;
		this.amount = amount;
		this.price = price;
		if(sell)
			this.price *= -1;
	}
	
	public String getTeamName() {
		return teamName;
	}
	
	public int getStockId() {
		return stockId;
	}
	
	public int getStockAmount() {
		return amount;
	}
	
	public double getPrice() {
		return Math.abs(price);
	}
	
	@Override
	public int compareTo(Offer o) {
		return (int)Math.signum(price-o.price);
	}
}
