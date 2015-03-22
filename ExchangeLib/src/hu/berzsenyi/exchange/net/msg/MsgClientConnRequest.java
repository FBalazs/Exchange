package hu.berzsenyi.exchange.net.msg;


public class MsgClientConnRequest extends Msg {
	private static final long serialVersionUID = 9033761640852074594L;
	
	private String nickName, password;
	private int versionCode;
	
	public MsgClientConnRequest(int versionCode, String nickName, String password) {
		this.versionCode = versionCode;
		this.nickName = nickName;
		this.password = password;
	}
	
	public String getNickName() {
		return nickName;
	}
	
	public String getPassword() {
		return password;
	}
	
	public int getVersionCode() {
		return versionCode;
	}
}
