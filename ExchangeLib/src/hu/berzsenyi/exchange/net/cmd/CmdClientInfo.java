package hu.berzsenyi.exchange.net.cmd;

import java.io.DataInputStream;
import java.io.DataOutputStream;

public class CmdClientInfo extends TCPCommand {
	public static final int ID = 1;
	
	public String name;
	
	public CmdClientInfo(int length) {
		super(ID, length);
	}
	
	public CmdClientInfo(String name) {
		super(ID, name.length());
		this.name = name;
	}
	
	@Override
	public void read(DataInputStream in) throws Exception {
		this.name = readString(in);
	}
	
	@Override
	public void write(DataOutputStream out) throws Exception {
		writeString(out, this.name);
	}
}
