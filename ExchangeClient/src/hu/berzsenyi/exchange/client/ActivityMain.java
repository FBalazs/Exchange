package hu.berzsenyi.exchange.client;

import hu.berzsenyi.exchange.Stock;
import hu.berzsenyi.exchange.Team;
import hu.berzsenyi.exchange.net.TCPClient;
import hu.berzsenyi.exchange.net.cmd.CmdServerError;

import java.io.IOException;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.Spinner;
import android.widget.TabHost;
import android.widget.TabHost.TabSpec;
import android.widget.TextView;
import android.widget.Toast;

public class ActivityMain extends Activity implements IClientListener {

	protected static final String EXTRA_NAME = "strName", EXTRA_IP = "strIP",
			EXTRA_PORT = "intPort";

	protected static final String TAG_OVERVIEW = "overview",
			TAG_STOCKS = "stocks", TAG_OFFER = "offer",
			TAG_OUTGOING = "outgoing";

	protected static final DecimalFormat DECIMAL_FORMAT = new DecimalFormat(
			"#0.00");

	private TabHost tabHost;
	private TabSpec tabMain, tabStocks, tabOffer, tabOutgoing;

	private ListView tabStocks_listStocks;

	private Spinner tabOffer_listStocks;
	private Button tabOffer_buttonOfferSend;
	private SeekBar tabOffer_seekBarAmount;
	private TextView tabOffer_textAmount;
	private EditText tabOffer_editTextUnitPrice;
	private RadioGroup tabOffer_radioGroup;

	private List<Integer> tabOffer_spinnerPosition2StockIndex,
			tabOffer_spinnerPosition2TeamIndex;

	private ExchangeClient mClient;

	private boolean zerothRoundDone = false, zerothRoundStarted = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {

		Log.d(this.getClass().getName(), "onCreate()");
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.activity_main);

		mClient = ExchangeClient.getInstance();

		if (!mClient.isConnected()) { // Maybe there was an error message,
										// indicating the failure of joining the
										// game
			this.finish();
			return;
		}

		this.tabHost = (TabHost) this.findViewById(R.id.tabHost);
		this.tabHost.setup();

		this.tabMain = this.tabHost.newTabSpec(TAG_OVERVIEW);
		this.tabMain.setContent(R.id.tabMain);
		this.tabMain.setIndicator(this.getString(R.string.overview));

		this.tabStocks = this.tabHost.newTabSpec(TAG_STOCKS);
		this.tabStocks.setContent(R.id.tabStocks);
		this.tabStocks.setIndicator(this.getString(R.string.stocks));

		this.tabOffer = this.tabHost.newTabSpec(TAG_OFFER);
		this.tabOffer.setContent(R.id.tabOffer);
		this.tabOffer.setIndicator(this.getString(R.string.offer));

		this.tabOutgoing = this.tabHost.newTabSpec(TAG_OUTGOING);
		this.tabOutgoing.setContent(R.id.tabOutgoing);
		this.tabOutgoing.setIndicator(this.getString(R.string.outgoing));

		this.tabStocks_listStocks = (ListView) this
				.findViewById(R.id.tabStocks_listStocks);

