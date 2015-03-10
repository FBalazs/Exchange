package hu.berzsenyi.exchange.net.msg;

import hu.berzsenyi.exchange.net.TCPConnection;

public interface ICmdHandler {
	public void handleCmd(Msg o, TCPConnection conn);
}
