package hu.berzsenyi.exchange.net.cmd;

import java.io.DataInputStream;
import java.io.DataOutputStream;

public class CmdOffer extends TCPCommand {
	public static final int ID = 5;
	
	public String playerID;
	public int stockID, amount;
	public double money;
	
	public CmdOffer(int length) {
		super(ID, length);
	}
	
	public CmdOffer(String playerID, int stockID, int amount, double money) {
		super(ID, 4+playerID.length()+4+4+8);
		this.playerID = playerID;
		this.stockID = stockID;
		this.amount = amount;
		this.money = money;
	}
	
	@Override
	public void read(DataInputStream in) throws Exception {
		this.playerID = readString(in);
		this.stockID = in.readInt();
		this.amount = in.readInt();
		this.money = in.readDouble();
	}
	
	@Override
	public void write(DataOutputStream out) throws Exception {
		writeString(out, this.playerID);
		out.writeInt(this.stockID);
		out.writeInt(this.amount);
		out.writeDouble(this.money);
	}
}
