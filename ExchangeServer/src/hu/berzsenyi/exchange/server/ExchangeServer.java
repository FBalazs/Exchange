package hu.berzsenyi.exchange.server;

import hu.berzsenyi.exchange.Model;
import hu.berzsenyi.exchange.net.IServerListener;
import hu.berzsenyi.exchange.net.TCPServer;
import hu.berzsenyi.exchange.net.TCPServerClient;

public class ExchangeServer implements Runnable, IServerListener {
	public boolean running;
	public TCPServer net;
	public ServerDisplay display;
	public Model model;
	
	public void create() {
		this.display = new ServerDisplay(this);
		
		this.model = new Model();
		this.model.loadStocks("data/stocks");
		
		this.net = new TCPServer(8080, new CmdHandlerServer(this), this);
	}
	
	@Override
	public void onClientConnected(TCPServerClient client) {
		System.out.println("Client connected!");
		
	}

	@Override
	public void onClientDisconnected(TCPServerClient client) {
		System.out.println("Client disconnected!");
		
	}
	
	public void update() {
		if(!this.net.open)
			this.net = new TCPServer(8080, new CmdHandlerServer(this), this);
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
