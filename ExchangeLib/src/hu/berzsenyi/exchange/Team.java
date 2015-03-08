package hu.berzsenyi.exchange;

public class Team {
	private final String name, password;
	private String netId;
	private double money;
	private int[] stocks;
	
	public Team(String name, String password, String netId) {
		this.name = name;
		this.password = password;
		this.netId = netId;
		money = 0;
		stocks = null;
	}
	
	public String getName() {
		return name;
	}
	
	public String getPassword() {
		return password;
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
	
	public int[] getStocks() {
		return stocks;
	}
	
	public void setStocks(int[] stocks) {
		this.stocks = stocks;
	}
	
	public int getStock(int stockId) {
		return stocks[stockId];
	}
	
	public void setStock(int stockId, int amount) {
		stocks[stockId] = amount;
	}
}
