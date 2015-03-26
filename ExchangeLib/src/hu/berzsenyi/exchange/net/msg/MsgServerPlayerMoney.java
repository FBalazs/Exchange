package hu.berzsenyi.exchange.net.msg;

public class MsgServerPlayerMoney extends Msg {
	private static final long serialVersionUID = -8072866690448995578L;
	
	public double money;
	
	public MsgServerPlayerMoney(double money) {
		this.money = money;
	}
}
