package hu.berzsenyi.exchange.log;

public class LogEventConnAttempt extends LogEvent {
	public LogEventConnAttempt(String id) {
		super("Connection attempt", "Connection attempt from "+id);
	}
}
