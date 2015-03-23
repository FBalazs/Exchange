package hu.berzsenyi.exchange.net.msg;

public class MsgServerSentOfferIndirectAccept extends Msg {
	private static final long serialVersionUID = 4296816089805808071L;
	
	public int stockId, amount;
	public double price;
	public boolean sell;
	
	public MsgServerSentOfferIndirectAccept(int stockId, int amount, double price, boolean sell) {
		this.stockId = stockId;
		this.amount = amount;
		this.price = price;
		this.sell = sell;
	}
}
