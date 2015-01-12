package hu.berzsenyi.exchange.client;

import hu.berzsenyi.exchange.Model;
import hu.berzsenyi.exchange.Stock;
import hu.berzsenyi.exchange.Team;
import hu.berzsenyi.exchange.net.TCPClient;
import hu.berzsenyi.exchange.net.cmd.CmdOffer;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.Spinner;
import android.widget.TabHost;
import android.widget.TabHost.TabSpec;
import android.widget.TextView;

import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import android.content.Intent;

public class ActivityMain extends Activity implements IClientListener {

	protected static final String EXTRA_NAME = "strName", EXTRA_IP = "strIP",
			EXTRA_PORT = "intPort";

	protected static final String TAG_OVERVIEW = "overview",
			TAG_STOCKS = "stocks", TAG_OFFER = "offer",
			TAG_INCOMING = "incoming";

	private TabHost tabHost;
	private TabSpec tabMain, tabStocks, tabOffer, tabAccept;

	private ListView tabStocks_listStocks;

	private Spinner tabOffer_listTeams, tabOffer_listStocks;
	private Button tabOffer_buttonOfferSend;
	private SeekBar tabOffer_seekBarAmount;
	private TextView tabOffer_textAmount;
	private RadioGroup tabOffer_radioGroup;

	private ListView tabAccept_listOffers;

	private List<Integer> possessedStockList;

	private ExchangeClient mClient = ExchangeClient.getInstance();

