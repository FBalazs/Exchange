package hu.berzsenyi.exchange.net;

import hu.berzsenyi.exchange.net.msg.Msg;

import java.io.EOFException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.util.Vector;

public class NetClient {
	private static final String TAG = "["+NetClient.class.getSimpleName()+"] ";
	
	public static interface INetClientListener {
		public void onConnected(NetClient net);
		public void onObjectReceived(NetClient net, Msg msg);
		public void onClosed(NetClient net, Exception e);
	}
	
	private class ThreadReceive extends Thread {
		@Override
		public void run() {
			while(connected)
				try {
					Msg msg = (Msg)oin.readObject();
					System.out.println(TAG+"Received msg: "+msg);
					for(INetClientListener listener : listeners)
						listener.onObjectReceived(NetClient.this, msg);
				} catch(EOFException e) {
					
				} catch(Exception e) {
					e.printStackTrace();
					close(e);
				}
		}
	}
	
	private class ThreadConnect extends Thread {
		@Override
		public void run() {
			try {
				System.out.println(TAG+"Connecting to "+serverAddr);
				socket = new Socket();
				socket.setKeepAlive(true);
				socket.connect(serverAddr, 3000);
				oout = new ObjectOutputStream(socket.getOutputStream());
				oout.flush();
				oin = new ObjectInputStream(socket.getInputStream());
				connected = true;
				connecting = false;
				System.out.println(TAG+"Connected!");
				new ThreadReceive().start();
				for(INetClientListener listener : listeners)
					listener.onConnected(NetClient.this);
			} catch(Exception e) {
				System.out.println(TAG+"Connection failed!");
				e.printStackTrace();
				close(e);
			}
		}
	}
	
	private Socket socket = null;
	private SocketAddress serverAddr = null;
	private ObjectInputStream oin = null;
	private ObjectOutputStream oout = null;
	private boolean connected = false, connecting = false;
	private Vector<INetClientListener> listeners = new Vector<>();
	
	public synchronized void addListener(INetClientListener listener) {
		listeners.add(listener);
	}
	
	public synchronized void removeListener(INetClientListener listener) {
		listeners.remove(listener);
	}
	
	public synchronized boolean isConnected() {
		return connected;
	}
	
	public synchronized boolean isConnecting() {
		return connecting;
	}
	
	public synchronized SocketAddress getServerAddr() {
		return serverAddr;
	}
	
	public synchronized void connect(String host, int port) {
		if(connected || connecting)
			close(new Exception("Network line is already opened!"));
		connecting = true;
		serverAddr = new InetSocketAddress(host, port);
		new ThreadConnect().start();
	}
	
	public synchronized void sendMsg(Msg msg) {
		System.out.println(TAG+"Sending msg: "+msg);
		try {
			oout.writeObject(msg);
			oout.flush();
		} catch(Exception e) {
			e.printStackTrace();
			close(e);
		}
	}
	
	private synchronized void close(Exception e) {
		if(!connected && !connecting)
			return;
		System.out.println(TAG+"Closing...");
		if(oin != null)
			try {
				oin.close();
				oin = null;
			} catch(Exception e1) {
				e1.printStackTrace();
			}
		if(oout != null)
			try {
				oout.close();
				oout = null;
			} catch(Exception e1) {
				e1.printStackTrace();
			}
		if(socket != null)
			try {
				socket.close();
				socket = null;
			} catch(Exception e1) {
				e1.printStackTrace();
			}
		connected = connecting = false;
		for(INetClientListener listener : listeners)
			listener.onClosed(this, e);
	}
	
	public synchronized void close() {
		close(null);
	}
}
