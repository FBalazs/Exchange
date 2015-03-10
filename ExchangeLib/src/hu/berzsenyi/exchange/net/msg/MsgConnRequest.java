package hu.berzsenyi.exchange.net.msg;


public class MsgConnRequest extends Msg {
	private static final long serialVersionUID = -5064127097862148471L;
	
	public String nickName, password;
	
	public MsgConnRequest(String nickName, String password) {
		this.nickName = nickName;
		this.password = password;
	}
}
