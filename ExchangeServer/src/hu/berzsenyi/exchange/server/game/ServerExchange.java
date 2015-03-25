package hu.berzsenyi.exchange.server.game;

import java.io.File;
import java.io.FileReader;
import java.util.Random;
import java.util.Vector;

import hu.berzsenyi.exchange.game.Exchange;
import hu.berzsenyi.exchange.game.Offer;
import hu.berzsenyi.exchange.net.NetServer;
import hu.berzsenyi.exchange.net.NetServer.NetServerClient;
import hu.berzsenyi.exchange.net.msg.*;
import hu.berzsenyi.exchange.server.game.ServerStock.IOfferCallback;

public class ServerExchange extends Exchange implements
		NetServer.INetServerListener, IOfferCallback {
	public static interface IServerExchangeListener {
		public void onOpened(ServerExchange exchange);

		public void onEvent(ServerExchange exchange);

		public void onMsgReceived(ServerExchange exchange);

		public void onClosed(ServerExchange exchange);
	}

	public static final ServerExchange INSTANCE = new ServerExchange();

	public NetServer net;

	private Random rand;
	private int gameMode;
	private double startMoney;
	private ServerStock[] stocks;
	private Vector<ServerPlayer> players;
	private boolean started;
	private int[] shuffledEvents;
	private EventQueue[] events;
	private Vector<EventQueue> currentEvents;
	private int nextEventNumber;

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
	
	public synchronized boolean isStarted() {
		return started;
	}

	public synchronized double getStartMoney() {
		return startMoney;
	}

	public synchronized int getStockNumber() {
		return stocks.length;
	}

	public synchronized ServerStock getStock(int stockId) {
		return stocks[stockId];
	}
	
	public synchronized int getPlayerNumber() {
		return players.size();
	}
	
	public synchronized ServerPlayer getPlayer(int i) {
		return players.get(i);
	}

	public synchronized ServerPlayer getPlayerByName(String name) {
		for (ServerPlayer player : players)
			if (player.name.equals(name))
				return player;
		return null;
	}

	private synchronized ServerPlayer getPlayerByNetId(String netId) {
		for (ServerPlayer player : players)
			if (player.getNetId().equals(netId))
				return player;
		return null;
	}
	
	public synchronized void updateStocks() {
		for (int i = 0; i < stocks.length; i++)
			stocks[i].updatePrice(1);
		net.sendMsgToAll(new MsgServerStockUpdate(stocks));
	}

	public synchronized void newEventAndUpdateStocks() {
		if (!started) {
			net.sendMsgToAll(new MsgServerBuyEnd(stocks));
			started = true;
		}
		
		for (int i = 0; i < stocks.length; i++) {
			double multiplier = 1;
			for(int e = 0; e < currentEvents.size(); e++)
				multiplier *= currentEvents.get(e).getMultiplier(i);
			stocks[i].updatePrice(multiplier);
		}
		
		for(int e = 0; e < currentEvents.size(); e++) {
			EventQueue nextEvent = currentEvents.get(e).getNextEvent();
			if(nextEvent == null)
				currentEvents.remove(e--);
			else
				currentEvents.set(e, nextEvent);
		}
		currentEvents.add(events[shuffledEvents[(nextEventNumber++)%events.length]]);
		
		// TODO send data from events
		net.sendMsgToAll(new MsgServerStockUpdate(stocks));
		for (IServerExchangeListener listener : listeners)
			listener.onEvent(this);
	}

	public synchronized void load(String path) {

	}

	public synchronized void save(String path) {

	}

	public synchronized void loadStocks(String stockFolder) {
		File[] files = new File(stockFolder).listFiles();
		stocks = new ServerStock[files.length];
		for (int s = 0; s < stocks.length; s++) {
			try {
				DatParser parser = new DatParser(files[s].getPath());
				parser.parse();
				stocks[s] = new ServerStock(files[s].getName().substring(0,
						files[s].getName().lastIndexOf('.')),
						parser.getValue("name"), Double.parseDouble(parser
								.getValue("initvalue")));
			} catch (Exception e) {
				e.printStackTrace();
				System.err.println("Failed to parse stock: "
						+ files[s].getName());
			}
		}
	}

	public synchronized void open(int port, int gameMode, double startMoney) {
		try {
			rand = new Random(System.currentTimeMillis());
			this.gameMode = gameMode;
			this.startMoney = startMoney;
			loadStocks("data/stocks");
			players = new Vector<ServerPlayer>();
			started = false;
			events = EventParser.parseEvents(this, new FileReader("data/events/events.xml"));
			shuffledEvents = ArrayHelper.createShuffledIntArray(events.length, rand);
			currentEvents = new Vector<EventQueue>();
			nextEventNumber = 0;
			net.open(port);
		} catch (Exception e) {
			e.printStackTrace();
			close();
		}
	}

	public synchronized void close() {
		net.close();
		for (IServerExchangeListener listener : listeners)
			listener.onClosed(this);
	}

	@Override
	public synchronized void onOpened(NetServer net) {
		for (IServerExchangeListener listener : listeners)
			listener.onOpened(this);
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
					player = new ServerPlayer(msgConn.nickName,
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
			else if (player.password.equals(msgConn.password)) {
				player.setNetId(netClient.getId());
				netClient.sendMsg(new MsgServerConnAccept(gameMode, stocks,
						player.getMoney(), player.getStocks()));
				if (!started) {
					if (player.getMoney() == startMoney)
						netClient.sendMsg(new MsgServerBuyRequest());
				} else if (gameMode == GAMEMODE_DIRECT) {
					String[] playerNames = new String[players.size()];
					for (int i = 0; i < players.size(); i++)
						playerNames[i] = players.get(i).name;
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
						player.setStocks(msgBuy.stocks);
						player.setMoney(player.getMoney() - money);
						netClient.sendMsg(new MsgServerBuyAccept());
					} else {
						netClient.sendMsg(new MsgServerBuyRefuse());
					}
				} else {
					netClient.sendMsg(new MsgServerBuyRefuse());
				}
			}
		} else if (msg instanceof MsgClientOfferIndirect) {
			if (gameMode == GAMEMODE_INDIRECT) {
				MsgClientOfferIndirect msgOffer = (MsgClientOfferIndirect) msg;
				ServerPlayer player = getPlayerByNetId(netClient.getId()); // TODO if connection not accepted, player could be null
				if(player == null) {
					// TODO error, client not connected
				} else if ((msgOffer.sell && msgOffer.amount <= player.getStockAmount(msgOffer.stockId))
						|| (!msgOffer.sell && msgOffer.price * msgOffer.amount <= player.getMoney())) {
					netClient.sendMsg(new MsgServerSentOfferIndirectAccept(
							msgOffer.stockId, msgOffer.amount, msgOffer.price,
							msgOffer.sell));
					stocks[msgOffer.stockId].addOffer(player.name,
							msgOffer.stockId, msgOffer.amount, msgOffer.price,
							msgOffer.sell, this);
				} else {
					netClient.sendMsg(new MsgServerSentOfferRefuse());
				}
			} else {
				// TODO error
			}
		} else if (msg instanceof MsgClientOfferDirect) {
			if (gameMode == GAMEMODE_DIRECT) {
				MsgClientOfferDirect msgOffer = (MsgClientOfferDirect) msg;
				ServerPlayer player = getPlayerByNetId(netClient.getId());
				if ((msgOffer.sell && msgOffer.amount <= player.getStockAmount(msgOffer.stockId))
						|| (!msgOffer.sell && msgOffer.price * msgOffer.amount <= player.getMoney())) {
					netClient.sendMsg(new MsgServerSentOfferDirectAccept(
							msgOffer.player, msgOffer.stockId, msgOffer.amount,
							msgOffer.price, msgOffer.sell));
					stocks[msgOffer.stockId].addOfferTo(player.name,
							msgOffer.player, msgOffer.stockId, msgOffer.amount,
							msgOffer.price, msgOffer.sell, this);
				} else {
					// TODO error, not enough money or stocks
				}
			} else {
				// TODO error, wrong gamemode
			}
		}
	}

	@Override
	public synchronized void onClientClosed(NetServer net,
			NetServerClient netClient, Exception e) {

	}

	@Override
	public synchronized void onClosed(NetServer net, Exception e) {
		close();
	}

	@Override
	public synchronized void onOffersPaired(Offer buyOffer, Offer sellOffer,
			double price, int amount) {
		ServerPlayer playerSeller = getPlayerByName(sellOffer.sender);
		ServerPlayer playerBuyer = getPlayerByName(buyOffer.sender);
		playerSeller.setStockAmount(buyOffer.stockId, playerSeller.getStockAmount(buyOffer.stockId)-amount);
		playerBuyer.setStockAmount(buyOffer.stockId, playerBuyer.getStockAmount(buyOffer.stockId)+amount);
		playerSeller.setMoney(playerSeller.getMoney()+amount*price);
		playerBuyer.setMoney(playerBuyer.getMoney()-amount*price);
		if (gameMode == GAMEMODE_DIRECT) {
			net.sendMsgToXY(new MsgServerTradeDirect(playerSeller.name,
					buyOffer.stockId, amount, price, false), playerBuyer.getNetId());
			net.sendMsgToXY(new MsgServerTradeDirect(playerBuyer.name,
					buyOffer.stockId, amount, price, true), playerSeller.getNetId());
		} else {
			net.sendMsgToXY(new MsgServerTradeIndirect(buyOffer.stockId,
					amount, price, false), playerBuyer.getNetId());
			net.sendMsgToXY(new MsgServerTradeIndirect(buyOffer.stockId,
					amount, price, true), playerSeller.getNetId());
		}
	}
}
