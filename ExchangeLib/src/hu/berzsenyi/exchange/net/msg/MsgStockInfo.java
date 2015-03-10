package hu.berzsenyi.exchange.net.msg;

import hu.berzsenyi.exchange.Stock;

public class MsgStockInfo extends Msg {
	private static final long serialVersionUID = -3204099119298799477L;
	
	public double[] stockPrices;
	
	public MsgStockInfo(Stock[] stocks) {
		stockPrices = new double[stocks.length];
		for(int s = 0; s < stocks.length; s++)
			stockPrices[s] = stocks[s].value;
	}
}
