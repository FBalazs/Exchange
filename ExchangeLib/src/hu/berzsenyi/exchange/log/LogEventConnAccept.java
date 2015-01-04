package hu.berzsenyi.exchange.log;

public class LogEventConnAccept extends LogEvent {
	public LogEventConnAccept(String id, String name) {
		super("Connection accepted", "Connection accepted from "+id+" with the nickname "+name);
	}
}
