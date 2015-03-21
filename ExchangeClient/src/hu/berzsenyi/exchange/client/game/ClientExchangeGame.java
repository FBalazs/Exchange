package hu.berzsenyi.exchange.client.game;

import java.util.Vector;

import hu.berzsenyi.exchange.game.ExchangeGame;
import hu.berzsenyi.exchange.game.Stock;
import hu.berzsenyi.exchange.game.Team;
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
import hu.berzsenyi.exchange.net.msg.MsgServerStockUpdate;

public class ClientExchangeGame extends ExchangeGame implements NetClient.INetClientListener {
	public static interface IExchangeClientListener {
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
	}
	
	public static final ClientExchangeGame INSTANCE = new ClientExchangeGame();
	
	private NetClient net;
	
	// TODO synchronized getter functions
	private int gameMode;
	private Team myTeam;
	private String[] playerNames;
	
	private boolean sendingOffer;
	
	private Vector<IExchangeClientListener> listeners;
	
	public ClientExchangeGame() { // TODO lock until the connection accept or refuse arrives
		net = new NetClient();
		net.addListener(this);
		listeners = new Vector<IExchangeClientListener>();
	}
	
	public synchronized int getGameMode() {
		return gameMode;
	}
	
	public synchronized Team getOwnTeam() {
		return myTeam;
	}
	
	
	public synchronized void addListener(IExchangeClientListener listener) {
		listeners.add(listener);
	}
	
	public synchronized void removeListener(IExchangeClientListener listener) {
		listeners.remove(listener);
	}
	
	public synchronized void offer(int stockId, int amount, double price, boolean sell) {
		if(sendingOffer)
			new Exception("Already sending an offer!").printStackTrace();
		sendingOffer = true;
		net.sendMsg(new MsgClientOffer(stockId, amount, price, sell));
	}
	
	public synchronized void offerTo(int stockId, int amount, double price, boolean sell, int playerId) {
		if(sendingOffer)
			new Exception("Already sending an offer!").printStackTrace();
		sendingOffer = true;
		net.sendMsg(new MsgClientOfferTo(stockId, amount, price, sell, playerNames[playerId]));
	}
	
	public synchronized void connect(String host, int port, String nickName, String password) {
		gameMode = -1;
		myName = nickName;
		myPassword = password;
		myMoney = 0;
		myStocks = null;
		stocks = null;
		playerNames = null;
		sendingOffer = false;
		net.connect(host, port);
	}
	
	public synchronized void close() {
		net.close();
	}

	@Override
	public synchronized void onConnected(NetClient net) {
		net.sendMsg(new MsgClientConnRequest(myName, myPassword));
	}

	@Override
	public synchronized void onObjectReceived(NetClient net, Msg msg) {
		if(msg instanceof MsgServerConnAccept) {
			MsgServerConnAccept msgAccept = (MsgServerConnAccept)msg;
			gameMode = msgAccept.gameMode;
			stocks = new Stock[msgAccept.stockNames.length];
			for(int i = 0; i < stocks.length; i++)
				stocks[i] = new Stock(msgAccept.stockNames[i], msgAccept.stockPrices[i]);
			myMoney = msgAccept.playerMoney;
			myStocks = msgAccept.playerStocks;
			for(IExchangeClientListener listener : listeners)
				listener.onConnAccepted(this);
		} else if(msg instanceof MsgServerConnRefuse) {
			for(IExchangeClientListener listener : listeners)
				listener.onConnRefused(this);
		} else if(msg instanceof MsgServerBuyRequest) {
			for(IExchangeClientListener listener : listeners)
				listener.onShowBuy(this);
		} else if(msg instanceof MsgServerBuyAccept) {
			for(IExchangeClientListener listener : listeners)
				listener.onBuyAccepted(this);
		} else if(msg instanceof MsgServerBuyRefuse) {
			for(IExchangeClientListener listener : listeners)
				listener.onBuyRefused(this);
		} else if(msg instanceof MsgServerPlayers) {
			playerNames = ((MsgServerPlayers) msg).playerNames;
		} else if(msg instanceof MsgServerStockUpdate) {
			MsgServerStockUpdate msgUpdate = (MsgServerStockUpdate)msg;
			for(int i = 0; i < stocks.length; i++)
				stocks[i].price = msgUpdate.prices[i];
			for(IExchangeClientListener listener : listeners)
				listener.onStocksChanged(this);
		} else if(msg instanceof MsgServerMoneyUpdate) {
			myMoney = ((MsgServerMoneyUpdate) msg).money;
			for(IExchangeClientListener listener : listeners)
				listener.onMyMoneyChanged(this);
		} else if(msg instanceof MsgServerSentOfferAccept) {
			sendingOffer = false;
			for(IExchangeClientListener listener : listeners)
				listener.onSentOfferAccepted(this);
		} else if(msg instanceof MsgServerSentOfferRefuse) {
			sendingOffer = false;
			for(IExchangeClientListener listener : listeners)
				listener.onSentOfferRefused(this);
		}
	}

	@Override
	public synchronized void onClosed(NetClient net) {
		for(IExchangeClientListener listener : listeners)
			listener.onConnLost(this);
	}
}
