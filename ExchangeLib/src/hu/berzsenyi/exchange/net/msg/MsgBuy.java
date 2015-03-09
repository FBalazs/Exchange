package hu.berzsenyi.exchange.net.msg;

import java.io.Serializable;

public class MsgBuy implements Serializable {
	private static final long serialVersionUID = 7844182658416165777L;
	
	public int[] amounts;
	
	public MsgBuy(int[] amounts) {
		this.amounts = amounts;
	}
}
