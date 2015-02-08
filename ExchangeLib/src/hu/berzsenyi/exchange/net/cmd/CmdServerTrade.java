package hu.berzsenyi.exchange.net.cmd;

import java.io.DataInputStream;
import java.io.DataOutputStream;

public class CmdServerTrade extends TCPCommand {
	public static final int ID = 6;

	public int stockID, amount;
	public double price;

	public CmdServerTrade(int length) {
		super(ID, length);
	}

	public CmdServerTrade(int stockID, int amount, double price) {
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
