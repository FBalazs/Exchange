package hu.berzsenyi.exchange.net.cmd;

import java.io.DataInputStream;
import java.io.DataOutputStream;

public class CmdOfferResponse extends TCPCommand {
	public static final int ID = 6;
	
	public String playerID;
	public int stockID, amount;
	public double money;
	
	public CmdOfferResponse(int length) {
		super(ID, length);
	}
	
	public CmdOfferResponse(String playerID, int stockID, int amount, double money) {
		super(ID, 4+stringLength(playerID)+4+4+8);
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
