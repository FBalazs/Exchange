package hu.berzsenyi.exchange.client.ui;

import hu.berzsenyi.exchange.client.R;
import hu.berzsenyi.exchange.client.game.ClientExchange;
import hu.berzsenyi.exchange.client.game.ClientExchange.IClientExchangeListener;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

public class StockBuyActivity extends ActionBarActivity {

	protected final static int REQUEST_CODE = 1;
	private ClientExchange mClient = ClientExchange.INSTANCE;
	private StockAdapter mAdapter;
	private ListView mListView;
	private ProgressDialog mProgressDialog;
	private ProgressDialog mSendingDialog;

	private int[] mAmounts;
	private int[] mEditTextValues;
	private int[] mMaxes;

	private String[] mStockNames;
	private double[] mStockPrices;
	private int mCount;

	private boolean sent = false;
	private Object mSentLock = new Object();

	private IClientExchangeListener mListener = new IClientExchangeListener() {

		@Override
		public void onTradeIndirect(ClientExchange exchange, int stockId,
				int amount, double price, boolean sold) {
		}

		@Override
		public void onTradeDirect(ClientExchange exchange, String partner,
				int stockId, int amount, double price, boolean sold) {
		}

		@Override
		public void onStocksChanged(ClientExchange exchange) {
			mAdapter.updateStocks();
		}

		@Override
		public void onShowBuy(ClientExchange exchange) {
		}

		@Override
		public void onSentOfferRefused(ClientExchange exchange) {
		}

		@Override
		public void onSentOfferAccepted(ClientExchange exchange) {
		}

		@Override
		public void onOfferCame(ClientExchange exchange) {
		}

		@Override
		public void onMyStocksChanged(ClientExchange exchange) {
		}

		@Override
		public void onMyMoneyChanged(ClientExchange exchange) {
		}

		@Override
		public void onEventsChanged(ClientExchange exchange) {
		}

		@Override
		public void onConnRefused(ClientExchange exchange) {
		}

		@Override
		public void onConnLost(ClientExchange exchange) {
			StockBuyActivity.this.finish();
		}

		@Override
		public void onConnAccepted(ClientExchange exchange) {
		}

		@Override
		public void onBuyRefused(ClientExchange exchange) {
			synchronized (mSentLock) {
				if (sent) {
					runOnUiThread(new Runnable() {
						public void run() {
							mSendingDialog.dismiss();
							new AlertDialog.Builder(StockBuyActivity.this)
									.setMessage(R.string.dont_have_enough_money)
									.setPositiveButton(R.string.ok, null)
									.create().show();
						}
					});

				}
			}
		}

		@Override
		public void onBuyEnd(ClientExchange exchange) {
			StockBuyActivity.this.finish();
		}

		@Override
		public void onBuyAccepted(ClientExchange exchange) {
			synchronized (mSentLock) {
				if (sent) {
					runOnUiThread(new Runnable() {
						@Override
						public void run() {
							setResult(Activity.RESULT_OK);
							mProgressDialog = ProgressDialog
									.show(StockBuyActivity.this,
											null,
											getString(R.string.waiting_for_the_first_round),
											true, false);
						}
					});

				}
			}
		}

		@Override
		public void onPlayersChanged(ClientExchange exchange) {
		}

		@Override
		public void onOfferDeleted(ClientExchange exchange) {
		}
	};
	private int COLOR_ILLEGAL = Color.RED;
	private ColorStateList colorDefault;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.d(getClass().getSimpleName(), "onCreate() started");

