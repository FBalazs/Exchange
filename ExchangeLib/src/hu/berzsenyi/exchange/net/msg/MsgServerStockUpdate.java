package hu.berzsenyi.exchange.net.msg;

import hu.berzsenyi.exchange.Stock;


public class MsgServerStockUpdate extends Msg {
	private static final long serialVersionUID = 4170546826338627855L;
	
	public double[] prices;
	
	public MsgServerStockUpdate(Stock[] stocks) {
		prices = new double[stocks.length];
		for(int i = 0; i < stocks.length; i++)
			prices[i] = stocks[i].getPrice();
	}
}
