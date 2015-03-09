package hu.berzsenyi.exchange.net.msg;

import java.io.Serializable;

public class MsgTeamInfo implements Serializable {
	private static final long serialVersionUID = 4050629332346872401L;
	
	public double money;
	public int[] stocks;
	
	public MsgTeamInfo(double money, int[] stocks) {
		this.money = money;
		this.stocks = stocks;
	}
}
