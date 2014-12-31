package hu.berzsenyi.exchange.client;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import android.util.Log;
import hu.berzsenyi.exchange.Model;
import hu.berzsenyi.exchange.net.IClientConnectionListener;
import hu.berzsenyi.exchange.net.TCPClient;
import hu.berzsenyi.exchange.net.TCPConnection;
import hu.berzsenyi.exchange.net.cmd.CmdClientDisconnect;
import hu.berzsenyi.exchange.net.cmd.CmdClientInfo;
import hu.berzsenyi.exchange.net.cmd.CmdOffer;
import hu.berzsenyi.exchange.net.cmd.CmdOfferResponse;
import hu.berzsenyi.exchange.net.cmd.CmdServerStocks;
import hu.berzsenyi.exchange.net.cmd.CmdServerTeams;
import hu.berzsenyi.exchange.net.cmd.ICmdHandler;
import hu.berzsenyi.exchange.net.cmd.TCPCommand;

/**
 * Singleton class
 *
 */
public class ExchangeClient implements ICmdHandler, IClientConnectionListener {

	private static final ExchangeClient INSTANCE = new ExchangeClient();

	private TCPClient client;
	private boolean connected = false;
	private String name;
	private Model model = new Model();
	private List<CmdOffer> offersIn = new ArrayList<CmdOffer>();
	private List<IClientListener> mListeners = new ArrayList<IClientListener>();

	private ExchangeClient() {
	}

	public static ExchangeClient getInstance() {
		return INSTANCE;
	}

	/**
	 * Starts an asynchronous connection attempt
	 * 
	 * @param ip
	 * @param port
	 */
	public void connect(String ip, int port) {
		new TCPConnectThread(ip, port).start();
	}

	public void disconnect() {
		if (client != null) {
			client.writeCommand(new CmdClientDisconnect());
			client.close();
			client = null;
		}
	}

	public String getName() {
		return name;
	}

	/**
	 * Setter for team name, which can be set only when this is disconnected
	 * 
	 * @param name
	 * @throws IllegalStateException
	 */
	public void setName(String name) throws IllegalStateException {
		if (isConnected())
			throw new IllegalStateException(
					"Name can be only set when ExchangeClient is disconnected");
		this.name = name;
	}

	public Model getModel() {
		return model;
	}

	public CmdOffer getOffer(int index) {
		return offersIn.get(index);
	}

	public boolean isConnected() {
		return connected;
	}

	public void offer(String teamID, int stockID, int amount, double money) {
		client.writeCommand(new CmdOffer(teamID, stockID, amount, money));
	}

	public void acceptOffer(int index) {
		CmdOffer offer = offersIn.get(index);
		client.writeCommand(new CmdOfferResponse(offer.teamID, offer.stockID,
				offer.amount, offer.money));
		offersIn.remove(index);
	}

	public void denyOffer(int index) {
		offersIn.remove(index);
	}

	public void addIClientListener(IClientListener listener) {
		mListeners.add(listener);
	}

	public boolean removeIClientListener(IClientListener listener) {
		return mListeners.remove(listener);
	}

	@Override
	public void onConnect(TCPClient client) {
		connected = true;
		for (IClientListener listener : mListeners)
			listener.onConnect(client);
	}

	@Override
	public void onClose(TCPClient client) {
		connected = false;
		for (IClientListener listener : mListeners)
			listener.onClose(client);
	}

	@Override
	public void onConnectionFail(TCPClient client, IOException exception) {
		for (IClientListener listener : mListeners)
			listener.onConnectionFail(client, exception);
	}

	@Override
	public void handleCmd(TCPCommand cmd, TCPConnection conn) {
		Log.d(this.getClass().getName(), "Received command! "
				+ cmd.getClass().getName());

		if (cmd instanceof CmdServerStocks) {
			CmdServerStocks stockInfo = ((CmdServerStocks) cmd);
			model.startMoney = stockInfo.startMoney;
			model.stockList = stockInfo.stockList;
			for (IClientListener listener : mListeners)
				listener.onStocksCommand(this);
			return;
		}

		if (cmd instanceof CmdServerTeams) {
			CmdServerTeams teamInfo = ((CmdServerTeams) cmd);
			this.model.teams = teamInfo.teams;
			for (IClientListener listener : mListeners)
				listener.onTeamsCommand(this);
			return;
		}

		if (cmd instanceof CmdOffer) {
			CmdOffer offer = (CmdOffer) cmd;
			this.offersIn.add(offer);
			for (IClientListener listener : mListeners)
				listener.onOfferIn(this, offer);
			return;
		}
	}

	private class TCPConnectThread extends Thread {

		private String ip;
		private int port;

		public TCPConnectThread(String ip, int port) {
			super("Thread-TCPConnect");
			this.ip = ip;
			this.port = port;
		}

		@Override
		public void run() {
			Log.d(this.getClass().getName(), "run()");
			try {
				client = new TCPClient(ip, port, ExchangeClient.this,
						ExchangeClient.this);
				Log.d(this.getClass().getName(), "success");
			} catch (IOException e) {
				e.printStackTrace();
				return;
			}
			client.writeCommand(new CmdClientInfo(name));
		}
	}

}
