package hu.berzsenyi.exchange.game;

public abstract class ExchangeGame {
	public static final int GAMEMODE_DIRECT = 0, GAMEMODE_INDIRECT = 1;

	private Stock[] mStocks;

	public synchronized int getStockCount() {
		return mStocks == null ? 0 : mStocks.length;
	}

	public synchronized Stock getStock(int index) {
		return mStocks[index];
	}
	
	/**
	 * Use {@link #getStock(int)} instead where you want to access a single element
	 * @return a cloned Stock array
	 */
	public synchronized Stock[] getStocks() {
		return mStocks.clone();
	}

	protected synchronized void setStocks(Stock[] stocks) {
		mStocks = stocks;
	}

	// =====================================================
	// Bridge methods between protected methods of .game and
	// classes of .client.game
	// =====================================================
	protected static void setMoney(Player player, double money) {
		player.setMoney(money);
	}

	protected static void setStockAmounts(Player player, int[] amounts) {
		player.setStockAmounts(amounts);
	}

	protected static void setStockPrice(Stock stock, double price) {
		stock.setPrice(price);
	}

	protected static String getPassword(Player player) {
		return player.getPassword();
	}
}
