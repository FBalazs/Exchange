package hu.berzsenyi.exchange.net.cmd;

import java.io.DataInputStream;
import java.io.DataOutputStream;

public class CmdServerError extends TCPCommand {
	public static final int ID = 10;
	public static final int ERROR_CONNECT = 0,
							ERROR_OFFER = 1;
	
	public int errorId;
	
	public CmdServerError(int length) {
		super(ID, length);
	}
	
	public CmdServerError(int errorId, Object nullObj) {
		super(ID, 4);
		this.errorId = errorId;
	}
	
	@Override
	public void read(DataInputStream in) throws Exception {
		this.errorId = in.readInt();
	}
	
	@Override
	public void write(DataOutputStream out) throws Exception {
		out.writeInt(this.errorId);
	}
}