		ActionBar actionBar = getSupportActionBar();
		actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_HOME
				| ActionBar.DISPLAY_SHOW_TITLE);
		actionBar.setIcon(R.drawable.ic_launcher);
		actionBar.setElevation(getResources().getDimension(
				R.dimen.actionBar_elevation));

		setResult(Activity.RESULT_CANCELED);

		setContentView(R.layout.activity_stock_buy);

		TextView money = ((TextView) findViewById(R.id.money));
		money.setText(MainActivity.DECIMAL_FORMAT.format(mClient.getMoney()));
		colorDefault = money.getTextColors();

		// TODO Unregister listener
		mClient.addListener(mListener);

		mListView = (ListView) findViewById(R.id.stocks);
		mAdapter = new StockAdapter();
		mListView.setAdapter(mAdapter);

		((Button) findViewById(R.id.activity_zeroth_round_done))
				.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View p1) {
						onDoneButtonClick();
					}

				});
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		mClient.removeListener(mListener);
		if (mProgressDialog != null)
			mProgressDialog.dismiss();
		if (mSendingDialog != null)
			mSendingDialog.dismiss();
	}

	private void onDoneButtonClick() {
		if (!checkEditTexts())
			return;
		synchronized (mSentLock) {
			sent = true;
			mClient.doBuy(mAmounts);
			mSendingDialog = ProgressDialog.show(this, null,
					getString(R.string.waiting_for_response), true, false);
		}
	}

	private boolean checkEditTexts() {
		for (int i = 0; i < mAmounts.length; i++) {
			if (mAmounts[i] != mEditTextValues[i]) {
				AlertDialog.Builder builder = new AlertDialog.Builder(this);
				builder.setPositiveButton(R.string.ok, null);
				if (mEditTextValues[i] == -1) {
					builder.setMessage(String.format(
							getString(R.string.zeroth_round_bad_number_format),
							mStockNames[i]).toString());
				} else {
					builder.setMessage(String
							.format(getString(R.string.zeroth_round_cannot_buy_as_many_stocks),
									mMaxes[i], mStockNames[i]).toString());
				}
				builder.create().show();
				return false;
			}
		}
		return true;
	}

	private class StockAdapter extends BaseAdapter {

		public StockAdapter() {
			init();
		}

		private void init() {
			synchronized (mClient) {
				mCount = mClient.getStocksNumber();
				mAmounts = new int[mCount];
				mEditTextValues = new int[mCount];
				mMaxes = new int[mCount];

				synchronized (mClient) {
					mStockNames = new String[mCount];
					mStockPrices = new double[mCount];
					for (int i = 0; i < mCount; i++) {
						mStockNames[i] = mClient.getStockName(i);
						mStockPrices[i] = mClient.getStockPrice(i);
					}
				}
				refreshMaxes();
			}
		}

		private void refreshMaxes() {
			try {
				synchronized (mClient) {
					double remaining = mClient
							.calculateMoneyAfterPurchase(mAmounts);
					for (int i = 0; i < mCount; i++)
						mMaxes[i] = mAmounts[i]
								+ (int) (remaining / mStockPrices[i]);
				}
				runOnUiThread(new Runnable() {
					@Override
					public void run() {
						if (mAdapter != null) {
							mAdapter.notifyDataSetChanged();
							mListView.invalidate();
						}
					}
				});
			} catch (IllegalArgumentException e) {
				e.printStackTrace();
			}
		}

		@Override
		public int getCount() {
			synchronized (mClient) {
				return mCount;
			}
		}

		@Override
		public Object getItem(int position) {
			return null;
		}

		@Override
		public long getItemId(int p1) {
			return 0;
		}

		@Override
		public View getView(final int position, View convertView,
				ViewGroup parent) {
			final View out;
			if (convertView == null) {
				out = getLayoutInflater().inflate(
						R.layout.activity_stock_buy_stock, parent, false);
			} else
				out = convertView;

			((TextView) out.findViewById(R.id.stock_name))
					.setText(mStockNames[position]);
			((TextView) out.findViewById(R.id.main_tab_stocks_card_value))
					.setText(getString(R.string.unit_price)
							+ MainActivity.DECIMAL_FORMAT
									.format(mStockPrices[position]));

			final EditText valueEditText = ((EditText) out
					.findViewById(R.id.stock_amount_value));
			valueEditText.setTag(Integer.valueOf(position));

			synchronized (mClient) {
				// Android may call SeekBar.setProgress(SeekBar.getMax()) on
				// amount,
				// causing mAmounts to be modified. So first save
				// mAmounts[position]
				// and then load it back, using setProgress(currentAmount)
				int currentAmount = mAmounts[position];

				final SeekBar amount = (SeekBar) out
						.findViewById(R.id.stock_amount_seekbar);
				amount.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {

					@Override
					public void onStopTrackingTouch(SeekBar seekBar) {
						refreshMaxes();
					}

					@Override
					public void onStartTrackingTouch(SeekBar seekBar) {
					}

					@Override
					public void onProgressChanged(SeekBar seekBar,
							int progress, boolean fromUser) {
						if (fromUser) {
							((TextView) out
									.findViewById(R.id.stock_amount_value))
									.setText(progress + "");
						}
						mAmounts[position] = progress;

						double currentMoney = mClient
								.calculateMoneyAfterPurchase(mAmounts);
						TextView tv = ((TextView) findViewById(R.id.money));
						if (currentMoney < 0)
							tv.setTextColor(COLOR_ILLEGAL);
						else
							tv.setTextColor(colorDefault);
						tv.setText(MainActivity.DECIMAL_FORMAT
								.format(currentMoney));
					}
				});
				synchronized (mClient) {
					amount.setMax(mMaxes[position]);
				}
				amount.setProgress(currentAmount);

				valueEditText.setText(amount.getProgress() + "");
				if (convertView == null) {
					valueEditText.addTextChangedListener(new TextWatcher() {

						@Override
						public void onTextChanged(CharSequence s, int start,
								int before, int count) {
							int position = (Integer) valueEditText.getTag();
							try {
								int value = Integer.parseInt(s.toString());
								amount.setProgress(value);
								mEditTextValues[position] = value;
							} catch (NumberFormatException e) {
								amount.setProgress(0);
								mEditTextValues[position] = -1;
							}
						}

						@Override
						public void beforeTextChanged(CharSequence s,
								int start, int count, int after) {
						}

						@Override
						public void afterTextChanged(Editable s) {
						}
					});
				}

				return out;
			}
		}

		public void updateStocks() {
			init();
			runOnUiThread(new Runnable() {
				@Override
				public void run() {
					notifyDataSetChanged();
					mListView.invalidate();
				}
			});
		}

	}
}