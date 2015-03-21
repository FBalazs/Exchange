package hu.berzsenyi.exchange.client;

import java.util.Vector;

import hu.berzsenyi.exchange.Exchange;
import hu.berzsenyi.exchange.Offer;
import hu.berzsenyi.exchange.Stock;
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
import hu.berzsenyi.exchange.net.msg.MsgServerStockUpdate;

public class ExchangeClient extends Exchange implements NetClient.INetClientListener {
	public static interface IExchangeClientListener {
		public void onConnAccepted(ExchangeClient exchange);
		public void onConnRefused(ExchangeClient exchange);
		public void onConnLost(ExchangeClient exchange);
		public void onShowBuy(ExchangeClient exchange);
		public void onBuyAccepted(ExchangeClient exchange);
		public void onBuyRefused(ExchangeClient exchange);
		public void onStocksChanged(ExchangeClient exchange);
		public void onMyMoneyChanged(ExchangeClient exchange);
		public void onMyStocksChanged(ExchangeClient exchange);
		public void onSentOfferAccepted(ExchangeClient exchange);
		public void onSentOfferRefused(ExchangeClient exchange);
		public void onOfferCame(ExchangeClient exchange);
		public void onTrade(ExchangeClient exchange);
	}
	
	public static final ExchangeClient INSTANCE = new ExchangeClient();
	
	private NetClient net;
	
	// TODO synchronized getter functions
	private int gameMode;
	private String myName, myPassword;
	private double myMoney;
	private int[] myStocks;
	private Stock[] stocks;
	private String[] playerNames;
	private Vector<Offer> incomingOffers, myOffers;
	
	private boolean sendingOffer;
	
	private Vector<IExchangeClientListener> listeners;
	
	public ExchangeClient() {
		net = new NetClient();
		net.addListener(this);
		listeners = new Vector<IExchangeClientListener>();
	}
	
	public synchronized void addListener(IExchangeClientListener listener) {
		listeners.add(listener);
	}
	
	public synchronized void removeListener(IExchangeClientListener listener) {
		listeners.remove(listener);
	}
	
	public synchronized int getGameMode() {
		return gameMode;
	}
	
	public synchronized String getName() {
		return myName;
	}
	
	public synchronized double getMoney() {
		return myMoney;
	}
	
	public synchronized int getStocksNumber() {
		return stocks.length;
	}
	
	public synchronized int getStockAmount(int stockId) {
		return myStocks[stockId];
	}
	
	public synchronized String getStockName(int stockId) {
		return stocks[stockId].name;
	}
	
	public synchronized double getStockPrice(int stockId) {
		return stocks[stockId].price;
	}
	
	public synchronized void connect(String host, int port, String nickName, String password) {
		gameMode = -1;
		myName = nickName;
		myPassword = password;
		myMoney = 0;
		myStocks = null;
		stocks = null;
		playerNames = null;
		incomingOffers = new Vector<Offer>();
		myOffers = new Vector<Offer>();
		sendingOffer = false;
		net.connect(host, port);
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
			MsgServerSentOfferAccept msgAccept = (MsgServerSentOfferAccept) msg;
			myOffers.add(new Offer(myName, null, msgAccept.stockId, msgAccept.amount, msgAccept.price, msgAccept.sell));
			sendingOffer = false;
			for(IExchangeClientListener listener : listeners)
				listener.onSentOfferAccepted(this);
		} else if(msg instanceof MsgServerSentOfferToAccept) {
			MsgServerSentOfferToAccept msgAccept = (MsgServerSentOfferToAccept) msg;
			myOffers.add(new Offer(myName, msgAccept.target, msgAccept.stockId, msgAccept.amount, msgAccept.price, msgAccept.sell));
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
