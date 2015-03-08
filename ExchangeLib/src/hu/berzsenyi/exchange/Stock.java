package hu.berzsenyi.exchange;

import java.io.Serializable;
import java.util.List;

public class Stock implements Serializable {
	private static final long serialVersionUID = -7698800698738451407L;
	public String id, name;
	public double value;
	/**
	 * <code>value/last value</code>
	 */
	public double change = 1;
	public double boughtFor;
	public int boughtAmount;
	// TODO kupac
	public List<Offer> buyOffers, saleOffers;

	public Stock(String id, String name, double value) {
		this.id = id;
		this.name = name;
		this.value = value;
	}

}
