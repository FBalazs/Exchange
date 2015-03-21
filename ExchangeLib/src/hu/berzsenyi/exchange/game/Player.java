package hu.berzsenyi.exchange.game;

public class Player {
	private String name, pass;
	private double mMoney = 0;
	private int[] mStocks;
	private OnChangeListener mListener;
	private ExchangeGame mExchange;

	public Player(ExchangeGame exchange, String name, String pass) {
		this.name = name;
		this.pass = pass;
		mExchange = exchange;
		mStocks = new int[mExchange.getStockCount()];
	}

	public double getMoney() {
		return mMoney;
	}

	public double getStockValue() {
		double out = 0.0;
		for (int i = 0; i < mStocks.length; i++)
			out += mStocks[i] * mExchange.getStock(i).getPrice();
		return out;
	}

	protected void setMoney(double money) {
		mMoney = money;
		if (mListener != null)
			mListener.onMoneyChanged(this);
	}

	public double calculateStocksValue() {
		double out = 0.0;
		for (int i = 0; i < mStocks.length; i++)
			out += mStocks[i] * mExchange.getStock(i).getPrice();
		return out;
	}

	public int getStock(int index) {
		return mStocks[index];
	}
	
	public int[] getStocks() {
		return mStocks.clone();
	}

	protected void setStock(int index, int newAmount) {
		mStocks[index] = newAmount;
		if (mListener != null)
			mListener.onStocksChanged(this, index);
	}
	

	protected void setStockAmounts(int[] stocks) {
		mStocks = stocks.clone();
		if (mListener != null)
			for (int i = 0; i < stocks.length; i++)
				mListener.onStocksChanged(this, i);
	}

	public String getName() {
		return name;
	}

	protected String getPassword() { // TODO should be avoided, using
										// Serialization and equals()
		return pass;
	}

	/*
	 * Returns true, if the names and the passwords match
	 * 
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		Player another = (Player) obj;
		return name.equals(another.name) && pass.equals(another.pass);
	}

	public void setOnChangeListener(OnChangeListener listener) {
		mListener = listener;
	}

	public static interface OnChangeListener {

		public void onMoneyChanged(Player team);

		public void onStocksChanged(Player team, int position);

	}
}
