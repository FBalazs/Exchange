package hu.berzsenyi.exchange.server;

import java.util.Vector;

import hu.berzsenyi.exchange.Exchange;
import hu.berzsenyi.exchange.net.NetServer;
import hu.berzsenyi.exchange.net.NetServer.NetServerClient;
import hu.berzsenyi.exchange.net.msg.Msg;
import hu.berzsenyi.exchange.net.msg.MsgClientBuy;
import hu.berzsenyi.exchange.net.msg.MsgClientConnRequest;
import hu.berzsenyi.exchange.net.msg.MsgServerBuyAccept;
import hu.berzsenyi.exchange.net.msg.MsgServerBuyRefuse;
import hu.berzsenyi.exchange.net.msg.MsgServerBuyRequest;
import hu.berzsenyi.exchange.net.msg.MsgServerConnAccept;
import hu.berzsenyi.exchange.net.msg.MsgServerConnRefuse;
import hu.berzsenyi.exchange.net.msg.MsgServerPlayers;

public class ExchangeServer extends Exchange implements NetServer.INetServerListener {
	public static interface IExchangeServerListener {
		public void onOpened(ExchangeServer exchange);
		public void onClosed(ExchangeServer exchange);
		public void onConnAccepted(ExchangeServer exchange);
	}
	
	public static final ExchangeServer INSTANCE = new ExchangeServer();
	
	private NetServer net;
	
	public int gameMode;
	public double startMoney;
	public StockServer[] stocks;
	public Vector<PlayerServer> players;
	public boolean started;
	
	private Vector<IExchangeServerListener> listeners;
	
	public ExchangeServer() {
		net = new NetServer();
		net.addListener(this);
		listeners = new Vector<>();
	}
	
	public synchronized void addListener(IExchangeServerListener listener) {
		listeners.add(listener);
	}
	
	public synchronized void removeListener(IExchangeServerListener listener) {
		listeners.remove(listener);
	}
	
	public synchronized PlayerServer getPlayerByName(String name) {
		for(PlayerServer player : players)
			if(player.name.equals(name))
				return player;
		return null;
	}
	
	private synchronized PlayerServer getPlayerByNetId(String netId) {
		for(PlayerServer player : players)
			if(player.netId.equals(netId))
				return player;
		return null;
	}
	
	public synchronized void open(int port) {
		// TODO load
		gameMode = GAMEMODE_DIRECT;
		startMoney = 10000;
		stocks = new StockServer[10];
		players = new Vector<PlayerServer>();
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
			PlayerServer player = getPlayerByName(msgConn.nickName);
			if(player == null)
				if(!started) {
					player = new PlayerServer(msgConn.nickName, msgConn.password, netClient.getId());
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
				PlayerServer player = getPlayerByNetId(netClient.getId());
				if(player.money == startMoney) {
					MsgClientBuy msgBuy = (MsgClientBuy)msg;
					double money = 0;
					for(int i = 0; i < stocks.length; i++)
						money += msgBuy.stocks[i]*stocks[i].price;
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
		}
	}

	@Override
	public synchronized void onClientClosed(NetServer net, NetServerClient netClient) {
		
	}

	@Override
	public synchronized void onClosed(NetServer net) {
		
	}
}
