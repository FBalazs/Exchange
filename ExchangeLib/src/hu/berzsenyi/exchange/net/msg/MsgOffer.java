package hu.berzsenyi.exchange.net.msg;


public class MsgOffer extends Msg {
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
