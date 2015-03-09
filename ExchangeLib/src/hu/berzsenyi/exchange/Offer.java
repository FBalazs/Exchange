package hu.berzsenyi.exchange;

public class Offer {
	public String clientName;
	public int amount;
	public double money;
	
	public Offer(String clientName, int amount, double money) {
		this.clientName = clientName;
		this.amount = amount;
		this.money = money;
	}
}
