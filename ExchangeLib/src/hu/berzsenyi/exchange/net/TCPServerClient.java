package hu.berzsenyi.exchange.net;

import hu.berzsenyi.exchange.net.cmd.ICmdHandler;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.Socket;

public class TCPServerClient extends TCPConnection {
	public IServerClientListener listener = null;
	
	public TCPServerClient(Socket socket, ICmdHandler cmdHandler) {
		super(cmdHandler);
		try {
			this.socket = socket;
			this.din = new DataInputStream(this.socket.getInputStream());
			this.dout = new DataOutputStream(this.socket.getOutputStream());
			onConnect();
		} catch (Exception e) {
			e.printStackTrace();
			this.close();
		}
	}
	
	public void setListener(IServerClientListener listener) {
		this.listener = listener;
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
