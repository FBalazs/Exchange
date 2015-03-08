package hu.berzsenyi.exchange.net.cmd;

import hu.berzsenyi.exchange.SingleEvent;

public class CmdServerEvent extends TCPCommand {

	private static final long serialVersionUID = 4667891564777437407L;
	public double[] multipliers;
	public SingleEvent[] newEvents;

	public CmdServerEvent(SingleEvent[] events, double[] multipliers) {
		this.newEvents = new SingleEvent[0];
		this.multipliers = multipliers;
	}
}
