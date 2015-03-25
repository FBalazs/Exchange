package hu.berzsenyi.exchange.net.msg;

public class MsgClientOfferDeleteDirect extends Msg {
	private static final long serialVersionUID = 1089049179157795077L;
	
	public int stockId, amount;
	public double price;
	public boolean sell;
	public String player;
	
	public MsgClientOfferDeleteDirect(int stockId, int amount, double price, boolean sell, String player) {
		this.stockId = stockId;
		this.amount = amount;
		this.price = price;
		this.sell = sell;
		this.player = player;
	}
}
