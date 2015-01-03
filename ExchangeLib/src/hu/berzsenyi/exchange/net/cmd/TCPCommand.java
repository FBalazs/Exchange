package hu.berzsenyi.exchange.net.cmd;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.UnsupportedEncodingException;

public class TCPCommand {
	public static int stringLength(String str) {
		try {
			return str.getBytes("UTF-8").length;
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			return 0;
		}
	}
	
	public static void writeString(DataOutputStream out, String str) throws Exception {
		out.writeInt(stringLength(str));
		out.write(str.getBytes("UTF-8"));
	}
	
	public static String readString(DataInputStream in) throws Exception {
		byte[] b = new byte[in.readInt()];
		in.read(b);
		return new String(b, "UTF-8");
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
