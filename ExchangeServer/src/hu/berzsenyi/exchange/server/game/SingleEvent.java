package hu.berzsenyi.exchange.server.game;

import hu.berzsenyi.exchange.game.Event;


public class SingleEvent extends Event {

	private static final long serialVersionUID = -740939041210748516L;

	public SingleEvent(String description, double[] multipliers) {
		super(description, multipliers);
	}

}
