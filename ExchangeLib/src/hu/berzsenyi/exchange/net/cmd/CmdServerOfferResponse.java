package hu.berzsenyi.exchange.net.cmd;

public class CmdServerOfferResponse extends TCPCommand {
	private static final long serialVersionUID = -5989913874150497098L;

	public int stockID, amount;
	public double price;

	public CmdServerOfferResponse(int stockID, int amount, double price) {
		this.stockID = stockID;
		this.amount = amount;
		this.price = price;
	}

}
