package hu.berzsenyi.exchange.net.msg;


public class MsgTeamInfo extends Msg {

	private static final long serialVersionUID = 1011240999320048166L;
	public double money;
	public int[] stocks;
	
	public MsgTeamInfo(double money, int[] stocks) {
		this.money = money;
		this.stocks = stocks;
	}
}
