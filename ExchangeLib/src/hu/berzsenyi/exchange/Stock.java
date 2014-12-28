package hu.berzsenyi.exchange;

public class Stock {
	public String name;
	public double value;
	
	public Stock(String name, double value) {
		this.name = name;
		this.value = value;
	}
	
	public int getCmdLength() {
		return 8+4+this.name.length();
	}
}
