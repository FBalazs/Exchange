package hu.berzsenyi.exchange.client;

import hu.berzsenyi.exchange.Model;
import hu.berzsenyi.exchange.net.TCPClient;
import hu.berzsenyi.exchange.net.cmd.CmdOffer;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TabHost;
import android.widget.TabHost.TabSpec;

import java.io.IOException;
import android.content.Intent;

public class ActivityMain extends Activity implements IClientListener {

	protected static final String EXTRA_NAME = "strName", EXTRA_IP = "strIP", EXTRA_PORT = "intPort";

	protected static final String TAG_OVERVIEW = "overview", TAG_STOCKS = "stocks", TAG_OFFER = "offer", TAG_INCOMING = "incoming";

	private TabHost tabHost;
	private TabSpec tabMain, tabStocks, tabOffer, tabAccept;

	private ListView tabStocks_listStocks;

	private Spinner tabOffer_listTeams, tabOffer_listStocks;
	private Button tabOffer_buttonOffer;

	private ListView tabAccept_listOffers;

	private ExchangeClient client = ExchangeClient.getInstance();

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

		this.tabStocks_listStocks = (ListView) this.findViewById(R.id.tabStocks_listStocks);

		this.tabOffer_listTeams = (Spinner) this.findViewById(R.id.tabOffer_listTeams);
		this.tabOffer_listStocks = (Spinner) this.findViewById(R.id.tabOffer_listStocks);
		this.tabOffer_buttonOffer = (Button) this.findViewById(R.id.tabOffer_buttonOffer);
		this.tabOffer_buttonOffer.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				onClickButtonOffer();
			}
		});

		this.tabAccept_listOffers = (ListView) this.findViewById(R.id.tabAccept_listOffers);
		this.tabAccept_listOffers.setAdapter(new ArrayAdapter<CmdOffer>(this, android.R.layout.simple_spinner_item, this.client.offersIn));
		this.tabAccept_listOffers.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> adapter, View view, int pos, long id) {
				onClickOffer(pos);
			}
		});

		this.tabHost.addTab(this.tabMain);
		this.tabHost.addTab(this.tabStocks);
		this.tabHost.addTab(this.tabOffer);
		this.tabHost.addTab(this.tabAccept);

		this.client.addIClientListener(this);

		ActivityMain.this.startActivityForResult(new Intent(ActivityMain.this, ActivityZerothRound.class), ActivityZerothRound.REQUEST_CODE);
		Log.d(ActivityMain.class.getName(), "ActivityZerothRound has been started");

	}

	@Override
	protected void onStart() {
		super.onStart();
		if (zerothRoundStarted && !zerothRoundDone) { // ActivityZerothRound was
														// cancelled
			finish();
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
		this.client.offer(this.client.getModel().teams.get(this.tabOffer_listTeams.getSelectedItemPosition()).id,
				this.tabOffer_listStocks.getSelectedItemPosition(), 1, -1.0);
	}

	public void onOfferAccept(int pos) {
		this.client.acceptOffer(pos);
		((BaseAdapter) this.tabAccept_listOffers.getAdapter()).notifyDataSetChanged();
		this.tabAccept_listOffers.invalidate();
	}

	public void onOfferDeny(int pos) {
		this.client.denyOffer(pos);
		((BaseAdapter) this.tabAccept_listOffers.getAdapter()).notifyDataSetChanged();
		this.tabAccept_listOffers.invalidate();
	}

	public void onClickOffer(final int offer) {
		new AlertDialog.Builder(this)
				.setMessage(
						this.getString(R.string.accept_offer_question)
								+ ExchangeClient.getInstance().getOffer(offer).toString(this.client.getModel()))
				.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						onOfferAccept(offer);
					}
				}).setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						onOfferDeny(offer);
					}
				}).show();
	}

	public void refreshTeamList(Model model) {
		String[] array = new String[model.teams.size()];
		for (int i = 0; i < array.length; i++)
			array[i] = model.teams.get(i).name;
		this.tabOffer_listTeams.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, array));
	}

	public void refreshStockList(Model model) {
		String[] array = new String[model.stockList.length];
		for (int i = 0; i < array.length; i++)
			array[i] = model.stockList[i].name;
		this.tabOffer_listStocks.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, array));
		this.tabStocks_listStocks.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, array));
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
		this.client.disconnect();
		Log.d(this.getClass().getName(), "Disconnect message has been sent");
		super.onDestroy();
	}

	@Override
	public void onConnectionFail(TCPClient client, IOException exception) {
		Log.d(this.getClass().getName(), "connection failed");
	}

	@Override
	public void onStocksCommand(final ExchangeClient client) {
//		this.runOnUiThread(new Runnable() {
//			public void run() {
//				ActivityMain.this.refreshStockList(client.getModel());
//			}
//		});
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
				ActivityMain.this.tabAccept_listOffers.setAdapter(ActivityMain.this.tabAccept_listOffers.getAdapter());
			}
		});
	}

	@Override
	public void onRoundCommand(ExchangeClient client) {
		
	}
}
