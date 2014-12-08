package hu.berzsenyi.exchange.client;

import hu.berzsenyi.exchange.net.TCPConnection;
import hu.berzsenyi.exchange.net.cmd.CmdServerInfo;
import hu.berzsenyi.exchange.net.cmd.ICmdHandler;
import hu.berzsenyi.exchange.net.cmd.TCPCommand;
import android.util.Log;

public class CmdHandlerClient implements ICmdHandler {
	public ActivityMain client;
	
	public CmdHandlerClient(ActivityMain client) {
		this.client = client;
	}
	
	@Override
	public void handleCmd(TCPCommand cmd, TCPConnection conn) {
		Log.d(this.getClass().getName(), "Received command! "+cmd.getClass().getName());
		
		if(cmd instanceof CmdServerInfo) {
			this.client.model = ((CmdServerInfo)cmd).model;
			return;
		}
	}
}
