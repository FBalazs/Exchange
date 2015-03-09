package hu.berzsenyi.exchange.net.msg;

import java.io.Serializable;

public class MsgOfferDelete implements Serializable {
	private static final long serialVersionUID = 4427033718069601790L;
	
	public int stockId, stockAmount;
	public double price;
	public boolean sell;
	
	public MsgOfferDelete(int stockId, int amount, double price, boolean sell) {
		this.stockId = stockId;
		stockAmount = amount;
		this.price = price;
		this.sell = sell;
	}
}
