package hu.berzsenyi.exchange.game;

public abstract class Exchange {
	public static final int GAMEMODE_DIRECT = 0,
							GAMEMODE_INDIRECT = 1;
	
	private Stock[] mStocks;
	
	public Exchange(Stock[] stocks) {
		if(stocks == null)
			throw new NullPointerException();
		mStocks = stocks;
	}
	
	public int getStockCount() {
		return mStocks.length;
	}
	
	public Stock getStock(int index) {
		return mStocks[index];
	}
}
