package hu.berzsenyi.exchange.net;

public interface IClientListener {
	public void onConnect(TCPClient client);
	public void onClose(TCPClient client);
}
