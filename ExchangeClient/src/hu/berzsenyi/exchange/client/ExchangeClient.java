package hu.berzsenyi.exchange.client;

import hu.berzsenyi.exchange.SingleEvent;
import hu.berzsenyi.exchange.Stock;
import hu.berzsenyi.exchange.Team;
import hu.berzsenyi.exchange.net.IClientConnectionListener;
import hu.berzsenyi.exchange.net.TCPClient;
import hu.berzsenyi.exchange.net.TCPConnection;
import hu.berzsenyi.exchange.net.msg.IMsgHandler;
import hu.berzsenyi.exchange.net.msg.Msg;
import hu.berzsenyi.exchange.net.msg.MsgBuy;
import hu.berzsenyi.exchange.net.msg.MsgConnAccept;
import hu.berzsenyi.exchange.net.msg.MsgConnRefuse;
import hu.berzsenyi.exchange.net.msg.MsgConnRequest;
import hu.berzsenyi.exchange.net.msg.MsgNewRound;
import hu.berzsenyi.exchange.net.msg.MsgOffer;
import hu.berzsenyi.exchange.net.msg.MsgOfferDelete;
import hu.berzsenyi.exchange.net.msg.MsgStockInfo;
import hu.berzsenyi.exchange.net.msg.MsgTeamInfo;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import android.util.Log;

/**
 * Singleton class
 *
 */
public class ExchangeClient implements IMsgHandler, IClientConnectionListener {

	private static final ExchangeClient INSTANCE = new ExchangeClient();

	private TCPClient mClient;
	private boolean mConnected = false;
	private String mName, mPassword;
	private ClientModel mModel;
	private List<IClientListener> mListeners;
	private Team mOwnTeam;
	private ArrayList<MsgOffer> mOutgoingOffers = new ArrayList<MsgOffer>(); // TODO
																				// maybe
																				// it's
																				// need
																				// to
																				// be
																				// synchronized,
																				// but
																				// not
																				// with
																				// vector
	private SingleEvent[] mEvents = new SingleEvent[0];
	private boolean mZerothRound = true;

	private ExchangeClient() {
		init();
	}

	private void init() {
		mClient = null;
		mName = mPassword = null;
		mModel = new ClientModel();
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
		if(ip.equals(""))
			ip = "localhost";
		new TCPConnectThread(ip, port).start();
	}

	public void disconnect() {
		if (mClient != null) {
			// mClient.writeCommand(new CmdClientDisconnect());
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

	public ClientModel getModel() {
		return mModel;
	}

	public Team getOwnTeam() {
		return mOwnTeam;
	}

	public boolean isConnected() {
		return mConnected;
	}
	
	public boolean isInZerothRound() {
		return mZerothRound;
	}

	public boolean doBuy(int[] amounts) {
		if (!isInZerothRound())
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
		for(int i = 0; i < mOutgoingOffers.size(); i++)
			if(mOutgoingOffers.get(i).stockId == stockID) {
				for(IClientListener listener : mListeners)
					listener.onOfferFailed();
				return;
			}
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
		return mOutgoingOffers.toArray(new MsgOffer[mOutgoingOffers.size()]);
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
	public void handleMsg(Msg o, TCPConnection conn) {
		Log.d(this.getClass().getName(), "Received command! "
				+ o.getClass().getName());

		if (o instanceof MsgConnAccept) {
			MsgConnAccept msg = (MsgConnAccept) o;
			mZerothRound = msg.zerothRound;
			for (int t = 0; t < msg.teamNames.length; t++)
				mModel.teams
						.add(new Team(mModel, null, msg.teamNames[t], null));
			mModel.stocks = new Stock[msg.stockNames.length];
			for(int s = 0; s < msg.stockNames.length; s++) {
				mModel.stocks[s] = new Stock(null, msg.stockNames[s], msg.stockValues[s]);
				mModel.stocks[s].circulated = msg.stockCirc[s];
			}
			for (IClientListener listener : mListeners)
				listener.onStocksCommand(this);
			mOwnTeam = mModel.getTeamByName(mName);
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
			mOwnTeam.setMoney(msg.teamMoney);
			if (msg.teamStocks != null)
				mOwnTeam.setStocks(msg.teamStocks);
		} else if (o instanceof MsgConnRefuse) {
			// TODO wrong pw or not connected in first round
		} else if (o instanceof MsgStockInfo) {
			MsgStockInfo msg = (MsgStockInfo) o;
			for (int s = 0; s < mModel.stocks.length; s++) {
				mModel.stocks[s].change = msg.stockPrices[s]
						/ mModel.stocks[s].value;
				mModel.stocks[s].value = msg.stockPrices[s];
				mModel.stocks[s].circulated = msg.circulated[s];
			}
			for (IClientListener listener : mListeners)
				listener.onStocksCommand(this);
		} else if (o instanceof MsgTeamInfo) {
			MsgTeamInfo msg = (MsgTeamInfo) o;
			mOwnTeam.setMoney(msg.money);
			mOwnTeam.setStocks(msg.stocks);
		}
		// TODO offer response
		else if (o instanceof MsgNewRound) {
			mZerothRound = false;
			MsgNewRound msg = (MsgNewRound) o;
			mEvents = msg.events;
			for (IClientListener listener : mListeners)
				listener.onNewRound(mEvents);
		} else if (o instanceof MsgStockInfo) {
			MsgStockInfo msg = (MsgStockInfo) o;
			for (int i = 0; i < mModel.stocks.length; i++) {
				Stock stock = mModel.stocks[i];
				stock.value = msg.stockPrices[i];
				stock.circulated = msg.circulated[i];
			}
			for (IClientListener listener : mListeners)
				listener.onStocksCommand(this);
		} else if (o instanceof MsgOffer) {
			MsgOffer msg = (MsgOffer) o;
			int offer = -1;
			for (int i = 0; i < mOutgoingOffers.size() && offer == -1; i++)
				if (mOutgoingOffers.get(i).stockId == msg.stockId
						&& mOutgoingOffers.get(i).sell == msg.sell)
					offer = i;
			if(offer != -1) { // client reconnected since offer
				mOutgoingOffers.get(offer).stockAmount -= msg.stockAmount;
				if (mOutgoingOffers.get(offer).stockAmount == 0)
					mOutgoingOffers.remove(offer);
				// TODO call some listener to display a toast or whatever to show
				// that a transaction did happen
				for (IClientListener listener : mListeners)
					listener.onOutgoingOffersChanged();
			}
		}
		/*
		 * if (cmd instanceof CmdServerStocks) { CmdServerStocks stockInfo =
		 * (CmdServerStocks) cmd; mModel.stocks = stockInfo.stockList; for
		 * (IClientListener listener : mListeners)
		 * listener.onStocksCommand(this); return; }
		 * 
		 * if (cmd instanceof CmdServerEvent) { CmdServerEvent cmdNextRound =
		 * (CmdServerEvent) cmd;
		 * 
		 * mEvents = cmdNextRound.newEvents; mModel.round++;
		 * mModel.nextRound(cmdNextRound.multipliers); for (IClientListener
		 * listener : mListeners) listener.onNewEvents(mEvents); return; }
		 * 
		 * 
		 * if (cmd instanceof CmdServerError) { CmdServerError error =
		 * (CmdServerError) cmd; for (IClientListener listener : mListeners)
		 * listener.onErrorCommand(error); }
		 */
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
