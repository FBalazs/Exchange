package hu.berzsenyi.exchange.net.cmd;

import java.io.DataInputStream;
import java.io.DataOutputStream;

public class CmdClientInfo extends TCPCommand {
	public static final int ID = 1;
	
	public String name, password;
	
	public CmdClientInfo(int length) {
		super(ID, length);
	}
	
	public CmdClientInfo(String name, String password) {
		super(ID, 4+stringLength(name)+4+stringLength(password));
		this.name = name;
		this.password = password;
	}
	
	@Override
	public void read(DataInputStream in) throws Exception {
		this.name = readString(in);
		this.password = readString(in);
	}
	
	@Override
	public void write(DataOutputStream out) throws Exception {
		writeString(out, this.name);
		writeString(out, this.password);
	}
}
