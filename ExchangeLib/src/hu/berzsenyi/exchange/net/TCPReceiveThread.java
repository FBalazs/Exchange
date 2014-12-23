package hu.berzsenyi.exchange.net;

import hu.berzsenyi.exchange.net.cmd.*;

public class TCPReceiveThread extends Thread {
	public TCPConnection connection;
	
	public TCPReceiveThread(TCPConnection connection) {
		super("Thread-TCPReceive");
		this.connection = connection;
	}
	
	@Override
	public void run() {
		while(this.connection.open) {
			try {
				while(this.connection.din.available() < 4+4)
					Thread.sleep(10);
				int id = this.connection.din.readInt();
				int length = this.connection.din.readInt();
				while(this.connection.din.available() < length);
				TCPCommand cmd = null;
				switch(id) {
				case CmdClientInfo.ID:
					cmd = new CmdClientInfo(length);
					break;
				case CmdClientDisconnect.ID:
					cmd = new CmdClientDisconnect(length);
					break;
				case CmdServerInfo.ID:
					cmd = new CmdServerInfo(length);
					break;
				case CmdOffer.ID:
					cmd = new CmdOffer(length);
					break;
				case CmdClientOfferResponse.ID:
					cmd = new CmdClientOfferResponse(length);
					break;
				case CmdServerExchange.ID:
					cmd = new CmdServerExchange(length);
					break;
				case CmdClientBuy.ID:
					cmd = new CmdClientBuy(length);
					break;
				}
				if(cmd != null)
					cmd.read(this.connection.din);
				synchronized (this.connection.commandList) {
					this.connection.commandList.add(cmd);
				}
			} catch(Exception e) {
				e.printStackTrace();
				this.connection.close();
			}
		}
	}
}
