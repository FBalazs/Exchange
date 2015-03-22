package hu.berzsenyi.exchange.client.game;

import java.io.IOException;
import java.util.Vector;

import hu.berzsenyi.exchange.game.ExchangeGame;
import hu.berzsenyi.exchange.game.Offer;
import hu.berzsenyi.exchange.game.SingleEvent;
import hu.berzsenyi.exchange.game.Stock;
import hu.berzsenyi.exchange.game.Player;
import hu.berzsenyi.exchange.net.NetClient;
import hu.berzsenyi.exchange.net.msg.Msg;
import hu.berzsenyi.exchange.net.msg.MsgClientConnRequest;
import hu.berzsenyi.exchange.net.msg.MsgClientOffer;
import hu.berzsenyi.exchange.net.msg.MsgClientOfferTo;
import hu.berzsenyi.exchange.net.msg.MsgServerBuyAccept;
import hu.berzsenyi.exchange.net.msg.MsgServerBuyRefuse;
import hu.berzsenyi.exchange.net.msg.MsgServerConnAccept;
import hu.berzsenyi.exchange.net.msg.MsgServerConnRefuse;
import hu.berzsenyi.exchange.net.msg.MsgServerBuyRequest;
import hu.berzsenyi.exchange.net.msg.MsgServerMoneyUpdate;
import hu.berzsenyi.exchange.net.msg.MsgServerPlayers;
import hu.berzsenyi.exchange.net.msg.MsgServerSentOfferAccept;
import hu.berzsenyi.exchange.net.msg.MsgServerSentOfferRefuse;
import hu.berzsenyi.exchange.net.msg.MsgServerSentOfferToAccept;
import hu.berzsenyi.exchange.net.msg.MsgServerStockIssueEnd;
import hu.berzsenyi.exchange.net.msg.MsgServerStockUpdate;

