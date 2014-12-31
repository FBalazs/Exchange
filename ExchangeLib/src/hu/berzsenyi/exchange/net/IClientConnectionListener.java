package hu.berzsenyi.exchange.net;

import java.io.IOException;

public interface IClientConnectionListener {
	public void onConnect(TCPClient client);
	public void onClose(TCPClient client);
	public void onConnectionFail(TCPClient client, IOException exception);
}
