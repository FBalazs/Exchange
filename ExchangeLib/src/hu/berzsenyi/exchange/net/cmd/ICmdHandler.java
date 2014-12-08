package hu.berzsenyi.exchange.net.cmd;

import hu.berzsenyi.exchange.net.TCPConnection;

public interface ICmdHandler {
	public void handleCmd(TCPCommand cmd, TCPConnection conn);
}
