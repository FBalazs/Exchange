package hu.berzsenyi.exchange.net;

import hu.berzsenyi.exchange.net.msg.Msg;

import java.io.EOFException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Vector;

public class NetServer {
	public static interface INetServerListener {
		public void onOpened(NetServer net);

		public void onClientConnected(NetServer net, NetServerClient netClient);

		public void onObjectReceived(NetServer net, NetServerClient netClient,
				Msg msg);

		public void onClientClosed(NetServer net, NetServerClient netClient,
				Exception e);

		public void onClosed(NetServer net, Exception e);
	}

	public class NetServerClient {
		private class ThreadReceive extends Thread {
			@Override
			public void run() {
				while (connected)
					try {
						Msg msg = (Msg)oin.readObject();
						System.out.println("Received msg: "+msg);
						for(INetServerListener listener : listeners)
							listener.onObjectReceived(NetServer.this, NetServerClient.this, msg);
					} catch(EOFException e) {
						
					} catch(Exception e) {
						e.printStackTrace();
						close(e);
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
				id = socket.getInetAddress().toString() + ":"
						+ socket.getPort();
				oout = new ObjectOutputStream(socket.getOutputStream());
				oout.flush();
				oin = new ObjectInputStream(socket.getInputStream());
				new ThreadReceive().start();
			} catch (Exception e) {
				e.printStackTrace();
				close(e);
			}
		}

		public synchronized boolean isConnected() {
			return connected;
		}
		
		public synchronized String getId() {
			return id;
		}
		
		public synchronized void sendMsg(Msg msg) {
			System.out.println("Sending msg: "+msg);
			try {
				oout.writeObject(msg);
				oout.flush();
			} catch (Exception e) {
				e.printStackTrace();
				close(e);
			}
		}
		
		public synchronized void close() {
			close(null);
		}
		
		private synchronized void close(Exception e) {
			if(!connected)
				return;
			if (oin != null)
				try {
					oin.close();
					oin = null;
				} catch (Exception e1) {
					e1.printStackTrace();
				}
			if (oout != null)
				try {
					oout.close();
					oout = null;
				} catch (Exception e1) {
					e1.printStackTrace();
				}
			if (socket != null)
				try {
					socket.close();
					socket = null;
				} catch (Exception e1) {
					e1.printStackTrace();
				}
			connected = false;
			for (INetServerListener listener : listeners)
				listener.onClientClosed(NetServer.this, this, e);
		}
	}

	private class ThreadListen extends Thread {
		@Override
		public void run() {
			while (opened)
				try {
					Socket socket = serverSocket.accept();
					if (socket != null) {
						NetServerClient client = new NetServerClient(socket);
						synchronized (clients) {
							clients.add(client);
						}
						for (INetServerListener listener : listeners)
							listener.onClientConnected(NetServer.this, client);
					}
				} catch (Exception e) {
					e.printStackTrace();
					close(e);
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
				for (INetServerListener listener : listeners)
					listener.onOpened(NetServer.this);
			} catch (Exception e) {
				e.printStackTrace();
				close(e);
			}
		}
	}

	private ServerSocket serverSocket = null;
	private int port = -1;
	private Vector<NetServerClient> clients = new Vector<>();
	private boolean opened = false, opening = false;
	private Vector<INetServerListener> listeners = new Vector<>();
	
	public synchronized boolean isOpened() {
		return opened;
	}
	
	public synchronized boolean isOpening() {
		return opening;
	}
	
	public synchronized int getPort() {
		return this.port;
	}
	
	public synchronized void addListener(INetServerListener listener) {
		listeners.add(listener);
	}
	
	public synchronized void removeListener(INetServerListener listener) {
		listeners.remove(listener);
	}
	
	public synchronized void open(int port) {
		if(opened || opening)
			close(new Exception("Network line is already opened!"));
		opening = true;
		this.port = port;
		new ThreadOpen().start();
	}
	
	public synchronized void sendMsgToXY(Msg msg, String id) {
		synchronized (clients) {
			for (int i = 0; i < clients.size(); i++)
				if (clients.get(i).id.equals(id)) {
					clients.get(i).sendMsg(msg);
					if (!clients.get(i).connected)
						clients.remove(i--);
				}
		}
	}
	
	public synchronized void sendMsgToAllExceptXY(Msg msg, String id) {
		synchronized (clients) {
			for (int i = 0; i < clients.size(); i++)
				if (!clients.get(i).id.equals(id)) {
					clients.get(i).sendMsg(msg);
					if (!clients.get(i).connected)
						clients.remove(i--);
				}
		}
	}
	
	public synchronized void sendMsgToAll(Msg msg) {
		synchronized (clients) {
			for (int i = 0; i < clients.size(); i++) {
				clients.get(i).sendMsg(msg);
				if (!clients.get(i).connected)
					clients.remove(i--);
			}
		}
	}
	
	private synchronized void close(Exception e) {
		if(!opened && !opening)
			return;
		for (NetServerClient client : clients)
			client.close(e);
		clients.clear();
		if (serverSocket != null)
			try {
				serverSocket.close();
				serverSocket = null;
			} catch (Exception e1) {
				e1.printStackTrace();
			}
		opened = opening = false;
		for (INetServerListener listener : listeners)
			listener.onClosed(this, e);
	}
	
	public synchronized void close() {
		close(null);
	}
}
