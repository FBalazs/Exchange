package hu.berzsenyi.exchange.net.msg;


public class MsgServerConnRefuse extends Msg {
	private static final long serialVersionUID = 4191876282808628637L;
	
	public static final int BAD_PASSWORD = 0,
							NOT_0ROUND = 1;
	
	public int msg;
	
	public MsgServerConnRefuse(int msg) {
		this.msg = msg;
	}
}
