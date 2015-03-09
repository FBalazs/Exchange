package hu.berzsenyi.exchange.net.msg;

import java.io.Serializable;

public class MsgOffer implements Serializable {
	private static final long serialVersionUID = -6028442585288832957L;
	
	public int stockId, stockAmount;
	public double price;
	public boolean sell;
	
	public MsgOffer(int stockId, int amount, double price, boolean sell) {
		this.stockId = stockId;
		stockAmount = amount;
		this.price = price;
		this.sell = sell;
	}
}
