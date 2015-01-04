package hu.berzsenyi.exchange.net.cmd;

import java.io.DataInputStream;
import java.io.DataOutputStream;

public class CmdServerStocks extends TCPCommand {
	public static final int ID = 9;
	
	public double startMoney;
	public String clientID;
	
	public CmdServerStocks(int length) {
		super(ID, length);
	}
	
	public CmdServerStocks(double startMoney, String clientID) {
		super(ID, 8+4+stringLength(clientID));
		this.startMoney = startMoney;
		this.clientID = clientID;
	}
	
	@Override
	public void read(DataInputStream in) throws Exception {
		this.startMoney = in.readDouble();
		this.clientID = readString(in);
	}
	
	@Override
	public void write(DataOutputStream out) throws Exception {
		out.writeDouble(this.startMoney);
		writeString(out, this.clientID);
	}
}
