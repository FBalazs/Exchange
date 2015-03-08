package hu.berzsenyi.exchange.net.cmd;

public class CmdClientOffer extends TCPCommand {

	private static final long serialVersionUID = 375228284809078656L;
	public int stockID, amount;
	public double price; // TODO Should be positive, and there should be a
							// boolean indicating whether is it a sell or buy
							// type offer


	public CmdClientOffer(int stockID, int amount, double price, boolean sell) {
		this.stockID = stockID;
		this.amount = amount;
		this.price = sell ? price : -price;
	}


}
