package hu.berzsenyi.exchange.net.msg;


public class MsgOffer extends Msg {
	private static final long serialVersionUID = -8386984689320068766L;
	
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
