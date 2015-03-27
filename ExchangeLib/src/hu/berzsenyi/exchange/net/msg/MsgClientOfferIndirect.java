package hu.berzsenyi.exchange.net.msg;

public class MsgClientOfferIndirect extends Msg {
	private static final long serialVersionUID = 7446986771844592428L;
	
	public int stockId, amount;
	public double price;
	public boolean sell;
	
	public MsgClientOfferIndirect(int stockId, int amount, double price, boolean sell) {
		this.stockId = stockId;
		this.amount = amount;
		this.price = price;
		this.sell = sell;
	}
}
