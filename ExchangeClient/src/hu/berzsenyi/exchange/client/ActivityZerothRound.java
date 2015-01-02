package hu.berzsenyi.exchange.client;

import java.io.IOException;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.BaseAdapter;
import android.view.ViewGroup;
import android.view.View;
import hu.berzsenyi.exchange.Stock;
import hu.berzsenyi.exchange.net.TCPClient;
import hu.berzsenyi.exchange.net.cmd.CmdOffer;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.SeekBar;
import android.widget.ListView;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.util.Log;

public class ActivityZerothRound extends Activity {

	protected final static int REQUEST_CODE = 1;
	private ExchangeClient mClient = ExchangeClient.getInstance();
	private StockAdapter mAdapter;
	private ListView mListView;

	private int[] mAmounts;
	private Stock[] mStocks;

	private static final double MONEY = 2000;

	private int COLOR_ILLEGAL = Color.RED;
	private ColorStateList colorDefault;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.d(getClass().getName(), "onCreate() started");

		setResult(Activity.RESULT_CANCELED);

		setContentView(R.layout.activity_zeroth_round);

		TextView money = ((TextView) findViewById(R.id.money));
		money.setText(MONEY + "");
		colorDefault = money.getTextColors();

		mListView = (ListView) findViewById(R.id.stocks);
		mAdapter = new StockAdapter(mClient.getModel().stockList);
		mListView.setAdapter(mAdapter);

		mClient.addIClientListener(new IClientListener() {

			@Override
			public void onConnectionFail(TCPClient client, IOException exception) {
			}

			@Override
			public void onConnect(TCPClient client) {
			}

			@Override
			public void onClose(TCPClient client) {
			}

			@Override
			public void onTeamsCommand(ExchangeClient client) {
			}

			@Override
			public void onStocksCommand(ExchangeClient client) {
				mAdapter.updateStocks(client.getModel().stockList);
			}

			@Override
			public void onOfferIn(ExchangeClient client, CmdOffer offer) {
			}
		});

		((Button) findViewById(R.id.activity_zeroth_round_done))
				.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View p1) {
						onDoneButtonClick();
					}

				});
	}

	private void onDoneButtonClick() {
		if (calculateMoney() > 0) { // OK
			// TODO send result to the server
			setResult(Activity.RESULT_OK);
			finish();
		} else {
			new AlertDialog.Builder(this)
					.setMessage(R.string.dont_have_enough_money)
					.setPositiveButton(R.string.ok, null).create().show();
		}
	}

	private double calculateMoney() {
		double sum = 0.0;
		for (int i = 0; i < mAmounts.length; i++)
			sum += mAmounts[i] * mStocks[i].value;
		return MONEY - sum;
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
			amount.setMax((int) (MONEY / stock.value));
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
					((TextView) out.findViewById(R.id.stock_amount_label))
							.setText(progress + "");
					mAmounts[position] = progress;

					double currentMoney = calculateMoney();
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
					: stocks; // stocks can
			// be null!
			mAmounts = new int[ActivityZerothRound.this.mStocks.length];
			notifyDataSetChanged();
		}

	}
}
