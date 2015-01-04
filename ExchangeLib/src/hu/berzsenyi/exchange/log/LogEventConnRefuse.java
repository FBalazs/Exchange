package hu.berzsenyi.exchange.log;

public class LogEventConnRefuse extends LogEvent {
	public LogEventConnRefuse(String id, String name) {
		super("Connection refused", "Connection refused from "+id+" with the nickname "+name);
	}
}
