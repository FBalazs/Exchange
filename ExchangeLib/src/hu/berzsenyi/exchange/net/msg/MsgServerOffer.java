package hu.berzsenyi.exchange.net.msg;

public class MsgServerOffer extends Msg {
	private static final long serialVersionUID = -8408437140436952502L;
	
	public int stockId, amount;
	public double price;
	public boolean sell;
	
	public MsgServerOffer(int stockId, int amount, double price, boolean sell) {
		this.stockId = stockId;
		this.amount = amount;
		this.price = price;
		this.sell = sell;
	}
}
