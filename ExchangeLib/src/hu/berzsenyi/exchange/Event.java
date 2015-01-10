package hu.berzsenyi.exchange;

public class Event {
	public String id, desc;
	public int howmany;
	public double[] multipliers;
	
	public Event(String id, String desc, int howmany) {
		this.id = id;
		this.desc = desc;
		this.howmany = howmany;
	}
}
