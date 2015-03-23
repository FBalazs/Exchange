package hu.berzsenyi.exchange.net.msg;

public class MsgServerTradeDirect extends Msg {
	private static final long serialVersionUID = 7777321721312722890L;
	
	public String partner;
	public int stockId, amount;
	public double price;
	public boolean sell;
	
	public MsgServerTradeDirect(String partner, int stockId, int amount, double price, boolean sell) {
		this.partner = partner;
		this.stockId = stockId;
		this.amount = amount;
		this.price = price;
		this.sell = sell;
	}
}
