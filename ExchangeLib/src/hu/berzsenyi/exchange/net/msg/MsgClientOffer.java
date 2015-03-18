package hu.berzsenyi.exchange.net.msg;

public class MsgClientOffer extends Msg {
	private static final long serialVersionUID = 7446986771844592428L;
	
	public int stockId, amount;
	public double price;
	public boolean sell;
	
	public MsgClientOffer(int stockId, int amount, double price, boolean sell) {
		this.stockId = stockId;
		this.amount = amount;
		this.price = price;
		this.sell = sell;
	}
}