public class ClientExchangeGame extends ExchangeGame implements
		NetClient.INetClientListener {

	public static interface IClientExchangeGameListener {
		public void onConnAccepted(ClientExchangeGame exchange);

		public void onConnRefused(ClientExchangeGame exchange);

		public void onConnLost(ClientExchangeGame exchange);

		public void onShowBuy(ClientExchangeGame exchange);

		public void onBuyAccepted(ClientExchangeGame exchange);

		public void onBuyRefused(ClientExchangeGame exchange);

		public void onStocksChanged(ClientExchangeGame exchange);

		public void onMyMoneyChanged(ClientExchangeGame exchange);

		public void onMyStocksChanged(ClientExchangeGame exchange);

		public void onSentOfferAccepted(ClientExchangeGame exchange);

		public void onSentOfferRefused(ClientExchangeGame exchange);

		public void onOfferCame(ClientExchangeGame exchange);

		public void onTrade(ClientExchangeGame exchange);

		public void onStockIssueEnded(ClientExchangeGame exchange);
	}

	public static final ClientExchangeGame INSTANCE = new ClientExchangeGame();

	private NetClient net;

	private int gameMode;
	private Player ownPlayer;
	private String[] playerNames;

	private boolean sendingOffer;
	private boolean inGame;
	private boolean stockIssue; // TODO update value

	private Vector<IClientExchangeGameListener> listeners;
	private Vector<Offer> incomingOffers, myOffers;

	private ClientExchangeGame() {
		net = new NetClient();
		net.addListener(this);
		listeners = new Vector<IClientExchangeGameListener>();
		resetFields();
	}

	public static ClientExchangeGame getInstance() {
		return INSTANCE;
	}

	public synchronized int getGameMode() {
		return gameMode;
	}

	public synchronized Player getOwnPlayer() {
		return ownPlayer;
	}

	public synchronized int getPlayerNameCount() {
		return playerNames.length;
	}

	public synchronized String getPlayerName(int index) {
		return playerNames[index];
	}

	public synchronized void addListener(IClientExchangeGameListener listener) {
		listeners.add(listener);
	}

	public synchronized void removeListener(IClientExchangeGameListener listener) {
		listeners.remove(listener);
	}

	public synchronized void offer(int stockId, int amount, double price,
			boolean sell) throws IOException {
		if (sendingOffer)
			new Exception("Already sending an offer!").printStackTrace();
		sendingOffer = true;
		net.sendMsg(new MsgClientOffer(stockId, amount, price, sell));
	}

	public synchronized void offerTo(int stockId, int amount, double price,
			boolean sell, int playerId) throws IOException {
		if (sendingOffer)
			new Exception("Already sending an offer!").printStackTrace();
		sendingOffer = true;
		net.sendMsg(new MsgClientOfferTo(stockId, amount, price, sell,
				playerNames[playerId]));
	}

	public synchronized boolean isStockIssue() {
		return stockIssue;
	}

	public synchronized boolean doBuy(int[] mAmounts) {
		// TODO Auto-generated method stub
		return false;
	}

	private synchronized void resetFields() {
		gameMode = -1;
		playerNames = null;
		sendingOffer = false;
		inGame = false;
		stockIssue = false;
		setStocks(null);
		ownPlayer = null;
		System.gc();
	}

	public synchronized void connect(String host, int port, String nickName,
			String password) {
		ownPlayer = new Player(this, nickName, password);
		net.connect(host, port);
	}

	public synchronized void close() {
		net.close();
	}

	public synchronized boolean isInGame() {
		return inGame;
	}

	@Override
	public synchronized void onConnected(NetClient net) {
		try {
			net.sendMsg(new MsgClientConnRequest(getOwnPlayer().getName(),
					getPassword(getOwnPlayer())));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public synchronized void onObjectReceived(NetClient net, Msg msg) {
		
		if (msg instanceof MsgServerConnAccept) {
			MsgServerConnAccept msgAccept = (MsgServerConnAccept) msg;
			inGame = true;
			gameMode = msgAccept.gameMode;
			Stock[] stocks = new Stock[msgAccept.stockNames.length];
			for (int i = 0; i < stocks.length; i++)
				stocks[i] = new Stock(msgAccept.stockNames[i],
						msgAccept.stockPrices[i]);
			setStocks(stocks);
			setMoney(getOwnPlayer(), msgAccept.playerMoney);
			setStockAmounts(getOwnPlayer(), msgAccept.playerStocks);
			for (IClientExchangeGameListener listener : listeners)
				listener.onConnAccepted(this);
			
		} else if (msg instanceof MsgServerConnRefuse) {
			
			for (IClientExchangeGameListener listener : listeners)
				listener.onConnRefused(this);
			
		} else if (msg instanceof MsgServerBuyRequest) {
			
			for (IClientExchangeGameListener listener : listeners)
				listener.onShowBuy(this);
			
		} else if (msg instanceof MsgServerBuyAccept) {
			
			for (IClientExchangeGameListener listener : listeners)
				listener.onBuyAccepted(this);
			
		} else if (msg instanceof MsgServerBuyRefuse) {
			
			for (IClientExchangeGameListener listener : listeners)
				listener.onBuyRefused(this);
			
		} else if (msg instanceof MsgServerPlayers) {
			
			playerNames = ((MsgServerPlayers) msg).playerNames;
			
		} else if (msg instanceof MsgServerStockUpdate) {
			
			MsgServerStockUpdate msgUpdate = (MsgServerStockUpdate) msg;
			for (int i = 0; i < getStockCount(); i++)
				setStockPrice(getStock(i), msgUpdate.prices[i]);
			for (IClientExchangeGameListener listener : listeners)
				listener.onStocksChanged(this);
			
		} else if (msg instanceof MsgServerMoneyUpdate) {
			
			setMoney(getOwnPlayer(), ((MsgServerMoneyUpdate) msg).money);
			for (IClientExchangeGameListener listener : listeners)
				listener.onMyMoneyChanged(this);
			
		} else if (msg instanceof MsgServerSentOfferAccept) {
			
			MsgServerSentOfferAccept msgAccept = (MsgServerSentOfferAccept) msg;
			myOffers.add(new Offer(getOwnPlayer().getName(), null,
					msgAccept.stockId, msgAccept.amount, msgAccept.price,
					msgAccept.sell));
			sendingOffer = false;
			for (IClientExchangeGameListener listener : listeners)
				listener.onSentOfferAccepted(this);
			
		} else if (msg instanceof MsgServerSentOfferToAccept) {
			
			MsgServerSentOfferToAccept msgAccept = (MsgServerSentOfferToAccept) msg;
			myOffers.add(new Offer(getOwnPlayer().getName(), msgAccept.target,
					msgAccept.stockId, msgAccept.amount, msgAccept.price,
					msgAccept.sell));
			sendingOffer = false;
			for (IClientExchangeGameListener listener : listeners)
				listener.onSentOfferAccepted(this);
			
		} else if (msg instanceof MsgServerSentOfferRefuse) {
			
			sendingOffer = false;
			for (IClientExchangeGameListener listener : listeners)
				listener.onSentOfferRefused(this);
			
		} else if(msg instanceof MsgServerStockIssueEnd) {
			
			stockIssue = false;
			for(IClientExchangeGameListener listener : listeners)
				listener.onStockIssueEnded(this);
		}
	}

	@Override
	public synchronized void onClosed(NetClient net) {
		for (IClientExchangeGameListener listener : listeners)
			listener.onConnLost(this);
		resetFields();
	}

	public synchronized SingleEvent[] getActiveEvents() {
		// TODO
		return null;
	}

	public double calculateMoneyAfterPurchase(int[] amounts) {
		if (!isStockIssue())
			throw new IllegalStateException("Not in stock issue");
		double sum = 0.0;
		for (int i = 0; i < amounts.length; i++)
			sum += amounts[i] * getStock(i).getPrice();
		return getOwnPlayer().getMoney() - sum;
	}

}
