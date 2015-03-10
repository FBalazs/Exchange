package hu.berzsenyi.exchange;

import java.io.Serializable;

public class Offer implements Serializable {
	public String clientName;
	public int amount;
	public double money;
	
	public Offer(String clientName, int amount, double money) {
		this.clientName = clientName;
		this.amount = amount;
		this.money = money;
	}
}
