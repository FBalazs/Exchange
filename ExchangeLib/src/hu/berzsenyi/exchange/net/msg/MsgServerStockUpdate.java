package hu.berzsenyi.exchange.net.msg;


public class MsgServerStockUpdate extends Msg {
	private static final long serialVersionUID = 4170546826338627855L;
	
	public double[] prices;
	
	public MsgServerStockUpdate(double[] prices) {
		this.prices = prices;
	}
}
