package hu.berzsenyi.exchange.net.cmd;

public class CmdServerError extends TCPCommand {
	private static final long serialVersionUID = -8338245388570570207L;
	public static final int ERROR_NOT_IN_ZEROTH_ROUND = 0,
							ERROR_NAME_DUPLICATE = 1,
							ERROR_OFFER = 2;
	
	public int errorId;
	
	public CmdServerError(int errorId) {
		this.errorId = errorId;
	}
	
}
