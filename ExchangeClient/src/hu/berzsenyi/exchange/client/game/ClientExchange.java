package hu.berzsenyi.exchange.client.game;

import java.util.Vector;

import hu.berzsenyi.exchange.game.Exchange;
import hu.berzsenyi.exchange.game.Offer;
import hu.berzsenyi.exchange.game.Stock;
import hu.berzsenyi.exchange.net.NetClient;
import hu.berzsenyi.exchange.net.msg.*;

public class ClientExchange extends Exchange implements
		NetClient.INetClientListener {
	private static final String TAG = "["+ClientExchange.class.getSimpleName()+"] ";
	
	public static interface IClientExchangeListener {
		public void onConnAccepted(ClientExchange exchange);

		public void onConnRefused(ClientExchange exchange);

		public void onConnLost(ClientExchange exchange);

		public void onShowBuy(ClientExchange exchange);

		public void onBuyAccepted(ClientExchange exchange);

		public void onBuyRefused(ClientExchange exchange);

		public void onBuyEnd(ClientExchange exchange);

		public void onStocksChanged(ClientExchange exchange);

		public void onMyMoneyChanged(ClientExchange exchange);

		public void onMyStocksChanged(ClientExchange exchange);

		public void onSentOfferAccepted(ClientExchange exchange);

		public void onSentOfferRefused(ClientExchange exchange);

		public void onOfferCame(ClientExchange exchange);

		public void onTradeDirect(ClientExchange exchange, String partner,
				int stockId, int amount, double price, boolean sold);

		public void onTradeIndirect(ClientExchange exchange, int stockId,
				int amount, double price, boolean sold);

		public void onEventsChanged(ClientExchange exchange);
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
	private String[] events = new String[0];

	private boolean sendingOffer;
	private boolean buyRequested;

	private Vector<IClientExchangeListener> listeners;

	public ClientExchange() {
		net = new NetClient();
		net.addListener(this);
		listeners = new Vector<IClientExchangeListener>();
	}

	public synchronized void addListener(IClientExchangeListener listener) {
		System.out.println(TAG+"Adding listener: "
				+ listener);
		listeners.add(listener);
	}

	public synchronized void removeListener(IClientExchangeListener listener) {
		System.out.println(TAG+"Removing listener: "
				+ listener);
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

	public synchronized double getStocksValue() {
		double ret = 0;
		for (int i = 0; i < stocks.length; i++)
			ret += stocks[i].getPrice() * myStocks[i];
		return ret;
	}

	public synchronized String getStockName(int stockId) {
		return stocks[stockId].getName();
	}

	public synchronized double getStockPrice(int stockId) {
		return stocks[stockId].getPrice();
	}

	public synchronized Stock getStock(int stockId) {
		return stocks[stockId];
	}

	public synchronized String[] getEvents() {
		return events;
	}

	public synchronized Offer[] getOutgoingOffers() {
		return myOffers.toArray(new Offer[myOffers.size()]);
	}

	public synchronized Offer[] getIncomingOffers() {
		return myOffers.toArray(new Offer[myOffers.size()]);
	}
	
	public synchronized boolean isBuyRequested() {
		return buyRequested;
	}

	public synchronized void connect(String host, int port, String nickName,
			String password) {
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
		buyRequested = false;
		net.connect(host, port);
	}

	public synchronized void doBuy(int[] stocks) {
		myStocks = stocks;
		net.sendMsg(new MsgClientBuy(stocks));
	}

	public synchronized void offer(int stockId, int amount, double price,
			boolean sell) {
		if (sendingOffer) {
			new Exception("Already sending an offer!").printStackTrace();
			return;
		}
		sendingOffer = true;
		net.sendMsg(new MsgClientOfferIndirect(stockId, amount, price, sell));
	}

	public synchronized void offerTo(int stockId, int amount, double price,
			boolean sell, int playerId) {
		if (sendingOffer) {
			new Exception("Already sending an offer!").printStackTrace();
			return;
		}
		sendingOffer = true;
		net.sendMsg(new MsgClientOfferDirect(stockId, amount, price, sell,
				playerNames[playerId]));
	}
	
	public synchronized void acceptOffer(Offer offer) {
		if(gameMode == GAMEMODE_INDIRECT) {
			new Exception("Wrong gamemode!").printStackTrace();
			return;
		}
		net.sendMsg(new MsgClientOfferDirect(offer.stockId, offer.amount, offer.price, offer.sell, offer.sender));
	}

	public synchronized void deleteOffer(Offer offer) {
		if(gameMode == GAMEMODE_DIRECT) {
			new Exception("Wrong gamemode!").printStackTrace();
			return;
		}
		net.sendMsg(new MsgClientOfferDeleteIndirect(offer.stockId, offer.amount, offer.price, offer.sell));
	}
	
	public synchronized void denyOffer(Offer offer) {
		if(gameMode == GAMEMODE_INDIRECT) {
			new Exception("Wrong gamemode!").printStackTrace();
			return;
		}
		net.sendMsg(new MsgClientOfferDeleteDirect(offer.stockId, offer.amount, offer.price, offer.sell, offer.sender));
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
		if (msg instanceof MsgServerConnAccept) {
			MsgServerConnAccept msgAccept = (MsgServerConnAccept) msg;
			gameMode = msgAccept.gameMode;
			stocks = new Stock[msgAccept.stockNames.length];
			for (int i = 0; i < stocks.length; i++) {
				stocks[i] = new Stock(msgAccept.stockNames[i],
						msgAccept.stockPrices[i]);
				stocks[i].setIngame(msgAccept.stocksIngame[i]);
			}
			myMoney = msgAccept.playerMoney;
			myStocks = msgAccept.playerStocks;
			for (IClientExchangeListener listener : listeners)
				listener.onConnAccepted(this);
		} else if (msg instanceof MsgServerConnRefuse) {
			for (IClientExchangeListener listener : listeners)
				listener.onConnRefused(this);
		} else if (msg instanceof MsgServerBuyRequest) {
			buyRequested = true;
			for (IClientExchangeListener listener : listeners)
				listener.onShowBuy(this);
		} else if (msg instanceof MsgServerBuyAccept) {
			for (IClientExchangeListener listener : listeners)
				listener.onBuyAccepted(this);
		} else if (msg instanceof MsgServerBuyRefuse) {
			for (IClientExchangeListener listener : listeners)
				listener.onBuyRefused(this);
		} else if (msg instanceof MsgServerBuyEnd) {
			buyRequested = false;
			MsgServerBuyEnd msgEnd = (MsgServerBuyEnd) msg;
			for (int i = 0; i < stocks.length; i++)
				stocks[i].setIngame(msgEnd.stocksIngame[i]);
			for (IClientExchangeListener listener : listeners)
				listener.onBuyEnd(this);
		} else if (msg instanceof MsgServerPlayers) {
			playerNames = ((MsgServerPlayers) msg).playerNames;
		} else if (msg instanceof MsgServerEvents) {
			events = ((MsgServerEvents) msg).events;
			if (events == null)
				events = new String[0];
			for (IClientExchangeListener listener : listeners)
				listener.onEventsChanged(this);
		} else if(msg instanceof MsgServerPlayerMoney) {
			myMoney = ((MsgServerPlayerMoney) msg).money;
			for(IClientExchangeListener listener : listeners)
				listener.onMyMoneyChanged(this);
		} else if (msg instanceof MsgServerStockUpdate) {
			MsgServerStockUpdate msgUpdate = (MsgServerStockUpdate) msg;
			for (int i = 0; i < stocks.length; i++)
				stocks[i].setPrice(msgUpdate.prices[i]);
			for (IClientExchangeListener listener : listeners)
				listener.onStocksChanged(this);
		} else if (msg instanceof MsgServerStockOfferUpdate) {
			MsgServerStockOfferUpdate msgUpdate = (MsgServerStockOfferUpdate) msg;
			stocks[msgUpdate.stockId].setMinMaxOffers(msgUpdate.minSellOffer,
					msgUpdate.maxBuyOffer);
			for (IClientExchangeListener listener : listeners)
				listener.onStocksChanged(this);
		} else if (msg instanceof MsgServerSentOfferIndirectAccept) {
			MsgServerSentOfferIndirectAccept msgAccept = (MsgServerSentOfferIndirectAccept) msg;
			myOffers.add(new Offer(myName, null, msgAccept.stockId,
					msgAccept.amount, msgAccept.price, msgAccept.sell));
			sendingOffer = false;
			for (IClientExchangeListener listener : listeners)
				listener.onSentOfferAccepted(this);
		} else if (msg instanceof MsgServerSentOfferDirectAccept) {
			MsgServerSentOfferDirectAccept msgAccept = (MsgServerSentOfferDirectAccept) msg;
			myOffers.add(new Offer(myName, msgAccept.target, msgAccept.stockId,
					msgAccept.amount, msgAccept.price, msgAccept.sell));
			sendingOffer = false;
			for (IClientExchangeListener listener : listeners)
				listener.onSentOfferAccepted(this);
		} else if (msg instanceof MsgServerSentOfferRefuse) {
			sendingOffer = false;
			for (IClientExchangeListener listener : listeners)
				listener.onSentOfferRefused(this);
		} else if (msg instanceof MsgServerTradeDirect) {
			MsgServerTradeDirect msgTrade = (MsgServerTradeDirect) msg;
			myMoney += msgTrade.sell ? msgTrade.amount * msgTrade.price
					: -msgTrade.amount * msgTrade.price;
			myStocks[msgTrade.stockId] += msgTrade.sell ? -msgTrade.amount
					: msgTrade.amount;
			for (IClientExchangeListener listener : listeners)
				listener.onTradeDirect(this, msgTrade.partner,
						msgTrade.stockId, msgTrade.amount, msgTrade.price,
						msgTrade.sell);
			for (IClientExchangeListener listener : listeners)
				listener.onMyMoneyChanged(this);
			for (IClientExchangeListener listener : listeners)
				listener.onMyStocksChanged(this);
		} else if (msg instanceof MsgServerTradeIndirect) {
			MsgServerTradeIndirect msgTrade = (MsgServerTradeIndirect) msg;
			myMoney += msgTrade.sell ? msgTrade.amount * msgTrade.price
					: -msgTrade.amount * msgTrade.price;
			myStocks[msgTrade.stockId] += msgTrade.sell ? -msgTrade.amount
					: msgTrade.amount;
			for (IClientExchangeListener listener : listeners)
				listener.onTradeIndirect(this, msgTrade.stockId,
						msgTrade.amount, msgTrade.price, msgTrade.sell);
			for (IClientExchangeListener listener : listeners)
				listener.onMyMoneyChanged(this);
			for (IClientExchangeListener listener : listeners)
				listener.onMyStocksChanged(this);
		} else if (msg instanceof MsgServerOfferToYou) {
			MsgServerOfferToYou msgOffer = (MsgServerOfferToYou) msg;
			incomingOffers.add(new Offer(msgOffer.sender, null,
					msgOffer.stockId, msgOffer.amount, msgOffer.price,
					msgOffer.sell));
			for (IClientExchangeListener listener : listeners)
				listener.onOfferCame(this);
		}
	}

	@Override
	public synchronized void onClosed(NetClient net, Exception e) {
		// TODO switch based on exception
		if (e != null)
			for (IClientExchangeListener listener : listeners)
				listener.onConnLost(this);
	}

	public synchronized double calculateMoneyAfterPurchase(int[] amounts) {
		if (amounts.length != getStocksNumber())
			throw new IllegalArgumentException("Bad length");
		double out = getMoney();
		for (int i = 0; i < getStocksNumber(); i++)
			out -= amounts[i] * getStockPrice(i);
		return out;
	}
}
