package hu.berzsenyi.exchange.server;

import java.util.Vector;

import hu.berzsenyi.exchange.Exchange;
import hu.berzsenyi.exchange.Offer;
import hu.berzsenyi.exchange.net.NetServer;
import hu.berzsenyi.exchange.net.NetServer.NetServerClient;
import hu.berzsenyi.exchange.net.msg.Msg;
import hu.berzsenyi.exchange.net.msg.MsgClientBuy;
import hu.berzsenyi.exchange.net.msg.MsgClientConnRequest;
import hu.berzsenyi.exchange.net.msg.MsgClientOffer;
import hu.berzsenyi.exchange.net.msg.MsgClientOfferTo;
import hu.berzsenyi.exchange.net.msg.MsgServerBuyAccept;
import hu.berzsenyi.exchange.net.msg.MsgServerBuyRefuse;
import hu.berzsenyi.exchange.net.msg.MsgServerBuyRequest;
import hu.berzsenyi.exchange.net.msg.MsgServerConnAccept;
import hu.berzsenyi.exchange.net.msg.MsgServerConnRefuse;
import hu.berzsenyi.exchange.net.msg.MsgServerPlayers;
import hu.berzsenyi.exchange.net.msg.MsgServerSentOfferAccept;
import hu.berzsenyi.exchange.net.msg.MsgServerSentOfferRefuse;
import hu.berzsenyi.exchange.server.ServerStock.IOfferCallback;

public class ServerExchange extends Exchange implements NetServer.INetServerListener, IOfferCallback {
	public static interface IServerExchangeListener {
		public void onOpened(ServerExchange exchange);
		public void onConnAccepted(ServerExchange exchange);
		public void onEvent(ServerExchange exchange);
		public void onClosed(ServerExchange exchange);
	}
	
	public static final ServerExchange INSTANCE = new ServerExchange();
	
	private NetServer net;
	
	private int gameMode;
	private double startMoney;
	private ServerStock[] stocks;
	private Vector<ServerPlayer> players;
	private boolean started;
	
	private Vector<IServerExchangeListener> listeners;
	
	public ServerExchange() {
		net = new NetServer();
		net.addListener(this);
		listeners = new Vector<>();
	}
	
	public synchronized void addListener(IServerExchangeListener listener) {
		listeners.add(listener);
	}
	
	public synchronized void removeListener(IServerExchangeListener listener) {
		listeners.remove(listener);
	}
	
	public synchronized double getStartMoney() {
		return startMoney;
	}
	
	public synchronized int getStockNumber() {
		return stocks.length;
	}
	
	public synchronized ServerPlayer getPlayerByName(String name) {
		for(ServerPlayer player : players)
			if(player.name.equals(name))
				return player;
		return null;
	}
	
	private synchronized ServerPlayer getPlayerByNetId(String netId) {
		for(ServerPlayer player : players)
			if(player.netId.equals(netId))
				return player;
		return null;
	}
	
	public synchronized void load(String path) {
		
	}
	
	public synchronized void save(String path) {
		
	}
	
	public synchronized void open(int port) {
		// TODO load
		gameMode = GAMEMODE_DIRECT;
		startMoney = 10000;
		stocks = new ServerStock[10];
		players = new Vector<ServerPlayer>();
		started = false;
		
		net.open(port);
	}
	
	public synchronized void close() {
		net.close();
	}

	@Override
	public synchronized void onOpened(NetServer net) {
		
	}

	@Override
	public synchronized void onClientConnected(NetServer net, NetServerClient netClient) {
		
	}

