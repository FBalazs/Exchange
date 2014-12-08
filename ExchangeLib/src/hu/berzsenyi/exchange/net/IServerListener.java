package hu.berzsenyi.exchange.net;

public interface IServerListener {
	public void onClientConnected(TCPServerClient client);
	public void onClientDisconnected(TCPServerClient client);
}
