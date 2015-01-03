package hu.berzsenyi.exchange;

import hu.berzsenyi.exchange.net.cmd.TCPCommand;

public class Team {
	public String id, name;
	public double money = 0;
	public int[] stocks = null;
	
	public Team(String id, String name) {
		this.id = id;
		this.name = name;
	}
	
	public int getCmdLength() {
		return 4+TCPCommand.stringLength(this.id)+4+TCPCommand.stringLength(this.name);
	}
}
