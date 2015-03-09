package hu.berzsenyi.exchange.net;

import hu.berzsenyi.exchange.net.msg.ICmdHandler;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.io.IOException;

public class TCPClient extends TCPConnection {
	public IClientConnectionListener listener;
	
	public TCPClient(String host, int port, ICmdHandler cmdHandler, IClientConnectionListener listener) throws IOException {
		super(cmdHandler);
		try {
			this.listener = listener;
			this.socket = new Socket();
			this.socket.setSoTimeout(2000);
			this.socket.connect(new InetSocketAddress(host, port),2000);
			// The order is important! Deadlock!
			this.oout = new ObjectOutputStream(this.socket.getOutputStream());
			this.oout.flush();
			this.oin = new ObjectInputStream(this.socket.getInputStream());
			onConnect();
		} catch (IOException e) {
			if(this.listener != null)
				this.listener.onConnectionFail(this, e);
			this.close();
			throw e;
		}
	}

	@Override
	protected void onConnect() {
		super.onConnect();
		if(this.listener != null)
			this.listener.onConnect(this);
	}
	
	
	
	@Override
	public void close() {
		super.close();
		if(this.listener != null)
			this.listener.onClose(this);
	}
}
