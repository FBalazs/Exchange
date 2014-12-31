package hu.berzsenyi.exchange.net.cmd;

import java.io.DataInputStream;
import java.io.DataOutputStream;

public class CmdClientBuy extends TCPCommand {
	public static final int ID = 7;
	
	public int[] amount;
	
	public CmdClientBuy(int length) {
		super(ID, length);
	}
	
	public CmdClientBuy(int[] amount) {
		super(ID, 4+amount.length*4);
		this.amount = amount;
	}
	
	@Override
	public void read(DataInputStream in) throws Exception {
		this.amount = new int[in.readInt()];
		for(int s = 0; s < this.amount.length; s++)
			this.amount[s] = in.readInt();
	}
	
	@Override
	public void write(DataOutputStream out) throws Exception {
		out.writeInt(this.amount.length);
		for(int s = 0; s < this.amount.length; s++)
			out.writeInt(this.amount[s]);
	}
}
