package hu.berzsenyi.exchange.client;

import hu.berzsenyi.exchange.SingleEvent;
import hu.berzsenyi.exchange.Stock;
import hu.berzsenyi.exchange.Team;
import hu.berzsenyi.exchange.net.TCPClient;
import hu.berzsenyi.exchange.net.cmd.CmdServerError;

import java.io.IOException;

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

public class ActivityZerothRound extends ActionBarActivity {

	protected final static int REQUEST_CODE = 1;
	private ExchangeClient mClient = ExchangeClient.getInstance();
	private StockAdapter mAdapter;
	private ListView mListView;
	private ProgressDialog mProgressDialog;

	private int[] mAmounts;
	private int[] mEditTextValues;
	private Stock[] mStocks;
	private int[] mMaxes;

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
		money.setText(NewActivityMain.DECIMAL_FORMAT.format(mClient.getModel().startMoney));
		colorDefault = money.getTextColors();

		// TODO Unregister listener
		mClient.addIClientListener(new IClientListener() {

			@Override
			public void onConnectionFail(TCPClient client, IOException exception) {
			}

			@Override
			public void onConnect(TCPClient client) {
			}

			@Override
			public void onClose(TCPClient client) {
				ActivityZerothRound.this.finish();
			}

			@Override
			public void onTeamsCommand(ExchangeClient client) {
			}

			@Override
			public void onStocksCommand(ExchangeClient client) {
				mAdapter.updateStocks(client.getModel().stocks);
			}

			@Override
			public void onNewEvents(SingleEvent[] events) {
				ActivityZerothRound.this.finish();
			}

			@Override
			public void onMoneyChanged(Team ownTeam) {
			}

			@Override
			public void onStocksChanged(Team ownTeam, int position) {
			}

			@Override
			public void onErrorCommand(CmdServerError error) {
			}

			@Override
			public void onOutgoingOffersChanged() {

			}

		});
		if (!mClient.isConnected())
			finish();

		mListView = (ListView) findViewById(R.id.stocks);
		mAdapter = new StockAdapter(mClient.getModel().stocks);
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
		for (int i = 0; i < mAmounts.length; i++) {
			if (mAmounts[i] != mEditTextValues[i]) {
				AlertDialog.Builder builder = new AlertDialog.Builder(this);
				builder.setPositiveButton(R.string.ok, null);
				if (mEditTextValues[i] == -1) {
					builder.setMessage(String.format(
							getString(R.string.zeroth_round_bad_number_format),
							mStocks[i].name).toString());
				} else {
					builder.setMessage(String
							.format(getString(R.string.zeroth_round_cannot_buy_as_many_stocks),
									mMaxes[i], mStocks[i].name).toString());
				}
				builder.create().show();
				return false;
			}
		}
		return true;
	}

	private void calculateMaxes() {
		for (int i = 0; i < mStocks.length; i++)
			mMaxes[i] = (int) (mClient.getModel().startMoney / mStocks[i].value);
	}

	private class StockAdapter extends BaseAdapter {

		public StockAdapter(Stock[] stocks) {
			mStocks = stocks == null ? new Stock[0] : stocks; // stocks can be
																// null!
			mAmounts = new int[mStocks.length];
			mEditTextValues = new int[mStocks.length];
			mMaxes = new int[mStocks.length];
			calculateMaxes();
		}

		@Override
		public int getCount() {
			return mStocks.length;
		}

		@Override
		public Stock getItem(int position) {
			return mStocks[position];
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
						R.layout.activity_zeroth_round_stock, parent, false);
			} else
				out = convertView;

			Stock stock = getItem(position);
			((TextView) out.findViewById(R.id.stock_name)).setText(stock.name);
			((TextView) out.findViewById(R.id.main_tab_stocks_card_value))
					.setText(getString(R.string.unit_price)
							+ NewActivityMain.DECIMAL_FORMAT.format(stock.value));

			// Android may call SeekBar.setProgress(SeekBar.getMax()) on amount,
			// causing mAmounts to be modified. So first save mAmounts[position]
			// and then load it back, using setProgress(currentAmount)
			int currentAmount = mAmounts[position];

			final SeekBar amount = (SeekBar) out
					.findViewById(R.id.stock_amount_seekbar);
			amount.setMax(mMaxes[position]);
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

					double currentMoney = mClient.getModel()
							.calculateMoneyAfterPurchase(mAmounts);
					TextView tv = ((TextView) findViewById(R.id.money));
					if (currentMoney < 0)
						tv.setTextColor(COLOR_ILLEGAL);
					else
						tv.setTextColor(colorDefault);
					tv.setText(NewActivityMain.DECIMAL_FORMAT.format(currentMoney));
				}
			});
			amount.setProgress(currentAmount);

			EditText value = ((EditText) out
					.findViewById(R.id.stock_amount_value));
			value.setText(amount.getProgress() + "");
			value.addTextChangedListener(new TextWatcher() {

				@Override
				public void onTextChanged(CharSequence s, int start,
						int before, int count) {
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

			return out;
		}

		public void updateStocks(Stock[] stocks) {
			mStocks = stocks == null ? new Stock[0] : stocks; // stocks can be
																// null!
			mAmounts = new int[mStocks.length];
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
