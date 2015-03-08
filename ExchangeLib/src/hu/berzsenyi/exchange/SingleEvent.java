package hu.berzsenyi.exchange;

import java.io.Serializable;

public class SingleEvent extends Event implements Serializable {

	private static final long serialVersionUID = -740939041210748516L;

	public SingleEvent(String description, double[] multipliers) {
		super(description, multipliers);
	}

}
