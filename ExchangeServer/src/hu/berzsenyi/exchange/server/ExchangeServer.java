package hu.berzsenyi.exchange.server;

import java.util.Vector;

import hu.berzsenyi.exchange.Exchange;
import hu.berzsenyi.exchange.net.NetServer;
import hu.berzsenyi.exchange.net.NetServer.NetServerClient;
import hu.berzsenyi.exchange.net.msg.Msg;

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
	
	public synchronized void open(int port) {
		// TODO load
		gameMode = GAMEMODE_DIRECT;
		startMoney = 10000;
		stocks = new StockServer[10];
		players = new Vector<PlayerServer>();
		
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
		
	}

	@Override
	public synchronized void onClientClosed(NetServer net, NetServerClient netClient) {
		
	}

	@Override
	public synchronized void onClosed(NetServer net) {
		
	}
}
