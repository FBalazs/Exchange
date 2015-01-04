package hu.berzsenyi.exchange.log;

public class LogEventDisconnect extends LogEvent {
	public LogEventDisconnect(String id, String name) {
		super("Client disconnecting", "Client disconnecting with the id "+id+" and nickname "+name);
	}
}
