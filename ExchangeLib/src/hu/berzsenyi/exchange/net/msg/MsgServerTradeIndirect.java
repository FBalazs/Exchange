package hu.berzsenyi.exchange.net.msg;

public class MsgServerTradeIndirect extends Msg {
	private static final long serialVersionUID = -6097789772732691333L;

	public int stockId, amount;
	public double price;
	public boolean sell;
	
	public MsgServerTradeIndirect(int stockId, int amount, double price, boolean sell) {
		this.stockId = stockId;
		this.amount = amount;
		this.price = price;
		this.sell = sell;
	}
}
