package hu.berzsenyi.exchange.client;

import hu.berzsenyi.exchange.Stock;
import hu.berzsenyi.exchange.Team;
import hu.berzsenyi.exchange.net.TCPClient;
import hu.berzsenyi.exchange.net.cmd.CmdClientOffer;
import hu.berzsenyi.exchange.net.cmd.CmdServerError;

import java.io.IOException;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

public class ActivityZerothRound extends Activity {

	protected final static int REQUEST_CODE = 1;
	private ExchangeClient mClient = ExchangeClient.getInstance();
	private StockAdapter mAdapter;
	private ListView mListView;
	private ProgressDialog mProgressDialog;

	private int[] mAmounts;
	private Stock[] mStocks;

	private int COLOR_ILLEGAL = Color.RED;
	private ColorStateList colorDefault;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.d(getClass().getName(), "onCreate() started");

		setResult(Activity.RESULT_CANCELED);

		setContentView(R.layout.activity_zeroth_round);

		TextView money = ((TextView) findViewById(R.id.money));
		money.setText(mClient.getModel().startMoney + "");
		colorDefault = money.getTextColors();

		mListView = (ListView) findViewById(R.id.stocks);
		mAdapter = new StockAdapter(mClient.getModel().stocks);
		mListView.setAdapter(mAdapter);

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
			public void onRoundCommand(ExchangeClient client) {
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

		});
		if (!mClient.isConnected())
			finish();

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
		if (mClient.doBuy(this.mAmounts)) { // OK
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

	private class StockAdapter extends BaseAdapter {

		public StockAdapter(Stock[] stocks) {
			ActivityZerothRound.this.mStocks = stocks == null ? new Stock[0]
					: stocks; // stocks can be null!
			mAmounts = new int[ActivityZerothRound.this.mStocks.length];
		}

		@Override
		public int getCount() {
			return mStocks.length;
		}

		@Override
		public Object getItem(int position) {
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

			Stock stock = (Stock) getItem(position);
			((TextView) out.findViewById(R.id.stock_name)).setText(stock.name);
			((TextView) out.findViewById(R.id.stock_value))
					.setText(getString(R.string.unit_price) + stock.value);
			SeekBar amount = (SeekBar) out
					.findViewById(R.id.stock_amount_seekbar);
			amount.setMax((int) (mClient.getModel().startMoney / stock.value));
			amount.setProgress(mAmounts[position]);
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
					if (!fromUser)
						return;
					((TextView) out.findViewById(R.id.stock_amount_label))
							.setText(progress + "");
					mAmounts[position] = progress;

					double currentMoney = mClient.getModel()
							.calculateMoneyAfterPurchase(mAmounts);
					TextView tv = ((TextView) findViewById(R.id.money));
					if (currentMoney < 0)
						tv.setTextColor(COLOR_ILLEGAL);
					else
						tv.setTextColor(colorDefault);
					tv.setText(currentMoney + "");
				}
			});

			((TextView) out.findViewById(R.id.stock_amount_label))
					.setText(amount.getProgress() + "");

			return out;
		}

		public void updateStocks(Stock[] stocks) {
			ActivityZerothRound.this.mStocks = stocks == null ? new Stock[0]
					: stocks; // stocks can be null!
			mAmounts = new int[ActivityZerothRound.this.mStocks.length];
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
