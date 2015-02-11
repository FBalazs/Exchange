package hu.berzsenyi.exchange;

public class Offer {
	public String clientID;
	public int amount;
	public double money;
	
	public Offer(String clientID, int amount, double money) {
		this.clientID = clientID;
		this.amount = amount;
		this.money = money;
	}
}
