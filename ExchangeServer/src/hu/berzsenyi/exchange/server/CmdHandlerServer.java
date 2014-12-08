package hu.berzsenyi.exchange.server;

import hu.berzsenyi.exchange.net.TCPConnection;
import hu.berzsenyi.exchange.net.cmd.CmdClientDisconnect;
import hu.berzsenyi.exchange.net.cmd.CmdClientInfo;
import hu.berzsenyi.exchange.net.cmd.CmdServerInfo;
import hu.berzsenyi.exchange.net.cmd.ICmdHandler;
import hu.berzsenyi.exchange.net.cmd.TCPCommand;

public class CmdHandlerServer implements ICmdHandler {
	public ExchangeServer server;
	
	public CmdHandlerServer(ExchangeServer server) {
		this.server = server;
	}
	
	@Override
	public void handleCmd(TCPCommand cmd, TCPConnection conn) {
		System.out.println("Received command! "+cmd.getClass().getName());
		
		if(cmd instanceof CmdClientInfo) {
			if(this.server.model.round == 0) {
				this.server.model.newTeam(conn.getAddrString(), ((CmdClientInfo)cmd).name);
				conn.writeCommand(new CmdServerInfo(this.server.model));
			} else {
				// TODO send feedback and disconnect client
			}
			return;
		}
		
		if(cmd instanceof CmdClientDisconnect) {
			this.server.model.removeTeam(conn.socket.getInetAddress().toString()+":"+conn.socket.getPort());
			conn.close();
			return;
		}
	}
}
