package hu.berzsenyi.exchange.game;

public class Stock {
	private String name;
	private double price;
	
	public Stock(String name, double price) {
		this.name = name;
		this.price = price;
	}
	
	public String getName() {
		return name;
	}
	
	public double getPrice() {
		return price;
	}
}
