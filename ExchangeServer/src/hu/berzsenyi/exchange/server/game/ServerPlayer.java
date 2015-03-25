package hu.berzsenyi.exchange.server.game;

public class ServerPlayer {
	public final String name, password;
	private String netId;
	private double money;
	private int[] stocks;
	
	public ServerPlayer(String name, String password, String netId) {
		this.name = name;
		this.password = password;
		this.netId = netId;
		money = ServerExchange.INSTANCE.getStartMoney();
		stocks = new int[ServerExchange.INSTANCE.getStockNumber()];
	}
	
	public String getNetId() {
		return netId;
	}
	
	public void setNetId(String id) {
		netId = id;
	}
	
	public double getMoney() {
		return money;
	}
	
	public void setMoney(double money) {
		this.money = money;
	}
	
	public int getStockAmount(int stockId) {
		return stocks[stockId];
	}
	
	public void setStockAmount(int stockId, int amount) {
		stocks[stockId] = amount;
	}
	
	public int[] getStocks() {
		return stocks;
	}
	
	public void setStocks(int[] stocks) {
		this.stocks = stocks;
	}
	
	public double getStocksValue() {
		double ret = 0;
		for(int i = 0; i < stocks.length; i++)
			ret += stocks[i]*ServerExchange.INSTANCE.getStock(i).getPrice();
		return ret;
	}
}
