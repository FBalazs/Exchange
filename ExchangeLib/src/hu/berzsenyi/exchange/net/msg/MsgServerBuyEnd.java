package hu.berzsenyi.exchange.net.msg;

import hu.berzsenyi.exchange.Stock;

public class MsgServerBuyEnd extends Msg {
	private static final long serialVersionUID = -579949381855320453L;
	
	public int[] stocksIngame;
	
	public MsgServerBuyEnd(Stock[] stocks) {
		stocksIngame = new int[stocks.length];
		for(int i = 0; i < stocks.length; i++)
			stocksIngame[i] = stocks[i].getIngame();
	}
}
