package hu.berzsenyi.exchange.net;

import hu.berzsenyi.exchange.net.cmd.ICmdHandler;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.Socket;

public class TCPServerClient extends TCPConnection {
	public TCPServerClient(Socket socket, ICmdHandler cmdHandler) {
		super(cmdHandler);
		try {
			this.socket = socket;
			this.din = new DataInputStream(this.socket.getInputStream());
			this.dout = new DataOutputStream(this.socket.getOutputStream());
			new TCPReceiveThread(this).start();
		} catch (Exception e) {
			e.printStackTrace();
			this.close();
		}
	}
}