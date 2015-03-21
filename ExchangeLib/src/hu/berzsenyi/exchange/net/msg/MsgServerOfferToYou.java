package hu.berzsenyi.exchange.net.msg;

public class MsgServerOfferToYou extends Msg {
	private static final long serialVersionUID = 7939992137068002322L;
	
	public String sender;
	public int stockId, amount;
	public double price;
	public boolean sell;
	
	public MsgServerOfferToYou(String sender, int stockId, int amount, double price, boolean sell) {
		this.sender = sender;
		this.stockId = stockId;
		this.amount = amount;
		this.price = price;
		this.sell = sell;
	}
}
