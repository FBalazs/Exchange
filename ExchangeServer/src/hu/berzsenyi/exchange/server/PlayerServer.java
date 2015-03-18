package hu.berzsenyi.exchange.server;

public class PlayerServer {
	public String name, netId;
	public double money;
	public int[] stocks;
	
	public PlayerServer(String name, String netId) {
		this.name = name;
		this.netId = netId;
		money = ExchangeServer.INSTANCE.startMoney;
		stocks = new int[ExchangeServer.INSTANCE.stocks.length];
	}
}
