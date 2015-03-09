package hu.berzsenyi.exchange.net.msg;

import java.io.Serializable;

public class MsgNewTeam implements Serializable {
	private static final long serialVersionUID = -4937331453079959512L;
	
	public String name;
	
	public MsgNewTeam(String name) {
		this.name = name;
	}
}
