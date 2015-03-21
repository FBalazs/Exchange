package hu.berzsenyi.exchange.game;

public abstract class ExchangeGame {
	public static final int GAMEMODE_DIRECT = 0,
							GAMEMODE_INDIRECT = 1;
	
	private Stock[] mStocks;
	
	public ExchangeGame(Stock[] stocks) {
		if(stocks == null)
			throw new NullPointerException();
		mStocks = stocks;
	}
	
	public synchronized int getStockCount() {
		return mStocks.length;
	}
	
	public synchronized Stock getStock(int index) {
		return mStocks[index];
	}
}
