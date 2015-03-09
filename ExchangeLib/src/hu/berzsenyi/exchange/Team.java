package hu.berzsenyi.exchange;

public class Team {
	public final String name, password;
	public String netId;
	public double money;
	public int[] stocks;
	
	public Team(String name, String password, String netId) {
		this.name = name;
		this.password = password;
		this.netId = netId;
		money = 0;
		stocks = null;
	}
}
