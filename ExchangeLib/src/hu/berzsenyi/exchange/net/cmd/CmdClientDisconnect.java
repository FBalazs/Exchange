package hu.berzsenyi.exchange.net.cmd;

import java.io.DataInputStream;
import java.io.DataOutputStream;

public class CmdClientDisconnect extends TCPCommand {
	public static final int ID = 2;
	
	public CmdClientDisconnect(int length) {
		super(ID, length);
	}
	
	public CmdClientDisconnect() {
		super(ID, 0);
	}
	
	@Override
	public void read(DataInputStream in) throws Exception {
		
	}
	
	@Override
	public void write(DataOutputStream out) throws Exception {
		
	}
}
