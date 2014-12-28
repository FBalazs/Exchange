package hu.berzsenyi.exchange.net;

import hu.berzsenyi.exchange.net.cmd.*;

import java.io.*;
import java.net.Socket;

public abstract class TCPConnection {
	
	private class TCPReceiveThread extends Thread {

		public TCPReceiveThread() {
			super("Thread-TCPReceive");
		}

		@Override
		public void run() {
			System.out.println("TCPReceiveThread started");
			while(TCPConnection.this.open) {
				try {
					while(TCPConnection.this.open && TCPConnection.this.din.available() < 4+4)
						Thread.sleep(10);
					int id = TCPConnection.this.din.readInt();
					int length = TCPConnection.this.din.readInt();
					while(TCPConnection.this.din.available() < length);
					TCPCommand cmd = null;
					switch(id) {
					case CmdClientInfo.ID:
						cmd = new CmdClientInfo(length);
						break;
					case CmdClientDisconnect.ID:
						cmd = new CmdClientDisconnect(length);
						break;
					case CmdServerStocks.ID:
						cmd = new CmdServerStocks(length);
						break;
					case CmdServerTeams.ID:
						cmd = new CmdServerTeams(length);
						break;
					case CmdOffer.ID:
						cmd = new CmdOffer(length);
						break;
					case CmdOfferResponse.ID:
						cmd = new CmdOfferResponse(length);
						break;
					case CmdClientBuy.ID:
						cmd = new CmdClientBuy(length);
						break;
					}
					if(cmd != null)
						cmd.read(TCPConnection.this.din);
					
					cmdHandler.handleCmd(cmd, TCPConnection.this);
					System.out.println("A command has arrived");
				} catch(Exception e) {
					e.printStackTrace();
					TCPConnection.this.close();
				}
			}
			System.out.println("TCPReceiveThread stopped");
		}
	}
	
	
	public Socket socket;
	public boolean open = false;
	public DataInputStream din;
	public DataOutputStream dout;
	public ICmdHandler cmdHandler;
	
	public TCPConnection(ICmdHandler cmdHandler) {
		this.cmdHandler = cmdHandler;
	}
	
	public String getAddrString() {
		return this.socket.getInetAddress().toString()+":"+this.socket.getPort();
	}
	
	public void writeCommand(TCPCommand cmd) {
		try {
			this.dout.writeInt(cmd.id);
			this.dout.writeInt(cmd.length);
			cmd.write(this.dout);
		} catch(Exception e) {
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
		if(!this.open)
			return;
		this.open = false;
		try {
			if(this.din != null)
				this.din.close();
		} catch(Exception e) {
			e.printStackTrace();
		}
		try {
			if(this.dout != null)
				this.dout.close();
		} catch(Exception e) {
			e.printStackTrace();
		}
		try {
			if(this.socket != null)
				this.socket.close();
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
}
