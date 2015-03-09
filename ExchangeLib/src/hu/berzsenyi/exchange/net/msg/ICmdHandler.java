package hu.berzsenyi.exchange.net.msg;

import hu.berzsenyi.exchange.net.TCPConnection;

public interface ICmdHandler {
	public void handleCmd(Object o, TCPConnection conn);
}
