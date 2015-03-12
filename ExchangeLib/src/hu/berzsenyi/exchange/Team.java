package hu.berzsenyi.exchange;

public class Team {
	public String id, name, pass;
	private double mMoney = 0;
	private int[] mStocks;
	private OnChangeListener mListener;
	private Model mModel;

	public Team(Model model, String id, String name, String pass) {
		this.id = id;
		this.name = name;
		this.pass = pass;
		mModel = model;
		mStocks = new int[mModel.stocks.length];
	}

	public double getMoney() {
		return mMoney;
	}

	public double getStockValue() {
		double out = 0.0;
		for (int i = 0; i < mStocks.length; i++)
			out += mStocks[i] * mModel.stocks[i].value;
		return out;
	}

	public void setMoney(double money) {
		mMoney = money;
		if (mListener != null)
			mListener.onMoneyChanged(this);
	}

	public double calculateStocksValue() {
		double out = 0.0;
		for (int i = 0; i < mStocks.length; i++)
			out += mStocks[i] * mModel.stocks[i].value;
		return out;
	}

	public int[] getStocks() {
		return mStocks.clone();
	}

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
