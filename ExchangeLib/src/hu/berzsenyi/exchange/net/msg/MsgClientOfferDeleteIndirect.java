package hu.berzsenyi.exchange.net.msg;

public class MsgClientOfferDeleteIndirect extends Msg {
	private static final long serialVersionUID = 5136148372253596950L;
	
	public int stockId, amount;
	public double price;
	public boolean sell;
	
	public MsgClientOfferDeleteIndirect(int stockId, int amount, double price, boolean sell) {
		this.stockId = stockId;
		this.amount = amount;
		this.price = price;
		this.sell = sell;
	}
}
