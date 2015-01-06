package hu.berzsenyi.exchange.net.cmd;

import java.io.DataInputStream;
import java.io.DataOutputStream;

public class CmdServerNextRound extends TCPCommand {
	public static final int ID = 8;
	
	public String eventDesc;
	public double[] multipliers;
	
	public CmdServerNextRound(int length) {
		super(ID, length);
	}
	
	public CmdServerNextRound(String eventDesc, double[] multipliers) {
		super(ID, 4+stringLength(eventDesc)+4+8*multipliers.length);
		this.eventDesc = eventDesc;
		this.multipliers = multipliers;
	}
	
	@Override
	public void read(DataInputStream in) throws Exception {
		this.eventDesc = readString(in);
		this.multipliers = new double[in.readInt()];
		for(int i = 0; i < this.multipliers.length; i++)
			this.multipliers[i] = in.readDouble();
	}
	
	@Override
	public void write(DataOutputStream out) throws Exception {
		writeString(out, this.eventDesc);
		out.writeInt(this.multipliers.length);
		for(int i = 0; i < this.multipliers.length; i++)
			out.writeDouble(this.multipliers[i]);
	}
}
