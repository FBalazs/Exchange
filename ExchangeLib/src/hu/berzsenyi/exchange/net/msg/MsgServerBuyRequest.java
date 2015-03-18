package hu.berzsenyi.exchange.net.msg;

public class MsgServerBuyRequest extends Msg {
	private static final long serialVersionUID = -2390536557475235472L;
	
	public double money;
	
	public MsgServerBuyRequest(double money) {
		this.money = money;
	}
}
