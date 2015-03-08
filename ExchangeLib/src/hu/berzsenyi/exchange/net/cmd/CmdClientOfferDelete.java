package hu.berzsenyi.exchange.net.cmd;


public class CmdClientOfferDelete extends TCPCommand {
	private static final long serialVersionUID = 3661833059262398986L;

	public int stockID, amount;
	public double price;


	public CmdClientOfferDelete(int stockID, int amount, double price, boolean sell) {
		this.stockID = stockID;
		this.amount = amount;
		this.price = sell ? price : -price;
	}


}
