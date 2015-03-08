package hu.berzsenyi.exchange.net;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class NetServer {
	public static interface INetServerListener {
		public void onOpened(NetServer net);
		public void onClientConnected(NetServer net, NetServerClient netClient);
		public void onObjectReceived(NetServer net, NetServerClient netClient, Object o);
		public void onClientClosed(NetServer net, NetServerClient netClient);
		public void onClosed(NetServer net);
	}
	
	public class NetServerClient {
		private class ThreadReceive extends Thread {
			@Override
			public void run() {
				while(connected)
					try {
						if(0 < oin.available()) {
							Object o = oin.readObject();
							for(INetServerListener listener : listeners)
								listener.onObjectReceived(NetServer.this, NetServerClient.this, o);
						}
					} catch(Exception e) {
						e.printStackTrace();
						close();
					}
			}
		}
		
		private Socket socket = null;
		private String id = null;
		private ObjectInputStream oin = null;
		private ObjectOutputStream oout = null;
		private boolean connected = false;
		
		private NetServerClient(Socket socket) {
			try {
				connected = true;
				this.socket = socket;
				id = socket.getInetAddress().toString()+":"+socket.getPort();
				oout = new ObjectOutputStream(socket.getOutputStream());
				oin = new ObjectInputStream(socket.getInputStream());
				new ThreadReceive().start();
			} catch(Exception e) {
				e.printStackTrace();
				close();
			}
		}
		
		public boolean isConnected() {
			return connected;
		}
		
		public String getId() {
			return id;
		}
		
		public void sendObject(Object o) {
			try {
				oout.writeObject(o);
				oout.flush();
			} catch(Exception e) {
				e.printStackTrace();
				close();
			}
		}
		
		public void close() {
			if(!connected)
				return;
			if(oin != null)
				try {
					oin.close();
					oin = null;
				} catch(Exception e) {
					e.printStackTrace();
				}
			if(oout != null)
				try {
					oout.close();
					oout = null;
				} catch(Exception e) {
					e.printStackTrace();
				}
			if(socket != null)
				try {
					socket.close();
					socket = null;
				} catch(Exception e) {
					e.printStackTrace();
				}
			connected = false;
			for(INetServerListener listener : listeners)
				listener.onClientClosed(NetServer.this, this);
			synchronized (clients) {
				clients.remove(this);
			}
		}
	}
	
	private class ThreadListen extends Thread {
		@Override
		public void run() {
			while(opened)
				try {
					Socket socket = serverSocket.accept();
					if(socket != null) {
						NetServerClient client = new NetServerClient(socket);
						synchronized (clients) {
							clients.add(client);
						}
						for(INetServerListener listener : listeners)
							listener.onClientConnected(NetServer.this, client);
					}
				} catch(Exception e) {
					e.printStackTrace();
					close();
				}
		}
	}
	
	private class ThreadOpen extends Thread {
		@Override
		public void run() {
			try {
				serverSocket = new ServerSocket(port);
				opened = true;
				opening = false;
				new ThreadListen().start();
			} catch(Exception e) {
				e.printStackTrace();
				close();
			}
		}
	}
	
	private ServerSocket serverSocket = null;
	private int port = -1;
	private List<NetServerClient> clients = new ArrayList<>();
	private boolean opened = false, opening = false;
	private List<INetServerListener> listeners = new ArrayList<>();
	
	public boolean isOpened() {
		return opened;
	}
	
	public boolean isOpening() {
		return opening;
	}
	
	public int getPort() {
		return this.port;
	}
	
	public void addListener(INetServerListener listener) {
		listeners.add(listener);
	}
	
	public void removeListener(INetServerListener listener) {
		listeners.remove(listener);
	}
	
	public void open(int port) {
		if(opened || opening)
			close();
		opening = true;
		this.port = port;
		new ThreadOpen().start();
	}
	
	public void sendObjectToXY(Object o, String id) {
		synchronized (clients) {
			for(NetServerClient client : clients)
				if(id.equals(client.id))
					client.sendObject(o);
		}
	}
	
	public void sendObjectToAllExceptXY(Object o, String id) {
		synchronized (clients) {
			for(NetServerClient client : clients)
				if(!id.equals(client.id))
					client.sendObject(o);
		}
	}
	
	public void sendObjectToAll(Object o) {
		synchronized (clients) {
			for(NetServerClient client : clients)
				client.sendObject(o);
		}
	}
	
	public void close() {
		if(!opened && !opening)
			return;
		for(NetServerClient client : clients)
			client.close();
		clients.clear();
		if(serverSocket != null)
			try {
				serverSocket.close();
				serverSocket = null;
			} catch(Exception e) {
				e.printStackTrace();
			}
	}
}
