package hu.berzsenyi.exchange.server.game;

import java.util.Vector;

import hu.berzsenyi.exchange.game.ExchangeGame;
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

public class ServerExchangeGame extends ExchangeGame implements
		NetServer.INetServerListener {
	public static interface IExchangeServerListener {
		public void onOpened(ServerExchangeGame exchange);

		public void onClosed(ServerExchangeGame exchange);

		public void onConnAccepted(ServerExchangeGame exchange);
	}

	public static final ServerExchangeGame INSTANCE = new ServerExchangeGame();

	private NetServer net;

	public int gameMode;
	public double startMoney;
	public ServerStock[] stocks;
	public Vector<ServerPlayer> players;
	public boolean started;

	private Vector<IExchangeServerListener> listeners;

	public ServerExchangeGame() {
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

	public synchronized ServerPlayer getPlayerByName(String name) {
		for (ServerPlayer player : players)
			if (player.getName().equals(name))
				return player;
		return null;
	}

	private synchronized ServerPlayer getPlayerByNetId(String netId) {
		for (ServerPlayer player : players)
			if (player.netId.equals(netId))
				return player;
		return null;
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
	public synchronized void onClientConnected(NetServer net,
			NetServerClient netClient) {

	}

	@Override
	public synchronized void onObjectReceived(NetServer net,
			NetServerClient netClient, Msg msg) {
		if (msg instanceof MsgClientConnRequest) {
			MsgClientConnRequest msgConn = (MsgClientConnRequest) msg;
			ServerPlayer player = getPlayerByName(msgConn.nickName);
			if (player == null)
				if (!started) {
					player = new ServerPlayer(this, msgConn.nickName,
							msgConn.password, netClient.getId());
					players.add(player);
					netClient.sendMsg(new MsgServerConnAccept(gameMode, stocks,
							player.getMoney(), player.getStocks()));
					if (player.getMoney() == startMoney)
						netClient.sendMsg(new MsgServerBuyRequest());
				} else {
					netClient.sendMsg(new MsgServerConnRefuse(
							MsgServerConnRefuse.NOT_0ROUND));
				}
			else if (getPassword(player).equals(msgConn.password)) {
				player.netId = netClient.getId();
				netClient.sendMsg(new MsgServerConnAccept(gameMode, stocks,
						player.getMoney(), player.getStocks()));
				if (!started) {
					if (player.getMoney() == startMoney)
						netClient.sendMsg(new MsgServerBuyRequest());
				} else if (gameMode == GAMEMODE_DIRECT) {
					String[] playerNames = new String[players.size()];
					for (int i = 0; i < players.size(); i++)
						playerNames[i] = players.get(i).getName();
					netClient.sendMsg(new MsgServerPlayers(playerNames));
				}
			} else {
				netClient.sendMsg(new MsgServerConnRefuse(
						MsgServerConnRefuse.BAD_PASSWORD));
			}
		} else if (msg instanceof MsgClientBuy) {
			if (started) {
				netClient.sendMsg(new MsgServerBuyRefuse());
			} else {
				ServerPlayer player = getPlayerByNetId(netClient.getId());
				if (player.getMoney() == startMoney) {
					MsgClientBuy msgBuy = (MsgClientBuy) msg;
					double money = 0;
					for (int i = 0; i < stocks.length; i++)
						money += msgBuy.stocks[i] * stocks[i].getPrice();
					if (money <= player.getMoney()) {
						setStockAmounts(player, msgBuy.stocks);
						setMoney(player, player.getMoney() - money);
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
	public synchronized void onClientClosed(NetServer net,
			NetServerClient netClient) {

	}

	@Override
	public synchronized void onClosed(NetServer net) {

	}

	private static ServerStock[] loadStocks() {
		return null;
	}
}
