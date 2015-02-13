package hu.berzsenyi.exchange;

public class Team {
	public String id, name, pass;
	private double mMoney = 0;
	private int[] mStocks = null;
	private OnChangeListener mListener;

	public Team(String id, String name, String pass) {
		this.id = id;
		this.name = name;
		this.pass = pass;
	}

	public double getMoney() {
		return mMoney;
	}
	
	public double getStockValue(Model model) {
		double out = 0.0;
		for(int i=0;i<mStocks.length;i++)
			out += mStocks[i] * model.stocks[i].value;
		return out;
	}

	public void setMoney(double money) {
		mMoney = money;
		if (mListener != null)
			mListener.onMoneyChanged(this);
	}

	// /**
	// * Should not be edited!!! Avoid using {@code {@link #getStocks()}[i] =
	// * ...}!
	// *
	// * @return
	// */
	// public int[] getStocks() {
	// return mStocks;
	// }

	public int getStock(int index) {
		return mStocks[index];
	}

	public void setStock(int index, int newAmount) {
		mStocks[index] = newAmount;
		if (mListener != null)
			mListener.onStocksChanged(this, index);
	}

	public void setStocks(int[] stocks) {
		mStocks = stocks.clone();
		if (mListener != null)
			for (int i = 0; i < stocks.length; i++)
				mListener.onStocksChanged(this, i);
	}

	public void setOnChangeListener(OnChangeListener listener) {
		mListener = listener;
	}

	public static interface OnChangeListener {

		public void onMoneyChanged(Team team);

		public void onStocksChanged(Team team, int position);

	}
}
