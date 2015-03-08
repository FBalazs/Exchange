package hu.berzsenyi.exchange.net;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.util.ArrayList;
import java.util.List;

public class NetClient {
	public static interface INetClientListener {
		public void onConnected(NetClient net);
		public void onObjectReceived(Object o);
		public void onClosed(NetClient net);
	}
	
	private class ThreadReceive extends Thread {
		@Override
		public void run() {
			while(connected)
				try {
					if(0 < oin.available()) {
						Object o = oin.readObject();
						for(INetClientListener listener : listeners)
							listener.onObjectReceived(o);
					}
				} catch(Exception e) {
					e.printStackTrace();
					close();
				}
		}
	}
	
	private class ThreadConnect extends Thread {
		@Override
		public void run() {
			try {
				socket = new Socket();
				socket.connect(serverAddr);
				oin = new ObjectInputStream(socket.getInputStream());
				oout = new ObjectOutputStream(socket.getOutputStream());
				connected = true;
				connecting = false;
				new ThreadReceive().start();
				for(INetClientListener listener : listeners)
					listener.onConnected(NetClient.this);
			} catch(Exception e) {
				e.printStackTrace();
				close();
			}
		}
	}
	
	private Socket socket = null;
	private SocketAddress serverAddr = null;
	private ObjectInputStream oin = null;
	private ObjectOutputStream oout = null;
	private boolean connected = false, connecting = false;
	private List<INetClientListener> listeners = new ArrayList<>();
	
	public void addListener(INetClientListener listener) {
		listeners.add(listener);
	}
	
	public void removeListener(INetClientListener listener) {
		listeners.remove(listener);
	}
	
	public boolean isConnected() {
		return connected;
	}
	
	public boolean isConnecting() {
		return connecting;
	}
	
	public SocketAddress getServerAddr() {
		return serverAddr;
	}
	
	public void connect(String host, int port) {
		if(connected || connecting)
			close();
		connecting = true;
		serverAddr = new InetSocketAddress(host, port);
		new ThreadConnect().start();
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
		if(!connected && !connecting)
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
		connected = connecting = false;
		for(INetClientListener listener : listeners)
			listener.onClosed(this);
	}
}
