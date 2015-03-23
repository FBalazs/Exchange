package hu.berzsenyi.exchange.client;

import java.util.Vector;

import hu.berzsenyi.exchange.Exchange;
import hu.berzsenyi.exchange.Offer;
import hu.berzsenyi.exchange.Stock;
import hu.berzsenyi.exchange.net.NetClient;
import hu.berzsenyi.exchange.net.msg.Msg;
import hu.berzsenyi.exchange.net.msg.MsgClientConnRequest;
import hu.berzsenyi.exchange.net.msg.MsgClientOfferIndirect;
import hu.berzsenyi.exchange.net.msg.MsgClientOfferDirect;
import hu.berzsenyi.exchange.net.msg.MsgServerBuyAccept;
import hu.berzsenyi.exchange.net.msg.MsgServerBuyRefuse;
import hu.berzsenyi.exchange.net.msg.MsgServerConnAccept;
import hu.berzsenyi.exchange.net.msg.MsgServerConnRefuse;
import hu.berzsenyi.exchange.net.msg.MsgServerBuyRequest;
import hu.berzsenyi.exchange.net.msg.MsgServerPlayers;
import hu.berzsenyi.exchange.net.msg.MsgServerSentOfferIndirectAccept;
import hu.berzsenyi.exchange.net.msg.MsgServerSentOfferRefuse;
import hu.berzsenyi.exchange.net.msg.MsgServerSentOfferDirectAccept;
import hu.berzsenyi.exchange.net.msg.MsgServerStockOfferUpdate;
import hu.berzsenyi.exchange.net.msg.MsgServerStockUpdate;

public class ClientExchange extends Exchange implements NetClient.INetClientListener {
	public static interface IClientExchangeListener {
		public void onConnAccepted(ClientExchange exchange);
		public void onConnRefused(ClientExchange exchange);
		public void onConnLost(ClientExchange exchange);
		public void onShowBuy(ClientExchange exchange);
		public void onBuyAccepted(ClientExchange exchange);
		public void onBuyRefused(ClientExchange exchange);
		public void onStocksChanged(ClientExchange exchange);
		public void onMyMoneyChanged(ClientExchange exchange);
		public void onMyStocksChanged(ClientExchange exchange);
		public void onSentOfferAccepted(ClientExchange exchange);
		public void onSentOfferRefused(ClientExchange exchange);
		public void onOfferCame(ClientExchange exchange);
		public void onTrade(ClientExchange exchange);
	}
	
	public static final ClientExchange INSTANCE = new ClientExchange();
	
	private NetClient net;
	
	private int gameMode;
	private String myName, myPassword;
	private double myMoney;
	private int[] myStocks;
	private Stock[] stocks;
	private String[] playerNames;
	private Vector<Offer> incomingOffers, myOffers;
	
	private boolean sendingOffer;
	
	private Vector<IClientExchangeListener> listeners;
	
	public ClientExchange() {
		net = new NetClient();
		net.addListener(this);
		listeners = new Vector<IClientExchangeListener>();
	}
	
	public synchronized void addListener(IClientExchangeListener listener) {
		listeners.add(listener);
	}
	
	public synchronized void removeListener(IClientExchangeListener listener) {
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
		return stocks[stockId].getName();
	}
	
	public synchronized double getStockPrice(int stockId) {
		return stocks[stockId].getPrice();
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
		net.sendMsg(new MsgClientOfferIndirect(stockId, amount, price, sell));
	}
	
	public synchronized void offerTo(int stockId, int amount, double price, boolean sell, int playerId) {
		if(sendingOffer)
			new Exception("Already sending an offer!").printStackTrace();
		sendingOffer = true;
		net.sendMsg(new MsgClientOfferDirect(stockId, amount, price, sell, playerNames[playerId]));
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
			for(IClientExchangeListener listener : listeners)
				listener.onConnAccepted(this);
		} else if(msg instanceof MsgServerConnRefuse) {
			for(IClientExchangeListener listener : listeners)
				listener.onConnRefused(this);
		} else if(msg instanceof MsgServerBuyRequest) {
			for(IClientExchangeListener listener : listeners)
				listener.onShowBuy(this);
		} else if(msg instanceof MsgServerBuyAccept) {
			for(IClientExchangeListener listener : listeners)
				listener.onBuyAccepted(this);
		} else if(msg instanceof MsgServerBuyRefuse) {
			for(IClientExchangeListener listener : listeners)
				listener.onBuyRefused(this);
		} else if(msg instanceof MsgServerPlayers) {
			playerNames = ((MsgServerPlayers) msg).playerNames;
		} else if(msg instanceof MsgServerStockUpdate) {
			MsgServerStockUpdate msgUpdate = (MsgServerStockUpdate)msg;
			for(int i = 0; i < stocks.length; i++)
				stocks[i].setPrice(msgUpdate.prices[i]);
			for(IClientExchangeListener listener : listeners)
				listener.onStocksChanged(this);
		} else if(msg instanceof MsgServerStockOfferUpdate) {
			MsgServerStockOfferUpdate msgUpdate = (MsgServerStockOfferUpdate)msg;
			stocks[msgUpdate.stockId].setMinMaxOffers(msgUpdate.minSellOffer, msgUpdate.maxBuyOffer);
			for(IClientExchangeListener listener : listeners)
				listener.onStocksChanged(this);
		} else if(msg instanceof MsgServerSentOfferIndirectAccept) {
			MsgServerSentOfferIndirectAccept msgAccept = (MsgServerSentOfferIndirectAccept) msg;
			myOffers.add(new Offer(myName, null, msgAccept.stockId, msgAccept.amount, msgAccept.price, msgAccept.sell));
			sendingOffer = false;
			for(IClientExchangeListener listener : listeners)
				listener.onSentOfferAccepted(this);
		} else if(msg instanceof MsgServerSentOfferDirectAccept) {
			MsgServerSentOfferDirectAccept msgAccept = (MsgServerSentOfferDirectAccept) msg;
			myOffers.add(new Offer(myName, msgAccept.target, msgAccept.stockId, msgAccept.amount, msgAccept.price, msgAccept.sell));
			sendingOffer = false;
			for(IClientExchangeListener listener : listeners)
				listener.onSentOfferAccepted(this);
		} else if(msg instanceof MsgServerSentOfferRefuse) {
			sendingOffer = false;
			for(IClientExchangeListener listener : listeners)
				listener.onSentOfferRefused(this);
		}
	}

	@Override
	public synchronized void onClosed(NetClient net, Exception e) {
		// TODO switch based on exception
		for(IClientExchangeListener listener : listeners)
			listener.onConnLost(this);
	}
}
