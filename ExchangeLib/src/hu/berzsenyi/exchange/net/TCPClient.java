package hu.berzsenyi.exchange.net;

import hu.berzsenyi.exchange.net.cmd.ICmdHandler;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;

public class TCPClient extends TCPConnection {
	public IClientListener listener;
	
	public TCPClient(String host, int port, ICmdHandler cmdHandler, IClientListener listener) {
		super(cmdHandler);
		try {
			this.listener = listener;
			this.socket = new Socket();
			this.socket.setSoTimeout(1000);
			this.socket.connect(new InetSocketAddress(host, port));
			this.din = new DataInputStream(this.socket.getInputStream());
			this.dout = new DataOutputStream(this.socket.getOutputStream());
			new TCPReceiveThread(this).start();
			if(this.listener != null)
				this.listener.onConnect(this);
		} catch (Exception e) {
			e.printStackTrace();
			this.close();
		}
	}
	
	@Override
	public void close() {
		super.close();
		if(this.listener != null)
			this.listener.onClose(this);
	}
}
