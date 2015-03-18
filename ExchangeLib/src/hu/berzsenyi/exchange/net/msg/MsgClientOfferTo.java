package hu.berzsenyi.exchange.net.msg;

public class MsgClientOfferTo extends Msg {
	private static final long serialVersionUID = 5975937614374439452L;
	
	public int stockId, amount;
	public double price;
	public boolean sell;
	public String player;
	
	public MsgClientOfferTo(int stockId, int amount, double price, boolean sell, String player) {
		this.stockId = stockId;
		this.amount = amount;
		this.price = price;
		this.sell = sell;
		this.player = player;
	}
}