		this.tabOffer_radioGroup = (RadioGroup) findViewById(R.id.tabOffer_radioGroup);
		this.tabOffer_radioGroup
				.setOnCheckedChangeListener(new OnCheckedChangeListener() {

					@Override
					public void onCheckedChanged(RadioGroup group, int checkedId) {
						refreshStockList();
						onOfferedStockSelected();
						ActivityMain.this.tabOffer_seekBarAmount
								.setEnabled(true);
						ActivityMain.this.tabOffer_buttonOfferSend
								.setEnabled(true);
					}
				});
		this.tabOffer_listStocks = (Spinner) this
				.findViewById(R.id.tabOffer_listStocks);
		this.tabOffer_seekBarAmount = (SeekBar) this
				.findViewById(R.id.tabOffer_seekBarAmount);
		this.tabOffer_seekBarAmount
				.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {

					@Override
					public void onStopTrackingTouch(SeekBar seekBar) {
					}

					@Override
					public void onStartTrackingTouch(SeekBar seekBar) {
					}

					@Override
					public void onProgressChanged(SeekBar seekBar,
							int progress, boolean fromUser) {
						ActivityMain.this.tabOffer_textAmount.setText(progress
								+ 1 + "");
					}
				});
		this.tabOffer_textAmount = (TextView) this
				.findViewById(R.id.tabOffer_textAmount);
		this.tabOffer_editTextUnitPrice = (EditText) this
				.findViewById(R.id.tabOffer_textUnitPrice);
		this.tabOffer_editTextUnitPrice
				.addTextChangedListener(new TextWatcher() {

					@Override
					public void onTextChanged(CharSequence s, int start,
							int before, int count) {
					}

					@Override
					public void beforeTextChanged(CharSequence s, int start,
							int count, int after) {
					}

					@Override
					public void afterTextChanged(Editable s) {
						if (ActivityMain.this.tabOffer_radioGroup
								.getCheckedRadioButtonId() == R.id.tabOffer_radioBuy) {
							NumberFormat format = NumberFormat
									.getInstance(Locale.getDefault());
							try {
								double price = format.parse(s.toString())
										.doubleValue();
								int max = (int) (ActivityMain.this.mClient
										.getOwnTeam().getMoney() / price);
								if (max == 0) {
									ActivityMain.this.tabOffer_seekBarAmount
											.setMax(0);
									ActivityMain.this.tabOffer_seekBarAmount
											.setProgress(0);
									ActivityMain.this.tabOffer_seekBarAmount
											.setEnabled(false);
									ActivityMain.this.tabOffer_buttonOfferSend
											.setEnabled(false);
								} else {
									ActivityMain.this.tabOffer_seekBarAmount
											.setMax(max - 1);
									ActivityMain.this.tabOffer_seekBarAmount
											.setProgress(0);
									ActivityMain.this.tabOffer_seekBarAmount
											.setEnabled(true);
									ActivityMain.this.tabOffer_buttonOfferSend
											.setEnabled(true);
								}
							} catch (ParseException e) {
								ActivityMain.this.tabOffer_seekBarAmount
										.setMax(0);
								ActivityMain.this.tabOffer_seekBarAmount
										.setProgress(0);
							}
						}
					}
				});
		this.tabOffer_buttonOfferSend = (Button) this
				.findViewById(R.id.tabOffer_buttonOfferSend);
		this.tabOffer_buttonOfferSend
				.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						sendOffer();
					}
				});

		// this.tabAccept_listOffers = (ListView) this
		// .findViewById(R.id.tabAccept_listOffers);
		// this.tabAccept_listOffers.setAdapter(new OfferAdapter());
		// this.tabAccept_listOffers
		// .setOnItemClickListener(new AdapterView.OnItemClickListener() {
		// @Override
		// public void onItemClick(AdapterView<?> parent, View view,
		// int position, long id) {
		// onClickOffer(position);
		// }
		// });

		this.tabHost.addTab(this.tabMain);
		this.tabHost.addTab(this.tabStocks);
		this.tabHost.addTab(this.tabOffer);
		this.tabHost.addTab(this.tabOutgoing);

		// TODO Unregister listener
		this.mClient.addIClientListener(this);

	}

	@Override
	protected void onStart() {
		super.onStart();
		if (zerothRoundStarted && !zerothRoundDone) { // ActivityZerothRound was
														// cancelled
			finish();
		} else if (!zerothRoundStarted) {
			ActivityMain.this.startActivityForResult(new Intent(
					ActivityMain.this, ActivityZerothRound.class),
					ActivityZerothRound.REQUEST_CODE);
			zerothRoundStarted = true;
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == ActivityZerothRound.REQUEST_CODE) {
			zerothRoundDone = resultCode == Activity.RESULT_OK;
		}
	}

	@Override
	public void onBackPressed() {
		new AlertDialog.Builder(this)
				.setTitle(R.string.exit_question)
				.setMessage(R.string.exit_message)
				.setPositiveButton(R.string.yes,
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								ActivityMain.super.onBackPressed();
							}
						})
				.setNegativeButton(R.string.no,
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog,
									int which) {

							}
						}).create().show();
	}

	@Override
	public void onConnect(TCPClient client) {
	}

	@Override
	public void onErrorCommand(CmdServerError error) {

		switch (error.errorId) {
		case CmdServerError.ERROR_OFFER:
			runOnUiThread(new Runnable() {
				@Override
				public void run() {
					Toast.makeText(ActivityMain.this.getApplicationContext(),
							R.string.not_enough_money_or_stock,
							Toast.LENGTH_LONG).show();
				}
			});
			break;
		}
		return;
	}

	private void refreshTeamList() {
		this.tabOffer_spinnerPosition2TeamIndex = new ArrayList<Integer>();
		List<String> nameList = new ArrayList<String>();
		for (int t = 0; t < mClient.getModel().teams.size(); t++)
			if (mClient.getModel().teams.get(t).id != mClient.getOwnTeam().id) {
				nameList.add(mClient.getModel().teams.get(t).name);
				this.tabOffer_spinnerPosition2TeamIndex.add(t);
			}

	}

	private void sendOffer() {

		try {
			double price = NumberFormat.getInstance()
					.parse(tabOffer_editTextUnitPrice.getText().toString())
					.doubleValue();

			mClient.sendOffer(
					tabOffer_spinnerPosition2StockIndex.get(tabOffer_listStocks
							.getSelectedItemPosition()),
					tabOffer_seekBarAmount.getProgress(),
					price,
					tabOffer_radioGroup.getCheckedRadioButtonId() == R.id.tabOffer_radioSell);
		} catch (ParseException e) {
			new AlertDialog.Builder(this)
					.setMessage(R.string.tabOffer_bad_number_format)
				.setPositiveButton(R.string.ok,null)
				.create().show();
		}

	}

	private void refreshStockList() {

		// Offer tab
		tabOffer_spinnerPosition2StockIndex = new ArrayList<Integer>();
		List<String> nameList = new ArrayList<String>();

		if (this.tabOffer_radioGroup.getCheckedRadioButtonId() == R.id.tabOffer_radioSell) { // Only
																								// show
																								// those
																								// stocks
																								// that
																								// we
																								// possess
			for (int i = 0; i < mClient.getModel().stocks.length; i++)
				if (mClient.getOwnTeam().getStock(i) > 0) {
					nameList.add(mClient.getModel().stocks[i].name);
					tabOffer_spinnerPosition2StockIndex.add(i);
				}
		} else {
			// Show all stocks
			for (int i = 0; i < mClient.getModel().stocks.length; i++) {
				nameList.add(mClient.getModel().stocks[i].name);
				tabOffer_spinnerPosition2StockIndex.add(i);
			}
			/*
			 * for (int i = 0; i < mClient.getModel().stockList.length; i++) if
			 * (mClient.getOwnTeam().getMoney() >=
			 * mClient.getModel().stockList[i].value) {
			 * nameList.add(mClient.getModel().stockList[i].name);
			 * tabOffer_spinnerPosition2StockIndex.add(i); }
			 */
		}

		this.tabOffer_listStocks.setAdapter(new ArrayAdapter<String>(this,
				android.R.layout.simple_spinner_item, nameList));
		this.tabOffer_seekBarAmount.setEnabled(!nameList.isEmpty());
		this.tabOffer_buttonOfferSend.setEnabled(!nameList.isEmpty());
		this.tabOffer_listStocks
				.setOnItemSelectedListener(new OnItemSelectedListener() {

					@Override
					public void onItemSelected(AdapterView<?> parent,
							View view, int position, long id) {
						onOfferedStockSelected();
						/*
						 * ActivityMain.this.tabOffer_buttonOfferSend
						 * .setEnabled(true);
						 * ActivityMain.this.tabOffer_seekBarAmount
						 * .setEnabled(true);
						 */
					}

					@Override
					public void onNothingSelected(AdapterView<?> parent) {
						// Does not work :(
						/*
						 * ActivityMain.this.tabOffer_buttonOfferSend
						 * .setEnabled(false);
						 * ActivityMain.this.tabOffer_seekBarAmount
						 * .setEnabled(false);
						 */
					}
				});
		// Stocks tab
		this.tabStocks_listStocks.setAdapter(new StockAdapter());
	}

	private void onOfferedStockSelected() {
		if (this.tabOffer_listStocks.getSelectedItemPosition() == AdapterView.INVALID_POSITION) {
			this.tabOffer_seekBarAmount.setMax(0);
			this.tabOffer_seekBarAmount.setProgress(0);
		} else {
			int selectedStockIndex = this.tabOffer_spinnerPosition2StockIndex
					.get(this.tabOffer_listStocks.getSelectedItemPosition());
			if (this.tabOffer_radioGroup.getCheckedRadioButtonId() == R.id.tabOffer_radioSell) { // Set
																									// max
																									// according
																									// to
																									// possessed
																									// stocks
				this.tabOffer_seekBarAmount.setProgress(0);
				this.tabOffer_seekBarAmount.setMax(this.mClient.getOwnTeam()
						.getStock(selectedStockIndex) - 1);
			}
			// Set unit price to the stock's value
			this.tabOffer_editTextUnitPrice
					.setText(DECIMAL_FORMAT.format(mClient.getModel().stocks[selectedStockIndex].value));

		}
	}

	@Override
	public void onClose(TCPClient client) {
		this.runOnUiThread(new Runnable() {
			@Override
			public void run() {
				ActivityMain.this.finish();
			}
		});
	}

	@Override
	protected void onDestroy() {
		Log.d(this.getClass().getName(), "onDestroy()");
		this.mClient.disconnect();
		Log.d(this.getClass().getName(), "Disconnect message has been sent");
		super.onDestroy();
	}

	@Override
	public void onConnectionFail(TCPClient client, IOException exception) {
		Log.d(this.getClass().getName(), "connection failed");
	}

	@Override
	public void onStocksCommand(final ExchangeClient client) {
		// this.runOnUiThread(new Runnable() {
		// public void run() {
		// ActivityMain.this.refreshStockList(client.getModel());
		// }
		// });
	}

	@Override
	public void onTeamsCommand(final ExchangeClient client) {
		this.runOnUiThread(new Runnable() {
			public void run() {
				ActivityMain.this.refreshTeamList();
				ActivityMain.this.refreshStockList();
			}
		});
	}

	@Override
	public void onRoundCommand(ExchangeClient client) {
		this.runOnUiThread(new Runnable() {
			public void run() {
				// Toast.makeText(ActivityMain.this, R.string.new_round,
				// Toast.LENGTH_LONG).show();
				new AlertDialog.Builder(ActivityMain.this)
						.setTitle(R.string.new_round)
						.setMessage(
								ExchangeClient.getInstance().getModel().eventMessage)
						.setNeutralButton("Ok", null).show();
				((TextView) ActivityMain.this
						.findViewById(R.id.tabMain_eventMessage))
						.setText(ExchangeClient.getInstance().getModel().eventMessage);
				refreshStockList();
			}
		});
	}

	@Override
	public void onMoneyChanged(final Team ownTeam) {
		runOnUiThread(new Runnable() {
			public void run() {
				((TextView) findViewById(R.id.tabMain_money))
						.setText(DECIMAL_FORMAT.format(ownTeam.getMoney()));
			}
		});
	}

	@Override
	public void onStocksChanged(final Team ownTeam, int position) {

		runOnUiThread(new Runnable() {
			public void run() {
				((TextView) findViewById(R.id.tabMain_valueOfStocks))
						.setText(DECIMAL_FORMAT.format(ownTeam
								.getStockValue(mClient.getModel())));
			}
		});
	}

	private class StockAdapter extends BaseAdapter {

		private Stock[] stocks;

		public StockAdapter() {
			stocks = mClient.getModel().stocks;
		}

		@Override
		public int getCount() {
			return stocks.length;
		}

		@Override
		public Object getItem(int position) {
			return stocks[position];
		}

		@Override
		public long getItemId(int position) {
			return 0;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			View out;
			if (convertView == null)
				out = getLayoutInflater().inflate(
						R.layout.activity_main_tab_stocks_list_item, parent,
						false);
			else
				out = convertView;

			Stock stock = (Stock) getItem(position);

			((TextView) out.findViewById(R.id.tabIncoming_offerName))
					.setText(stock.name);

			((TextView) out.findViewById(R.id.tabIncoming_offerValue))
					.setText(DECIMAL_FORMAT.format(stock.value));

			((TextView) out.findViewById(R.id.tabIncoming_offerAmount))
					.setText(mClient.getOwnTeam().getStock(position) + "");

			TextView change = (TextView) out
					.findViewById(R.id.tabStocks_stockChange);

			DecimalFormat df = new DecimalFormat("+#0.00;-#");
			int colorID;
			if (stock.change > 1) {
				colorID = R.color.stock_change_increase;
			} else if (stock.change == 1) {
				colorID = R.color.stock_change_stagnation;
			} else {
				colorID = R.color.stock_change_decrease;
			}
			change.setText(df.format((stock.change - 1.0) * 100) + "%");

			change.setTextColor(getResources().getColor(colorID));

			return out;
		}

	}

	private class OutgoingAdapter extends BaseAdapter {

		@Override
		public int getCount() {
			// TODO Auto-generated method stub
			return 0;
		}

		@Override
		public Object getItem(int position) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public long getItemId(int position) {
			// TODO Auto-generated method stub
			return 0;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			// TODO Auto-generated method stub
			return null;
		}

	}

}
