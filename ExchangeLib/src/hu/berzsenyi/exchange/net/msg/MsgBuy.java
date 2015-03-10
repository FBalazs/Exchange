package hu.berzsenyi.exchange.net.msg;


public class MsgBuy extends Msg {
	private static final long serialVersionUID = 7844182658416165777L;
	
	public int[] amounts;
	
	public MsgBuy(int[] amounts) {
		this.amounts = amounts;
	}
}
