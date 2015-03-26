package hu.berzsenyi.exchange.net.msg;

public class MsgServerEvents extends Msg {
	private static final long serialVersionUID = 3961672462855871724L;
	
	public String[] events;
	
	public MsgServerEvents(String[] events) {
		this.events = events;
	}
}
