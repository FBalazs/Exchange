package hu.berzsenyi.exchange.net.msg;

import java.io.Serializable;

public class MsgConnRequest implements Serializable {
	private static final long serialVersionUID = -5064127097862148471L;
	
	public String nickName, password;
}
