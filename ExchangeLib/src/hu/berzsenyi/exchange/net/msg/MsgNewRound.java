package hu.berzsenyi.exchange.net.msg;

import hu.berzsenyi.exchange.SingleEvent;

public class MsgNewRound extends Msg {

	private static final long serialVersionUID = 6969433135725677396L;
	
	public SingleEvent[] events;
	
	public MsgNewRound(SingleEvent[] events) {
		this.events = events;
	}

}
