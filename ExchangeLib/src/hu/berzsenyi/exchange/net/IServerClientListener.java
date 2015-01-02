package hu.berzsenyi.exchange.net;

public interface IServerClientListener {
	public void onConnect(TCPServerClient client);
	public void onClose(TCPServerClient client);
}
