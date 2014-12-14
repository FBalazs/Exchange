package hu.berzsenyi.exchange.client;

import hu.berzsenyi.exchange.Model;
import hu.berzsenyi.exchange.net.IClientListener;
import hu.berzsenyi.exchange.net.TCPClient;
import hu.berzsenyi.exchange.net.cmd.CmdClientDisconnect;
import hu.berzsenyi.exchange.net.cmd.CmdClientInfo;
import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TabHost;
import android.widget.TabHost.TabSpec;

public class ActivityMain extends Activity implements IClientListener {
	public static class TCPConnectThread extends Thread {
		public ActivityMain client;
		
		public TCPConnectThread(ActivityMain client) {
			super("Thread-TCPConnect");
			this.client = client;
		}
		
		@Override
		public void run() {
			this.client.net = new TCPClient(this.client.getIntent().getStringExtra(EXTRA_IP), this.client.getIntent().getIntExtra(EXTRA_PORT, -1), new CmdHandlerClient(this.client), this.client);
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
	public TabSpec tabSpecMain, tabStocks, tabSpecExchange;
	
	public Spinner listTeams, listStocks;
	public Button buttonOffer;
	
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
		
		this.tabSpecMain = this.tabHost.newTabSpec("Overview");
		this.tabSpecMain.setContent(R.id.tabMain);
		this.tabSpecMain.setIndicator("Overview");
		
		this.tabStocks = this.tabHost.newTabSpec("Stocks");
		this.tabStocks.setContent(R.id.tabStocks);
		this.tabStocks.setIndicator("Stocks");
		
		this.tabSpecExchange = this.tabHost.newTabSpec("Exchange");
		this.tabSpecExchange.setContent(R.id.tabExchange);
		this.tabSpecExchange.setIndicator("Exchange");
		
		this.listTeams = (Spinner)this.findViewById(R.id.listTeams);
		this.listStocks = (Spinner)this.findViewById(R.id.listStocks);
		this.buttonOffer = (Button)this.findViewById(R.id.buttonOffer);
		this.buttonOffer.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				onClickButtonOffer();
			}
		});
		
		this.tabHost.addTab(this.tabSpecMain);
		this.tabHost.addTab(this.tabStocks);
		this.tabHost.addTab(this.tabSpecExchange);
		
		this.name = this.getIntent().getStringExtra(EXTRA_NAME);
		new TCPConnectThread(this).start();
	}
	
	@Override
	public void onConnect(TCPClient client) {
		this.running = true;
		new UpdateThread(this).start();
	}
	
	public void onClickButtonOffer() {
		// TODO send offer message
	}
	
	public void setModel(Model model) {
		this.model = model;
		
		String[] array = new String[this.model.teams.size()];
		for(int i = 0; i < array.length; i++)
			array[i] = this.model.teams.get(i).name;
		this.listTeams.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, array));
		
		array = new String[this.model.stockList.length];
		for(int i = 0; i < array.length; i++)
			array[i] = this.model.stockList[i].name;
		this.listStocks.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, array));
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
