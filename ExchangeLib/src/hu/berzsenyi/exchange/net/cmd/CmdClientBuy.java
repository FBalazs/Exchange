package hu.berzsenyi.exchange.net.cmd;

import java.io.DataInputStream;
import java.io.DataOutputStream;

public class CmdClientBuy extends TCPCommand {
	public static final int ID = 7;
	
	public int stockID, amount;
	
	public CmdClientBuy(int length) {
		super(ID, length);
	}
	
	public CmdClientBuy(int stockID, int amount) {
		super(ID, 4+4);
		this.stockID = stockID;
		this.amount = amount;
	}
	
	@Override
	public void read(DataInputStream in) throws Exception {
		this.stockID = in.readInt();
		this.amount = in.readInt();
	}
	
	@Override
	public void write(DataOutputStream out) throws Exception {
		out.writeInt(this.stockID);
		out.writeInt(this.amount);
	}
}
