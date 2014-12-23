package hu.berzsenyi.exchange.client;

import java.util.ArrayList;
import java.util.List;

import hu.berzsenyi.exchange.Model;
import hu.berzsenyi.exchange.net.IClientListener;
import hu.berzsenyi.exchange.net.TCPClient;
import hu.berzsenyi.exchange.net.TCPConnection;
import hu.berzsenyi.exchange.net.cmd.CmdClientDisconnect;
import hu.berzsenyi.exchange.net.cmd.CmdClientInfo;
import hu.berzsenyi.exchange.net.cmd.CmdOffer;
import hu.berzsenyi.exchange.net.cmd.CmdServerInfo;
import hu.berzsenyi.exchange.net.cmd.ICmdHandler;
import hu.berzsenyi.exchange.net.cmd.TCPCommand;
import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
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
	
	public static class UpdateThread extends Thread {
		public ActivityMain client;
		
		public UpdateThread(ActivityMain client) {
			super("Thread-Update");
			this.client = client;
		}
		
		@Override
		public void run() {
			while(this.client.running) {
				long time = System.currentTimeMillis();
				this.client.update();
				time = 1000/25-(System.currentTimeMillis()-time);
				if(0 < time)
					try {
						Thread.sleep(time);
					} catch(InterruptedException e) {
						e.printStackTrace();
					}
			}
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
		new UpdateThread(this).start();
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
			this.offersInStrings.add(this.model.getTeamById(offer.senderID).name+" wants "+this.model.stockList[offer.stockID].name+", "+offer.amount+" for "+offer.money);
			return;
		}
	}
	
	public void onClickButtonOffer() {
		// TODO send offer message
		this.net.writeCommand(new CmdOffer(this.model.getTeamByName(this.name).id, this.model.teams.get(this.tabOffer_listTeams.getSelectedItemPosition()).id, this.tabOffer_listStocks.getSelectedItemPosition(), 1, 1.0));
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
	
	public void update() {
		this.net.update();
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
