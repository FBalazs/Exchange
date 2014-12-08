package hu.berzsenyi.exchange;

public class Team {
	public String id, name;
	public double money;
	public int[] stocks;
	
	public Team(String id, String name, double money, int stockNumber) {
		this.id = id;
		this.name = name;
		this.money = money;
		this.stocks = new int[stockNumber];
	}
}
