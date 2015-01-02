package hu.berzsenyi.exchange.net.cmd;

import java.io.DataInputStream;
import java.io.DataOutputStream;

public class CmdServerNextRound extends TCPCommand {
	public static final int ID = 8;
	
	public CmdServerNextRound(int length) {
		super(ID, length);
	}
	
	public CmdServerNextRound() {
		super(ID, 0);
	}
	
	@Override
	public void read(DataInputStream in) throws Exception {
		
	}
	
	@Override
	public void write(DataOutputStream out) throws Exception {
		
	}
}
