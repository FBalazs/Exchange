package hu.berzsenyi.exchange.client;

import java.util.ArrayList;
import java.util.List;

import hu.berzsenyi.exchange.Model;
import hu.berzsenyi.exchange.net.IClientListener;
import hu.berzsenyi.exchange.net.TCPClient;
import hu.berzsenyi.exchange.net.TCPConnection;
import hu.berzsenyi.exchange.net.cmd.CmdClientDisconnect;
import hu.berzsenyi.exchange.net.cmd.CmdClientInfo;
import hu.berzsenyi.exchange.net.cmd.CmdClientOfferResponse;
import hu.berzsenyi.exchange.net.cmd.CmdOffer;
import hu.berzsenyi.exchange.net.cmd.CmdServerInfo;
import hu.berzsenyi.exchange.net.cmd.ICmdHandler;
import hu.berzsenyi.exchange.net.cmd.TCPCommand;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TabHost;
import android.widget.TabHost.TabSpec;

public class ActivityMain extends Activity implements IClientListener, ICmdHandler {
	public static class TCPConnectThread extends Thread {
		public ActivityMain client;
		
		public TCPConnectThread(ActivityMain client) {
			super("Thread-TCPConnect");
			this.client = client;
		}
		
		@Override
		public void run() {
			this.client.net = new TCPClient(this.client.getIntent().getStringExtra(EXTRA_IP), this.client.getIntent().getIntExtra(EXTRA_PORT, -1), this.client, this.client);
			this.client.net.writeCommand(new CmdClientInfo(this.client.name));
		}
	}
	
	
	
	public static final String EXTRA_NAME = "strName",
								EXTRA_IP = "strIP",
								EXTRA_PORT = "intPort";
	
	public boolean running;
	
	public TabHost tabHost;
	public TabSpec tabMain, tabStocks, tabOffer, tabAccept;
	
	public ListView tabStocks_listStocks;
	
	public Spinner tabOffer_listTeams, tabOffer_listStocks;
	public Button tabOffer_buttonOffer;
	
	public ListView tabAccept_listOffers;
	public List<CmdOffer> offersIn = new ArrayList<CmdOffer>();
	public List<String> offersInStrings = new ArrayList<String>();
	
	public String name;
	public TCPClient net;
	public Model model;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		Log.d(this.getClass().getName(), "onCreate()");
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.activity_main);
		
		this.tabHost = (TabHost)this.findViewById(R.id.tabHost);
		this.tabHost.setup();
		
		this.tabMain = this.tabHost.newTabSpec("Overview");
		this.tabMain.setContent(R.id.tabMain);
		this.tabMain.setIndicator("Overview");
		
		this.tabStocks = this.tabHost.newTabSpec("Stocks");
		this.tabStocks.setContent(R.id.tabStocks);
		this.tabStocks.setIndicator("Stocks");
		
		this.tabOffer = this.tabHost.newTabSpec("Offer");
		this.tabOffer.setContent(R.id.tabOffer);
		this.tabOffer.setIndicator("Offer");
		
		this.tabAccept = this.tabHost.newTabSpec("Incoming");
		this.tabAccept.setContent(R.id.tabAccept);
		this.tabAccept.setIndicator("Incoming");
		
		this.tabStocks_listStocks = (ListView)this.findViewById(R.id.tabStocks_listStocks);
		
		this.tabOffer_listTeams = (Spinner)this.findViewById(R.id.tabOffer_listTeams);
		this.tabOffer_listStocks = (Spinner)this.findViewById(R.id.tabOffer_listStocks);
		this.tabOffer_buttonOffer = (Button)this.findViewById(R.id.tabOffer_buttonOffer);
		this.tabOffer_buttonOffer.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				onClickButtonOffer();
			}
		});
		
		this.tabAccept_listOffers = (ListView)this.findViewById(R.id.tabAccept_listOffers);
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
		
		this.name = this.getIntent().getStringExtra(EXTRA_NAME);
		new TCPConnectThread(this).start();
	}
	
	@Override
	public void onConnect(TCPClient client) {
		this.running = true;
		// new UpdateThread(this).start();
	}
	
	@Override
	public void handleCmd(TCPCommand cmd, TCPConnection conn) {
		Log.d(this.getClass().getName(), "Received command! "+cmd.getClass().getName());
		
		if(cmd instanceof CmdServerInfo) {
			this.setModel(((CmdServerInfo)cmd).model);
			return;
		}
		
		if(cmd instanceof CmdOffer) {
			CmdOffer offer = (CmdOffer)cmd;
			this.offersIn.add(offer);
			this.offersInStrings.add(this.model.getTeamById(offer.playerID).name+" wants "+this.model.stockList[offer.stockID].name+", "+offer.amount+" for "+offer.money);
			this.tabAccept_listOffers.setAdapter(this.tabAccept_listOffers.getAdapter());
			return;
		}
	}
	
	public void onClickButtonOffer() {
		// TODO send offer message
		this.net.writeCommand(new CmdOffer(this.model.teams.get(this.tabOffer_listTeams.getSelectedItemPosition()).id, this.tabOffer_listStocks.getSelectedItemPosition(), 1, 1.0));
	}
	
	public void onOfferAccept(int pos) {
		CmdOffer offer = this.offersIn.get(pos);
		this.net.writeCommand(new CmdClientOfferResponse(offer.playerID, offer.stockID, offer.amount, offer.money));
		this.offersIn.remove(pos);
		this.offersInStrings.remove(pos);
		this.tabAccept_listOffers.setAdapter(this.tabAccept_listOffers.getAdapter());
	}
	
	public void onOfferDeny(int pos) {
		this.offersIn.remove(pos);
		this.offersInStrings.remove(pos);
		this.tabAccept_listOffers.setAdapter(this.tabAccept_listOffers.getAdapter());
	}
	
	public void onClickOffer(final int offer) {
		new AlertDialog.Builder(this).setMessage("Do you want to accept this offer? "+this.offersInStrings.get(offer)).setPositiveButton("Yes", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				onOfferAccept(offer);
			}
		}).setNegativeButton("No", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				onOfferDeny(offer);
			}
		}).show();
	}
	
	public void setModel(Model model) {
		this.model = model;
		
		String[] array;
		
		array = new String[this.model.stockList.length];
		for(int i = 0; i < array.length; i++)
			array[i] = this.model.stockList[i].name+" "+this.model.stockList[i].value;
		this.tabStocks_listStocks.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, array));
		
		array = new String[this.model.teams.size()];
		for(int i = 0; i < array.length; i++)
			array[i] = this.model.teams.get(i).name;
		this.tabOffer_listTeams.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, array));
		array = new String[this.model.stockList.length];
		for(int i = 0; i < array.length; i++)
			array[i] = this.model.stockList[i].name;
		this.tabOffer_listStocks.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, array));
		
		this.tabAccept_listOffers.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, this.offersInStrings));
	}
	

	@Override
	public void onClose(TCPClient client) {
		this.running = false;
		this.finish();
	}
	
	@Override
	protected void onDestroy() {
		Log.d(this.getClass().getName(), "onDestroy()");
		if(this.net != null) {
			this.net.writeCommand(new CmdClientDisconnect());
			this.net.close();
		}
		super.onDestroy();
	}
}
