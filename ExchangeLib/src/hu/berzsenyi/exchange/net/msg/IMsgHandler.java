package hu.berzsenyi.exchange.net.msg;

import hu.berzsenyi.exchange.net.TCPConnection;

public interface IMsgHandler {
	public void handleMsg(Msg o, TCPConnection conn);
}
