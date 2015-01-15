package hu.berzsenyi.exchange.net;

import hu.berzsenyi.exchange.net.cmd.ICmdHandler;

import java.io.DataInputStream;
import java.io.DataOutputStream;
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
			this.din = new DataInputStream(this.socket.getInputStream());
			this.dout = new DataOutputStream(this.socket.getOutputStream());
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
