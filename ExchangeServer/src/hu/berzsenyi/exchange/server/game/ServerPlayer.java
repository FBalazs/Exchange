package hu.berzsenyi.exchange.server.game;

public class ServerPlayer {
	public String name, password, netId;
	public double money;
	public int[] stocks;
	
	public ServerPlayer(String name, String password, String netId) {
		this.name = name;
		this.password = password;
		this.netId = netId;
		money = ExchangeServer.INSTANCE.startMoney;
		stocks = new int[ExchangeServer.INSTANCE.stocks.length];
	}
}
