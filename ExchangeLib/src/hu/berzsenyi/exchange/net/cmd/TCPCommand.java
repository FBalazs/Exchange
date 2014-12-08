package hu.berzsenyi.exchange.net.cmd;

import java.io.DataInputStream;
import java.io.DataOutputStream;

public class TCPCommand {
	public static void writeString(DataOutputStream out, String str) throws Exception {
		out.writeInt(str.length());
		out.write(str.getBytes());
	}
	
	public static String readString(DataInputStream in) throws Exception {
		byte[] b = new byte[in.readInt()];
		in.read(b);
		return new String(b);
	}
	
	public int id, length;
	
	public TCPCommand(int id, int length) {
		this.id = id;
		this.length = length;
	}
	
	public void write(DataOutputStream out) throws Exception {
		
	}
	
	public void read(DataInputStream in) throws Exception {
		
	}
}
