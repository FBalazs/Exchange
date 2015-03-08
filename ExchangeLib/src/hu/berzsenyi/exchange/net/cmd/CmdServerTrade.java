package hu.berzsenyi.exchange.net.cmd;

public class CmdServerTrade extends TCPCommand {

	private static final long serialVersionUID = -3599113212175689096L;
	public int stockID, amount;
	public double price;

	public CmdServerTrade(int stockID, int amount, double price) {
		this.stockID = stockID;
		this.amount = amount;
		this.price = price;
	}

}
