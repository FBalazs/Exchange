package hu.berzsenyi.exchange.log;

public class LogEvent {
	public long time;
	public String title, desc;
	
	public LogEvent(String title, String desc) {
		this.time = System.currentTimeMillis();
		this.title = title;
		this.desc = desc;
	}
}
