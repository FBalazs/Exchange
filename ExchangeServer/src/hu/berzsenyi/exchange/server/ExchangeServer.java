package hu.berzsenyi.exchange.server;

import hu.berzsenyi.exchange.Model;
import hu.berzsenyi.exchange.net.IServerListener;
import hu.berzsenyi.exchange.net.TCPConnection;
import hu.berzsenyi.exchange.net.TCPServer;
import hu.berzsenyi.exchange.net.TCPServerClient;
import hu.berzsenyi.exchange.net.cmd.CmdClientDisconnect;
import hu.berzsenyi.exchange.net.cmd.CmdClientInfo;
import hu.berzsenyi.exchange.net.cmd.CmdOffer;
import hu.berzsenyi.exchange.net.cmd.CmdClientOfferResponse;
import hu.berzsenyi.exchange.net.cmd.CmdServerExchange;
import hu.berzsenyi.exchange.net.cmd.CmdServerInfo;
import hu.berzsenyi.exchange.net.cmd.ICmdHandler;
import hu.berzsenyi.exchange.net.cmd.TCPCommand;

public class ExchangeServer implements Runnable, IServerListener, ICmdHandler {
	public boolean running;
	public TCPServer net;
	public ServerDisplay display;
	public Model model;
	
	public void create() {
		this.display = new ServerDisplay(this);
		
		this.model = new Model();
		this.model.loadStocks("data/stocks");
		
		this.net = new TCPServer(8080, this, this);
	}
	
	@Override
	public void onClientConnected(TCPServerClient client) {
		System.out.println("Client connected!");
		
	}
	
	@Override
	public void handleCmd(TCPCommand cmd, TCPConnection conn) {
		System.out.println("Received command! "+cmd.getClass().getName());
		
		if(cmd instanceof CmdClientInfo) {
			if(this.model.round == 0) {
				this.model.newTeam(conn.getAddrString(), ((CmdClientInfo)cmd).name);
				conn.writeCommand(new CmdServerInfo(this.model));
			} else {
				// TODO send feedback and disconnect client
			}
			return;
		}
		
		if(cmd instanceof CmdClientDisconnect) {
			this.model.removeTeam(conn.getAddrString());
			conn.close();
			return;
		}
		
		if(cmd instanceof CmdOffer) {
			CmdOffer offer = (CmdOffer)cmd;
			String to = offer.playerID;
			offer.playerID = conn.getAddrString();
			this.net.writeCmdTo(offer, to);
			return;
		}
		
		if(cmd instanceof CmdClientOfferResponse) {
			CmdClientOfferResponse offer = (CmdClientOfferResponse)cmd;
			// TODO handle exchange
			this.net.writeCmdToAll(new CmdServerExchange(offer.playerID, conn.getAddrString(), offer.stockID, offer.amount, offer.money));
			return;
		}
	}

	@Override
	public void onClientDisconnected(TCPServerClient client) {
		System.out.println("Client disconnected!");
		this.model.removeTeam(client.getAddrString());
	}
	
	public void update() {
		if(!this.net.open)
			this.net = new TCPServer(8080, this, this);
		this.net.update();
		//System.out.println("clients: "+this.net.getClientNumber());
	}
	
	public void render() {
		
	}
	
	public void destroy() {
		this.net.close();
	}
	
	@Override
	public void run() {
		this.create();
		this.running = true;
		while(this.running) {
			long time = System.currentTimeMillis();
			this.update();
			this.render();
			time = 1000/25-(System.currentTimeMillis()-time);
			if(0 < time)
				try {
					Thread.sleep(time);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
//			System.out.println(this.net.getClientNumber());
		}
		this.destroy();
		System.exit(0);
	}
	
	public static void main(String[] args) {
		new Thread(new ExchangeServer(), "Thread-Server").start();
	}
}
