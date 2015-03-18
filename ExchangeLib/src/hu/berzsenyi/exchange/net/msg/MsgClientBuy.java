package hu.berzsenyi.exchange.net.msg;

public class MsgClientBuy extends Msg {
	private static final long serialVersionUID = 224674305967043208L;
	
	public int[] stocks;
	
	public MsgClientBuy(int[] stocks) {
		this.stocks = stocks;
	}
}
