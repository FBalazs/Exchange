package hu.berzsenyi.exchange.net.msg;


public class MsgClientConnRequest extends Msg {
	private static final long serialVersionUID = 9033761640852074594L;
	
	public String nickName, password;
	
	public MsgClientConnRequest(String nickName, String password) {
		this.nickName = nickName;
		this.password = password;
	}
}
