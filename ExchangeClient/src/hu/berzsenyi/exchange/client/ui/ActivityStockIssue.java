package hu.berzsenyi.exchange.client.ui;

import hu.berzsenyi.exchange.client.R;
import hu.berzsenyi.exchange.client.game.ClientExchangeGame;
import hu.berzsenyi.exchange.client.game.ClientExchangeGame.IClientExchangeGameListener;
import hu.berzsenyi.exchange.game.Stock;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
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

public class ActivityStockIssue extends ActionBarActivity {

	protected final static int REQUEST_CODE = 1;
	private ClientExchangeGame mClient = ClientExchangeGame.getInstance();
	private StockAdapter mAdapter;
	private ListView mListView;
	private ProgressDialog mProgressDialog;

	private Stock[] mStocks;

	private int[] mAmounts;
	private int[] mEditTextValues;
	private int[] mMaxes;
	private IClientExchangeGameListener mListener = new IClientExchangeGameListener() {

		@Override
		public void onTrade(ClientExchangeGame exchange) {
		}

		@Override
		public void onStocksChanged(ClientExchangeGame exchange) {
			mAdapter.notifyDataSetChanged();
		}

		@Override
		public void onShowBuy(ClientExchangeGame exchange) {
		}

		@Override
		public void onSentOfferRefused(ClientExchangeGame exchange) {
		}

		@Override
		public void onSentOfferAccepted(ClientExchangeGame exchange) {
		}

		@Override
		public void onOfferCame(ClientExchangeGame exchange) {
		}

		@Override
		public void onMyStocksChanged(ClientExchangeGame exchange) {
		}

		@Override
		public void onMyMoneyChanged(ClientExchangeGame exchange) {
		}

		@Override
		public void onConnRefused(ClientExchangeGame exchange) {

			new AlertDialog.Builder(ActivityStockIssue.this)
					.setMessage(R.string.error_connection_refused)
					.setNeutralButton("OK",
							new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog,
										int which) {
									finish();
								}
							}).create().show();
		}

		@Override
		public void onConnLost(ClientExchangeGame exchange) {
			ActivityStockIssue.this.finish();
		}

		@Override
		public void onConnAccepted(ClientExchangeGame exchange) {
		}

		@Override
		public void onBuyRefused(ClientExchangeGame exchange) {
		}

		@Override
		public void onBuyAccepted(ClientExchangeGame exchange) {
		}

		public void onStockIssueEnded(ClientExchangeGame exchange) {
			ActivityStockIssue.this.finish();
		}

		@Override
		public void onConnFailed(ClientExchangeGame exchange) {
		}
	};

	private int COLOR_ILLEGAL = Color.RED;
	private ColorStateList colorDefault;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.d(getClass().getName(), "onCreate() started");

		ActionBar actionBar = getSupportActionBar();
		actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_HOME
				| ActionBar.DISPLAY_SHOW_TITLE);
		actionBar.setIcon(R.drawable.ic_launcher);
		actionBar.setElevation(getResources().getDimension(
				R.dimen.actionBar_elevation));

		setResult(Activity.RESULT_CANCELED);

		setContentView(R.layout.activity_zeroth_round);

		TextView money = ((TextView) findViewById(R.id.money));
		money.setText(ActivityMain.DECIMAL_FORMAT.format(mClient.getOwnPlayer()
				.getMoney()));
		colorDefault = money.getTextColors();

		// TODO Unregister listener
		mClient.addListener(mListener);
		if (!mClient.isInGame())
			finish();

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
	}

	private void onDoneButtonClick() {
		if (!checkEditTexts())
			return;
		if (mClient.doBuy(mAmounts)) { // OK
			setResult(Activity.RESULT_OK);
			// ((Button)
			// findViewById(R.id.activity_zeroth_round_done)).setEnabled(false);
			mProgressDialog = ProgressDialog.show(this, null,
					getString(R.string.waiting_for_the_first_round), true,
					false);
		} else {
			new AlertDialog.Builder(this)
					.setMessage(R.string.dont_have_enough_money)
					.setPositiveButton(R.string.ok, null).create().show();
		}
	}

	private boolean checkEditTexts() {
		synchronized (mAdapter) {
			for (int i = 0; i < mAmounts.length; i++) {
				if (mAmounts[i] != mEditTextValues[i]) {
					AlertDialog.Builder builder = new AlertDialog.Builder(this);
					builder.setPositiveButton(R.string.ok, null);
					if (mEditTextValues[i] == -1) {
						builder.setMessage(String
								.format(getString(R.string.zeroth_round_bad_number_format),
										mStocks[i].getName()).toString());
					} else {
						builder.setMessage(String
								.format(getString(R.string.zeroth_round_cannot_buy_as_many_stocks),
										mMaxes[i], mStocks[i].getName())
								.toString());
					}
					builder.create().show();
					return false;
				}
			}
		}
		return true;
	}

	private void calculateMaxes() {
		synchronized (mAdapter) {
			for (int i = 0; i < mStocks.length; i++)
				mMaxes[i] = (int) (mClient.getOwnPlayer().getMoney() / mStocks[i]
						.getPrice());
		}
	}

	private class StockAdapter extends BaseAdapter {

		public StockAdapter() {
			init();
		}

		@Override
		public void notifyDataSetChanged() {
			init();
			mListView.invalidate(); // TODO necessary?
			super.notifyDataSetChanged();
		}

		private synchronized void init() {
			synchronized (mClient) {
				mStocks = mClient.getStocks();
				mAmounts = new int[mStocks.length];
				mEditTextValues = new int[mStocks.length];
				mMaxes = new int[mStocks.length];
				calculateMaxes();
			}
		}

		@Override
		public synchronized int getCount() {
			return mStocks.length;
		}

		@Override
		public synchronized Stock getItem(int position) {
			return mStocks[position];
		}

		@Override
		public long getItemId(int p1) {
			return 0;
		}

		@Override
		public synchronized View getView(final int position, View convertView,
				ViewGroup parent) {
			final View out;
			if (convertView == null) {
				out = getLayoutInflater().inflate(
						R.layout.activity_zeroth_round_stock, parent, false);
			} else
				out = convertView;

			Stock stock = getItem(position);
			((TextView) out.findViewById(R.id.stock_name)).setText(stock
					.getName());
			((TextView) out.findViewById(R.id.main_tab_stocks_card_value))
					.setText(getString(R.string.unit_price)
							+ ActivityMain.DECIMAL_FORMAT.format(stock
									.getPrice()));

			final EditText valueEditText = ((EditText) out
					.findViewById(R.id.stock_amount_value));
			valueEditText.setTag(Integer.valueOf(position));

			// Android may call SeekBar.setProgress(SeekBar.getMax()) on amount,
			// causing mAmounts to be modified. So first save mAmounts[position]
			// and then load it back, using setProgress(currentAmount)
			int currentAmount = mAmounts[position];

			final SeekBar amount = (SeekBar) out
					.findViewById(R.id.stock_amount_seekbar);
			amount.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {

				@Override
				public void onStopTrackingTouch(SeekBar seekBar) {
				}

				@Override
				public void onStartTrackingTouch(SeekBar seekBar) {
				}

				@Override
				public void onProgressChanged(SeekBar seekBar, int progress,
						boolean fromUser) {
					if (fromUser) {
						((TextView) out.findViewById(R.id.stock_amount_value))
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
					tv.setText(ActivityMain.DECIMAL_FORMAT.format(currentMoney));
				}
			});
			amount.setMax(mMaxes[position]);
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
					public void beforeTextChanged(CharSequence s, int start,
							int count, int after) {
					}

					@Override
					public void afterTextChanged(Editable s) {
					}
				});
			}

			return out;
		}

	}
}
