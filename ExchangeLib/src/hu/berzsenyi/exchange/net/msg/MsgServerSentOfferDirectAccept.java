package hu.berzsenyi.exchange.net.msg;

public class MsgServerSentOfferDirectAccept extends Msg {
	private static final long serialVersionUID = 4296816089805808071L;
	
	public String target;
	public int stockId, amount;
	public double price;
	public boolean sell;
	
	public MsgServerSentOfferDirectAccept(String target, int stockId, int amount, double price, boolean sell) {
		this.target = target;
		this.stockId = stockId;
		this.amount = amount;
		this.price = price;
		this.sell = sell;
	}
}
