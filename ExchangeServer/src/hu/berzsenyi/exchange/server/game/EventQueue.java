package hu.berzsenyi.exchange.server.game;

import hu.berzsenyi.exchange.game.Event;
import hu.berzsenyi.exchange.game.Exchange;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class EventQueue extends Event {

	private static final long serialVersionUID = -3571002757803563472L;

	private static final String BAD_PROBABILITY = "Event probability must be positive",
			BAD_NEXT_EVENTS = "Event nextEvents must not be null";

	private static Random random = new Random();

	private EventQueue mNextEvent;
	private int mProbability;

	private EventQueue(int probability, String description,
			double[] multipliers, EventQueue[] nextEvents) {
		super(description, multipliers);
		if (probability <= 0)
			throw new IllegalArgumentException(BAD_PROBABILITY);
		if (nextEvents == null)
			throw new NullPointerException(BAD_NEXT_EVENTS);
		mProbability = probability;
		mNextEvent = drawNextEvent(nextEvents);
	}

	public SingleEvent getSingleEvent() {
		return new SingleEvent(getDescription(), getMultipliers());
	}

	public EventQueue getNextEvent() {
		return mNextEvent;
	}

	private static EventQueue drawNextEvent(EventQueue[] nextEvents) {
		if (nextEvents.length == 0)
			return null;
		int probabilitySum = 0;
		for (EventQueue event : nextEvents)
			probabilitySum += event.mProbability;
		int randomNumber = random.nextInt(probabilitySum);
		int i = 0;
		while (randomNumber >= 0 && i < nextEvents.length)
			randomNumber -= nextEvents[i++].mProbability;
		return nextEvents[i - 1];

	}

	public static class Builder {

		private int mProbability = 1;
		private String mDescription;
		private double[] mMultipliers;
		private List<Builder> mNextEventBuilders = new ArrayList<Builder>();
		private Exchange mExchange;

		public Builder(Exchange exchange) {
			if (exchange == null)
				throw new NullPointerException();
			mExchange = exchange;
			mMultipliers = new double[mExchange.getStockCount()];
			Arrays.fill(mMultipliers, 1.0);
		}

		public Builder setProbability(int probability) {
			mProbability = probability;
			return this;
		}

		public Builder setDescription(String description) {
			mDescription = description;
			return this;
		}

		public Builder setMultipliers(double[] multipliers) {
			if (multipliers.length != mMultipliers.length)
				throw new IllegalArgumentException("Bad length");
			for (int i = 0; i < mMultipliers.length; i++)
				mMultipliers[i] = multipliers[i];
			return this;
		}

		public Builder setMultipliers(String multipliersAsString) {
			Arrays.fill(mMultipliers, 1.0);
			String[] splits = multipliersAsString.split(";");
			for (String split : splits) {
				String[] splittedSplit = split.split(":");
				String stockName = splittedSplit[0].trim();
				double multiplier = Double.parseDouble(splittedSplit[1].trim());

				int stockIndex = 0;
				for (; stockIndex < mExchange.getStockCount(); stockIndex++)
					if (mExchange.getStock(stockIndex).name.equals(stockName))
						break;
				if (stockIndex < mExchange.getStockCount())
					mMultipliers[stockIndex] = multiplier;
			}
			return this;
		}

		public Builder addNextEventBuilder(Builder builder) {
			mNextEventBuilders.add(builder);
			return this;
		}

		public EventQueue create() {
			EventQueue[] nextEvents = new EventQueue[mNextEventBuilders.size()];
			for (int i = 0; i < nextEvents.length; i++)
				nextEvents[i] = mNextEventBuilders.get(i).create();
			return new EventQueue(mProbability, mDescription, mMultipliers,
					nextEvents);
		}
	}

}
