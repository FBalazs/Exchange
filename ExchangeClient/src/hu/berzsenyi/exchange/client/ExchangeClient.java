package hu.berzsenyi.exchange.client;

import hu.berzsenyi.exchange.Model;
import hu.berzsenyi.exchange.Team;
import hu.berzsenyi.exchange.net.IClientConnectionListener;
import hu.berzsenyi.exchange.net.TCPClient;
import hu.berzsenyi.exchange.net.TCPConnection;
import hu.berzsenyi.exchange.net.cmd.CmdClientBuy;
import hu.berzsenyi.exchange.net.cmd.CmdClientDisconnect;
import hu.berzsenyi.exchange.net.cmd.CmdClientInfo;
import hu.berzsenyi.exchange.net.cmd.CmdClientOffer;
import hu.berzsenyi.exchange.net.cmd.CmdServerError;
import hu.berzsenyi.exchange.net.cmd.CmdServerEvent;
import hu.berzsenyi.exchange.net.cmd.CmdServerInfo;
import hu.berzsenyi.exchange.net.cmd.CmdServerStocks;
import hu.berzsenyi.exchange.net.cmd.ICmdHandler;
import hu.berzsenyi.exchange.net.cmd.TCPCommand;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import android.util.Log;

/**
 * Singleton class
 *
 */
public class ExchangeClient implements ICmdHandler, IClientConnectionListener {

	private static final ExchangeClient INSTANCE = new ExchangeClient();

	private TCPClient client;
	private boolean connected = false;
	private String name, password;
	private Model model;
	private List<IClientListener> mListeners;
	private Team ownTeam;

	private ExchangeClient() {
		init();
	}

	private void init() {
		client = null;
		name = password = null;
		model = new Model();
		mListeners = new ArrayList<IClientListener>();
		ownTeam = null;
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
			if(client != null) {
				client.close();
				client = null;
			}
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
	
	public void setPassword(String password) {
		this.password = password;
	}

	public Model getModel() {
		return model;
	}

	public Team getOwnTeam() {
		return ownTeam;
	}

	public boolean isConnected() {
		return connected;
	}

//	public void offer(String teamID, int stockID, int amount, double money, boolean sell) {
//		client.writeCommand(new CmdClientOffer(teamID, stockID, amount, money, sell));
//	}
//
//	public void acceptOffer(int index) {
//		CmdClientOffer offer = offersIn.get(index);
//		client.writeCommand(new CmdOfferResponse(offer.teamID, offer.stockID,
//				offer.amount, offer.money));
//		offersIn.remove(index);
//	}

	public boolean doBuy(int[] amounts) {
		if (getModel().round != 0)
			throw new IllegalStateException(
					"doBuy can be called only in the 0th round");

		double calculated = getModel().calculateMoneyAfterPurchase(amounts);
		if (calculated < 0)
			return false;

		ownTeam.setStocks(amounts);
		ownTeam.setMoney(calculated);
		client.writeCommand(new CmdClientBuy(amounts));
		return true;
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
		init();
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
		

		if (cmd instanceof CmdServerInfo) {
			CmdServerInfo info = (CmdServerInfo) cmd;
			this.ownTeam = new Team(info.clientID, this.name, this.password);
			this.ownTeam.setOnChangeListener(new Team.OnChangeListener() {

				@Override
				public void onStocksChanged(Team team, int position) {
					for (IClientListener listener : mListeners)
						listener.onStocksChanged(team, position);
				}

				@Override
				public void onMoneyChanged(Team team) {
					for (IClientListener listener : mListeners)
						listener.onMoneyChanged(team);
				}
			});
			this.ownTeam.setMoney(this.model.startMoney = info.startMoney);
			return;
		}

		if (cmd instanceof CmdServerStocks) {
			CmdServerStocks stockInfo = (CmdServerStocks) cmd;
			model.stocks = stockInfo.stockList;
			for (IClientListener listener : mListeners)
				listener.onStocksCommand(this);
			return;
		}

	

		if (cmd instanceof CmdServerEvent) {
			CmdServerEvent cmdNextRound = (CmdServerEvent)cmd;
			this.model.round++;
			this.model.nextRound(cmdNextRound.eventDesc, cmdNextRound.multipliers);
			for (IClientListener listener : mListeners)
				listener.onRoundCommand(this);
			return;
		}

//		if (cmd instanceof CmdOfferResponse) {
//			// TODO UGLY!
//			CmdOfferResponse offer = (CmdOfferResponse) cmd;
//			this.ownTeam.setMoney(this.ownTeam.getMoney() + offer.money*Math.abs(offer.amount));
//			this.ownTeam.setStock(offer.stockID,
//					this.ownTeam.getStock(offer.stockID) + offer.amount);
//			for (IClientListener listener : mListeners) {
//				listener.onMoneyChanged(this.ownTeam);
//				listener.onStocksChanged(this.ownTeam, offer.stockID);
//				listener.onOfferAccepted(offer);
//			}
//			return;
//		}
		
		if(cmd instanceof CmdServerError) {
			CmdServerError error = (CmdServerError) cmd;
			for(IClientListener listener : mListeners)
				listener.onErrorCommand(error);
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
			client.writeCommand(new CmdClientInfo(name, password));
		}
	}

}
