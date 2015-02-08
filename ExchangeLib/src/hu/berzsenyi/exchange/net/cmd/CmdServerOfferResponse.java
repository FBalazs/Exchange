package hu.berzsenyi.exchange.net.cmd;

import java.io.DataInputStream;
import java.io.DataOutputStream;

public class CmdServerOfferResponse extends TCPCommand {
	public static final int ID = 36;

	public int stockID, amount;
	public double price;

	public CmdServerOfferResponse(int length) {
		super(ID, length);
	}

	public CmdServerOfferResponse(int stockID, int amount, double price) {
		super(ID, 4 + 4 + 8);
		this.stockID = stockID;
		this.amount = amount;
		this.price = price;
	}

	@Override
	public void read(DataInputStream in) throws Exception {
		this.stockID = in.readInt();
		this.amount = in.readInt();
		this.price = in.readDouble();
	}

	@Override
	public void write(DataOutputStream out) throws Exception {
		out.writeInt(this.stockID);
		out.writeInt(this.amount);
		out.writeDouble(this.price);
	}

}
