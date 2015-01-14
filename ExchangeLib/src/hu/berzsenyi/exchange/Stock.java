package hu.berzsenyi.exchange;

import hu.berzsenyi.exchange.net.cmd.TCPCommand;

public class Stock {
	public String id, name;
	public double value;
	/**
	 * <code>value/last value</code>
	 */
	public double change = 1;
	public double boughtFor;
	public int boughtAmount;
	
	public Stock(String id, String name, double value) {
		this.id = id;
		this.name = name;
		this.value = value;
	}
	
	public int getCmdLength() {
		return 8+4+TCPCommand.stringLength(this.id)+4+TCPCommand.stringLength(this.name);
	}
}
