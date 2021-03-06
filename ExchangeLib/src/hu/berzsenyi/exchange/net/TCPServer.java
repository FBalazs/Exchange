package hu.berzsenyi.exchange.net;

import hu.berzsenyi.exchange.net.msg.IMsgHandler;
import hu.berzsenyi.exchange.net.msg.Msg;

import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class TCPServer implements IServerClientListener {
	public class TCPListenThread extends Thread {
		public TCPServer server;
		
		public TCPListenThread(TCPServer server) {
			super("Thread-TCPListen");
			this.server = server;
		}
		
		@Override
		public void run() {
			while(this.server.open) {
				try {
					Socket socket = this.server.socket.accept();
					if(socket != null)
						synchronized (this.server.clients) {
							TCPServerClient client = new TCPServerClient(socket, this.server.cmdHandler);
							client.setListener(TCPServer.this);
							this.server.clients.add(client);
//							if(this.server.listener != null)
//								this.server.listener.onClientConnected(client);
						}
				} catch(Exception e) {
					e.printStackTrace();
					this.server.close();
				}
			}
		}
	}
	
	public ServerSocket socket;
	public List<TCPServerClient> clients = new ArrayList<TCPServerClient>();
	public boolean open = true;
	public IMsgHandler cmdHandler;
	public IServerListener listener;
	
	public TCPServer(int port, IMsgHandler cmdHandler, IServerListener listener) {
		try {
			this.listener = listener;
			this.socket = new ServerSocket(port);
			this.socket.setSoTimeout(0);
			this.cmdHandler = cmdHandler;
			new TCPListenThread(this).start();
		} catch(Exception e) {
			e.printStackTrace();
			this.close();
		}
	}
	
	@Override
	public void onConnect(TCPServerClient client) {
		if(this.listener != null)
			this.listener.onClientConnected(client);
	}

	@Override
	public void onClose(TCPServerClient client) {
		/*synchronized (this.clients) {
			this.clients.remove(client);
			if(this.listener != null)
				this.listener.onClientDisconnected(client);
		}*/
		if(this.listener != null)
			this.listener.onClientDisconnected(client);
	}
	
//	public void update() {
//		try {
//			synchronized (this.clients) {
//				/*for(TCPServerClient client : this.clients) {
//					client.update();
//				}*/
//				for(int i = 0; i < this.clients.size(); i++)
//					if(!this.clients.get(i).open) {
//						if(this.listener != null)
//							this.listener.onClientDisconnected(this.clients.get(i));
//						this.clients.remove(i--);
//					}
//			}
//		} catch(Exception e) {
//			e.printStackTrace();
//			this.close();
//		}
//	}
	
	public int getClientNumber() {
		try {
			synchronized (this.clients) {
				return this.clients.size();
			}
		} catch(Exception e) {
			e.printStackTrace();
			this.close();
			return -1;
		}
	}
	
	public void writeCmdTo(Msg o, String addr) {
		synchronized (this.clients) {
			for(int i = 0; i < clients.size(); i++)
				if(clients.get(i).getAddrString().equals(addr)) {
					clients.get(i).writeCommand(o);
					if(!clients.get(i).open)
						clients.remove(i--);
				}
		}
	}
	
	public void writeCmdToAllExcept(Msg o, String addr) {
		synchronized (this.clients) {
			for(int i = 0; i < clients.size(); i++)
				if(!clients.get(i).getAddrString().equals(addr)) {
					clients.get(i).writeCommand(o);
					if(!clients.get(i).open)
						clients.remove(i--);
				}
		}
	}
	
	public void writeCmdToAll(Msg o) {
		synchronized (this.clients) {
			for(int i = 0; i < clients.size(); i++) {
				clients.get(i).writeCommand(o);
				if(!clients.get(i).open)
					clients.remove(i--);
			}
		}
	}
	
	public void close() {
		this.open = false;
		try {
			if(this.clients != null)
				for(int i = 0; i < this.clients.size(); i++)
					this.clients.get(i).close();
		} catch(Exception e) {
			e.printStackTrace();
		}
		try {
			if(this.socket != null)
				this.socket.close();
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
}
