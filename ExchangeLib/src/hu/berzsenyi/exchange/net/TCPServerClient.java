package hu.berzsenyi.exchange.net;

import hu.berzsenyi.exchange.net.msg.ICmdHandler;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class TCPServerClient extends TCPConnection {
	public IServerClientListener listener = null;

	public TCPServerClient(Socket socket, ICmdHandler cmdHandler) {
		super(cmdHandler);
		try {
			this.socket = socket;
			// The order is important! Deadlock!
			this.oout = new ObjectOutputStream(this.socket.getOutputStream());
			this.oout.flush();
			this.oin = new ObjectInputStream(this.socket.getInputStream());
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
		if (this.listener != null)
			this.listener.onConnect(this);
	}

	@Override
	public void close() {
		super.close();
		if (this.listener != null)
			this.listener.onClose(this);
	}
}