	private boolean zerothRoundDone = false, zerothRoundStarted = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		Log.d(this.getClass().getName(), "onCreate()");
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.activity_main);

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

		this.tabAccept = this.tabHost.newTabSpec(TAG_INCOMING);
		this.tabAccept.setContent(R.id.tabAccept);
		this.tabAccept.setIndicator(this.getString(R.string.incoming));

		this.tabStocks_listStocks = (ListView) this
				.findViewById(R.id.tabStocks_listStocks);

		this.tabOffer_radioGroup = (RadioGroup) findViewById(R.id.tabOffer_radioGroup);
		this.tabOffer_listTeams = (Spinner) this
				.findViewById(R.id.tabOffer_listTeams);
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
		this.tabOffer_buttonOfferSend = (Button) this
				.findViewById(R.id.tabOffer_buttonOfferSend);
		this.tabOffer_buttonOfferSend
				.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						onClickButtonOffer();
					}
				});

		this.tabAccept_listOffers = (ListView) this
				.findViewById(R.id.tabAccept_listOffers);
		this.tabAccept_listOffers.setAdapter(new ArrayAdapter<CmdOffer>(this,
				android.R.layout.simple_spinner_item, this.mClient.offersIn));
		this.tabAccept_listOffers
				.setOnItemClickListener(new AdapterView.OnItemClickListener() {
					@Override
					public void onItemClick(AdapterView<?> adapter, View view,
							int pos, long id) {
						onClickOffer(pos);
					}
				});

		this.tabHost.addTab(this.tabMain);
		this.tabHost.addTab(this.tabStocks);
		this.tabHost.addTab(this.tabOffer);
		this.tabHost.addTab(this.tabAccept);

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
	public void onConnect(TCPClient client) {
	}

	public void onClickButtonOffer() {
		this.mClient
				.offer(this.mClient.getModel().teams
						.get(this.tabOffer_listTeams.getSelectedItemPosition()).id,
						this.tabOffer_listStocks.getSelectedItemPosition(),
						tabOffer_seekBarAmount.getProgress() + 1,
						-1.0,
						this.tabOffer_radioGroup.getCheckedRadioButtonId() == R.id.tabOffer_radioSell);
	}

	public void acceptOffer(int pos) {
		this.mClient.acceptOffer(pos);
		((BaseAdapter) this.tabAccept_listOffers.getAdapter())
				.notifyDataSetChanged();
		this.tabAccept_listOffers.invalidate();
	}

	public void denyOffer(int pos) {
		this.mClient.denyOffer(pos);
		((BaseAdapter) this.tabAccept_listOffers.getAdapter())
				.notifyDataSetChanged();
		this.tabAccept_listOffers.invalidate();
	}

	public void onClickOffer(final int offer) {
		new AlertDialog.Builder(this)
				.setMessage(
						this.getString(R.string.accept_offer_question)
								+ ExchangeClient.getInstance().getOffer(offer)
										.toString(this.mClient.getModel()))
				.setPositiveButton(R.string.yes,
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								acceptOffer(offer);
							}
						})
				.setNegativeButton(R.string.no,
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								denyOffer(offer);
							}
						}).show();
	}

	public void refreshTeamList(Model model) {
		String[] array = new String[model.teams.size()];
		for (int i = 0; i < array.length; i++)
			array[i] = model.teams.get(i).name;
		this.tabOffer_listTeams.setAdapter(new ArrayAdapter<String>(this,
				android.R.layout.simple_spinner_item, array));
	}

	public void refreshStockList(Model model) {

		possessedStockList = new ArrayList<Integer>();
		List<String> nameList = new ArrayList<String>();
		for (int i = 0; i < mClient.getModel().stockList.length; i++)
			if (mClient.getOwnTeam().getStock(i) > 0) {
				nameList.add(model.stockList[i].name);
				possessedStockList.add(i);
			}

		this.tabOffer_listStocks.setAdapter(new ArrayAdapter<String>(this,
				android.R.layout.simple_spinner_item, nameList));
		this.tabOffer_listStocks
				.setOnItemSelectedListener(new OnItemSelectedListener() {

					@Override
					public void onItemSelected(AdapterView<?> parent,
							View view, int position, long id) {
						refreshAmountMax();
					}

					@Override
					public void onNothingSelected(AdapterView<?> parent) {
					}
				});
		this.tabStocks_listStocks.setAdapter(new StockAdapter());
	}

	private void refreshAmountMax() {
		ActivityMain.this.tabOffer_seekBarAmount.setProgress(0);
		int selectedStockIndex = this.possessedStockList
				.get(this.tabOffer_listStocks.getSelectedItemPosition());
		ActivityMain.this.tabOffer_seekBarAmount
				.setMax((this.tabOffer_radioGroup.getCheckedRadioButtonId() == R.id.tabOffer_radioSell ? this.mClient
						.getOwnTeam().getStock(selectedStockIndex)
						: (int) (this.mClient.getOwnTeam().getMoney() / this.mClient
								.getModel().stockList[selectedStockIndex].value)) - 1);
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
				ActivityMain.this.refreshTeamList(client.getModel());
				ActivityMain.this.refreshStockList(client.getModel());
			}
		});
	}

	@Override
	public void onOfferIn(ExchangeClient client, CmdOffer offer) {
		this.runOnUiThread(new Runnable() {
			public void run() {
				ActivityMain.this.tabAccept_listOffers
						.setAdapter(ActivityMain.this.tabAccept_listOffers
								.getAdapter());
			}
		});
	}

	@Override
	public void onRoundCommand(ExchangeClient client) {
		this.runOnUiThread(new Runnable() {
			public void run() {
				((TextView) ActivityMain.this
						.findViewById(R.id.tabMain_eventMessage))
						.setText(ExchangeClient.getInstance().getModel().eventMessage);
				refreshStockList(mClient.getModel());
			}
		});
	}

	@Override
	public void onMoneyChanged(final Team ownTeam) {
		runOnUiThread(new Runnable() {
			public void run() {
				((TextView) findViewById(R.id.tabMain_money)).setText(""
						+ ownTeam.getMoney());
			}
		});
	}

	@Override
	public void onStocksChanged(final Team ownTeam, int position) {

		runOnUiThread(new Runnable() {
			public void run() {
				((TextView) findViewById(R.id.tabMain_valueOfStocks))
						.setText("" + ownTeam.getStockValue(mClient.getModel()));
			}
		});
	}

	private class StockAdapter extends BaseAdapter {

		private Stock[] stocks;

		public StockAdapter() {
			stocks = mClient.getModel().stockList;
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

			((TextView) out.findViewById(R.id.tabStocks_stockName))
					.setText(stock.name);

			((TextView) out.findViewById(R.id.tabStocks_stockValue))
					.setText(new DecimalFormat("#0.00").format(stock.value)
							+ "");

			((TextView) out.findViewById(R.id.tabStocks_stockAmount))
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
}
