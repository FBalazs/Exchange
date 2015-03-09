package hu.berzsenyi.exchange.client;

import hu.berzsenyi.exchange.Model;
import hu.berzsenyi.exchange.SingleEvent;
import hu.berzsenyi.exchange.Team;
import hu.berzsenyi.exchange.net.IClientConnectionListener;
import hu.berzsenyi.exchange.net.TCPClient;
import hu.berzsenyi.exchange.net.TCPConnection;
import hu.berzsenyi.exchange.net.cmd.ICmdHandler;
import hu.berzsenyi.exchange.net.msg.*;

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

	private TCPClient mClient;
	private boolean mConnected = false;
	private String mName, mPassword;
	private Model mModel;
	private List<IClientListener> mListeners;
	private Team mOwnTeam;
	private ArrayList<MsgOffer> mOutgoingOffers = new ArrayList<MsgOffer>();
	private SingleEvent[] mEvents;

	private ExchangeClient() {
		init();
	}

	private void init() {
		mClient = null;
		mName = mPassword = null;
		mModel = new Model();
		mListeners = new ArrayList<IClientListener>();
		mOwnTeam = null;
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
		if (mClient != null) {
			//mClient.writeCommand(new CmdClientDisconnect());
			mClient.close();
			mClient = null;
		}
	}

	public String getName() {
		return mName;
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
		mName = name;
	}

	public void setPassword(String password) {
		mPassword = password;
	}

	public Model getModel() {
		return mModel;
	}

	public Team getOwnTeam() {
		return mOwnTeam;
	}

	public boolean isConnected() {
		return mConnected;
	}

	public boolean doBuy(int[] amounts) {
		if (getModel().round != 0)
			throw new IllegalStateException(
					"doBuy can be called only in the 0th round");

		double calculated = getModel().calculateMoneyAfterPurchase(amounts);
		if (calculated < 0)
			return false;

		mOwnTeam.setStocks(amounts);
		mOwnTeam.setMoney(calculated);
		mClient.writeCommand(new MsgBuy(amounts));
		return true;
	}

	public void sendOffer(int stockID, int amount, double price, boolean sell) {
		MsgOffer offer = new MsgOffer(stockID, amount, price, sell);
		mOutgoingOffers.add(offer);
		mClient.writeCommand(offer);
		for (IClientListener listener : mListeners)
			listener.onOutgoingOffersChanged();
	}

	public boolean deleteOffer(MsgOffer offer) {
		if (!mOutgoingOffers.remove(offer))
			return false;
		MsgOfferDelete cmd = new MsgOfferDelete(offer.stockId,
				offer.stockAmount, offer.price, offer.sell);
		mClient.writeCommand(cmd);
		for (IClientListener listener : mListeners)
			listener.onOutgoingOffersChanged();
		return true;
	}
	
	public SingleEvent[] getEvents() {
		return mEvents.clone();
	}

	public MsgOffer[] getOutgoingOffers() {
		return mOutgoingOffers.toArray(new MsgOffer[mOutgoingOffers
				.size()]);
	}

	public void addIClientListener(IClientListener listener) {
		mListeners.add(listener);
	}

	public boolean removeIClientListener(IClientListener listener) {
		return mListeners.remove(listener);
	}

	@Override
	public void onConnect(TCPClient client) {
		mConnected = true;
		for (IClientListener listener : mListeners)
			listener.onConnect(client);
	}

	@Override
	public void onClose(TCPClient client) {
		mConnected = false;
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
	public void handleCmd(Object o, TCPConnection conn) {
		Log.d(this.getClass().getName(), "Received command! "
				+ o.getClass().getName());

		/*if (cmd instanceof CmdServerInfo) {
			CmdServerInfo info = (CmdServerInfo) cmd;
			mOwnTeam = new Team(mModel, info.clientID, mName, mPassword);
			mOwnTeam.setOnChangeListener(new Team.OnChangeListener() {

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
			mOwnTeam.setMoney(mModel.startMoney = info.startMoney);
			return;
		}

		if (cmd instanceof CmdServerStocks) {
			CmdServerStocks stockInfo = (CmdServerStocks) cmd;
			mModel.stocks = stockInfo.stockList;
			for (IClientListener listener : mListeners)
				listener.onStocksCommand(this);
			return;
		}

		if (cmd instanceof CmdServerEvent) {
			CmdServerEvent cmdNextRound = (CmdServerEvent) cmd;
			
			mEvents = cmdNextRound.newEvents;
			mModel.round++;
			mModel.nextRound(cmdNextRound.multipliers);
			for (IClientListener listener : mListeners)
				listener.onNewEvents(mEvents);
			return;
		}


		if (cmd instanceof CmdServerError) {
			CmdServerError error = (CmdServerError) cmd;
			for (IClientListener listener : mListeners)
				listener.onErrorCommand(error);
		}*/
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
				mClient = new TCPClient(ip, port, ExchangeClient.this,
						ExchangeClient.this);
				Log.d(this.getClass().getName(), "success");
			} catch (IOException e) {
				e.printStackTrace();
				return;
			}
			mClient.writeCommand(new MsgConnRequest(mName, mPassword));
		}
	}

}
