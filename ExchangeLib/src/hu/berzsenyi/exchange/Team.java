package hu.berzsenyi.exchange;

public class Team {
	public String id, name;
	public double money = 0;
	public int[] stocks = null;
	
	public Team(String id, String name) {
		this.id = id;
		this.name = name;
	}
	
	public int getCmdLength() {
		return 4+this.id.length()+4+this.name.length()+8+4+4*this.stocks.length;
	}
}
