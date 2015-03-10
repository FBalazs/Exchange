package hu.berzsenyi.exchange.net;

import hu.berzsenyi.exchange.net.msg.ICmdHandler;
import hu.berzsenyi.exchange.net.msg.Msg;

import java.io.*;
import java.net.Socket;
import java.net.SocketTimeoutException;

public abstract class TCPConnection {

	private class TCPReceiveThread extends Thread {

		public TCPReceiveThread() {
			super("Thread-TCPReceive");
		}

		@Override
		public void run() {
			System.out.println("TCPReceiveThread started");
			while (TCPConnection.this.open) {
				try {
					Msg o = (Msg) oin.readObject();

					cmdHandler.handleCmd(o, TCPConnection.this);
					// System.out.println("A command has arrived");
				} catch (EOFException e) { // Nothing special
				} catch (SocketTimeoutException e) {
				} catch (Exception e) {
					e.printStackTrace();
					TCPConnection.this.close();
				}
			}
			System.out.println("TCPReceiveThread stopped");
		}
	}

	public Socket socket;
	public boolean open = false;
	protected ObjectInputStream oin;
	protected ObjectOutputStream oout;
	public ICmdHandler cmdHandler;

	public TCPConnection(ICmdHandler cmdHandler) {
		this.cmdHandler = cmdHandler;
	}

	public String getAddrString() {
		return this.socket.getInetAddress().toString() + ":"
				+ this.socket.getPort();
	}

	public void writeCommand(Object o) {
		try {
			this.oout.writeObject(o);
			this.oout.flush();
		} catch (Exception e) {
			e.printStackTrace();
			this.close();
		}
	}

	protected void onConnect() {
		System.out.println("TCPConnection.onConnect()");
		this.open = true;
		new TCPReceiveThread().start();
	}

	public void close() {
		if (!this.open)
			return;
		this.open = false;
		try {
			if (this.oin != null)
				this.oin.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		try {
			if (this.oout != null)
				this.oout.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		try {
			if (this.socket != null)
				this.socket.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
