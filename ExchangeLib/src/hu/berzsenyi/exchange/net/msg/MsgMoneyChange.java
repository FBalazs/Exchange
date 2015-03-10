package hu.berzsenyi.exchange.net.msg;

public class MsgMoneyChange extends Msg {

	private static final long serialVersionUID = 7405265800603022938L;
	public double newMoney;
	
	public MsgMoneyChange(double newMoney) {
		this.newMoney = newMoney;
	}
	
}