	@Override
	public synchronized void onObjectReceived(NetServer net, NetServerClient netClient,
			Msg msg) {
		if(msg instanceof MsgClientConnRequest) {
			MsgClientConnRequest msgConn = (MsgClientConnRequest) msg;
			ServerPlayer player = getPlayerByName(msgConn.nickName);
			if(player == null)
				if(!started) {
					player = new ServerPlayer(msgConn.nickName, msgConn.password, netClient.getId());
					players.add(player);
					netClient.sendMsg(new MsgServerConnAccept(gameMode, stocks, player.money, player.stocks));
					if(player.money == startMoney)
						netClient.sendMsg(new MsgServerBuyRequest());
				} else {
					netClient.sendMsg(new MsgServerConnRefuse(MsgServerConnRefuse.NOT_0ROUND));
				}
			else
				if(player.password.equals(msgConn.password)) {
					player.netId = netClient.getId();
					netClient.sendMsg(new MsgServerConnAccept(gameMode, stocks, player.money, player.stocks));
					if(!started) {
						if(player.money == startMoney)
							netClient.sendMsg(new MsgServerBuyRequest());
					} else if(gameMode == GAMEMODE_DIRECT) {
						String[] playerNames = new String[players.size()];
						for(int i = 0; i < players.size(); i++)
							playerNames[i] = players.get(i).name;
						netClient.sendMsg(new MsgServerPlayers(playerNames));
					}
				} else {
					netClient.sendMsg(new MsgServerConnRefuse(MsgServerConnRefuse.BAD_PASSWORD));
				}
		} else if(msg instanceof MsgClientBuy) {
			if(started) {
				netClient.sendMsg(new MsgServerBuyRefuse());
			} else {
				ServerPlayer player = getPlayerByNetId(netClient.getId());
				if(player.money == startMoney) {
					MsgClientBuy msgBuy = (MsgClientBuy)msg;
					double money = 0;
					for(int i = 0; i < stocks.length; i++)
						money += msgBuy.stocks[i]*stocks[i].getPrice();
					if(money <= player.money) {
						player.stocks = msgBuy.stocks;
						player.money = player.money-money;
						netClient.sendMsg(new MsgServerBuyAccept());
					} else {
						netClient.sendMsg(new MsgServerBuyRefuse());
					}
				} else {
					netClient.sendMsg(new MsgServerBuyRefuse());
				}
			}
		} else if(msg instanceof MsgClientOffer) {
			if(gameMode == GAMEMODE_INDIRECT) {
				MsgClientOffer msgOffer = (MsgClientOffer)msg;
				ServerPlayer player = getPlayerByNetId(netClient.getId()); // TODO if connection not accepted, player could be null
				if((msgOffer.sell && msgOffer.amount <= player.stocks[msgOffer.stockId])
					|| (!msgOffer.sell && msgOffer.price*msgOffer.amount <= player.money)) {
					netClient.sendMsg(new MsgServerSentOfferAccept(msgOffer.stockId, msgOffer.amount, msgOffer.price, msgOffer.sell));
					stocks[msgOffer.stockId].addOffer(player.name, msgOffer.stockId, msgOffer.amount, msgOffer.price, msgOffer.sell, this);
				} else {
					netClient.sendMsg(new MsgServerSentOfferRefuse());
				}
			} else {
				// TODO error
			}
		} else if(msg instanceof MsgClientOfferTo) {
			if(gameMode == GAMEMODE_DIRECT) {
				MsgClientOfferTo msgOffer = (MsgClientOfferTo)msg;
				
			} else {
				// TODO error
			}
		}
	}

	@Override
	public synchronized void onClientClosed(NetServer net, NetServerClient netClient) {
		
	}

	@Override
	public synchronized void onClosed(NetServer net, Exception e) {
		
	}

	@Override
	public synchronized void onOffersPaired(Offer buyOffer, Offer sellOffer, double price, int amount) {
		ServerPlayer playerSeller = getPlayerByName(sellOffer.sender);
		ServerPlayer playerBuyer = getPlayerByName(buyOffer.sender);
		playerSeller.stocks[buyOffer.stockId] -= amount;
		playerBuyer.stocks[buyOffer.stockId] += amount;
		playerSeller.money += amount*price;
		playerBuyer.money -= amount*price;
		// TODO comm
	}
}
