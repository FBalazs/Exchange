package hu.berzsenyi.exchange.net.cmd;

import java.io.DataInputStream;
import java.io.DataOutputStream;

public class CmdOfferResponse extends TCPCommand {
	public static final int ID = 5;
	
	public String senderID, receiverID;
	public int stockID, amount;
	public double money;
	
	public CmdOfferResponse(int length) {
		super(ID, length);
	}
	
	public CmdOfferResponse(String senderID, String receiverID, int stockID, int amount, double money) {
		super(ID, 4+senderID.length()+4+receiverID.length()+4+4+8);
		this.senderID = senderID;
		this.receiverID = receiverID;
		this.stockID = stockID;
		this.amount = amount;
		this.money = money;
	}
	
	@Override
	public void read(DataInputStream in) throws Exception {
		this.senderID = readString(in);
		this.receiverID = readString(in);
		this.stockID = in.readInt();
		this.amount = in.readInt();
		this.money = in.readDouble();
	}
	
	@Override
	public void write(DataOutputStream out) throws Exception {
		writeString(out, this.senderID);
		writeString(out, this.receiverID);
		out.writeInt(this.stockID);
		out.writeInt(this.amount);
		out.writeDouble(this.money);
	}
}
