package hu.berzsenyi.exchange;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class Event {

	private static final String BAD_PROBABILITY = "Event probability must be positive",
			BAD_DESCRIPTION = "Event description must not be null",
			BAD_NEXT_EVENTS = "Event nextEvents must not be null";

	private static Random random = new Random();

	private int mProbability;
	private String mDescription;
	private Event mNextEvent;
	private double[] mMultipliers;

	private Event(int probability, String description, double[] multipliers,
			Event[] nextEvents) {
		if (probability <= 0)
			throw new IllegalArgumentException(BAD_PROBABILITY);
		if (description == null)
			throw new NullPointerException(BAD_DESCRIPTION);
		if (nextEvents == null)
			throw new NullPointerException(BAD_NEXT_EVENTS);
		mProbability = probability;
		mDescription = description;
		mMultipliers = multipliers;
		mNextEvent = drawNextEvent(nextEvents);
	}

	public Event getNextEvent() {
		return mNextEvent;
	}

	public String getDescription() {
		return mDescription;
	}

	public double getMultiplier(int pos) {
		return mMultipliers[pos];
	}

	/**
	 * Get all multiplier in a double[]. Prefer {@link #getMultiplier(int)} if
	 * you need a single multiplier, to increase efficiency.
	 * 
	 * @return
	 */
	public double[] getMultipliers() {
		return mMultipliers.clone();
	}

	private static Event drawNextEvent(Event[] nextEvents) {
		if (nextEvents.length == 0)
			return null;
		int probabilitySum = 0;
		for (Event event : nextEvents)
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
		private Model mModel;

		public Builder(Model model) {
			mModel = model;
			mMultipliers = new double[mModel.stocks.length];
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
				for (; stockIndex < mModel.stocks.length; stockIndex++)
					if (mModel.stocks[stockIndex].id.equals(stockName))
						break;
				if (stockIndex < mModel.stocks.length)
					mMultipliers[stockIndex] = multiplier;
			}
			return this;
		}

		public Builder addNextEventBuilder(Builder builder) {
			mNextEventBuilders.add(builder);
			return this;
		}

		public Event create() {
			Event[] nextEvents = new Event[mNextEventBuilders.size()];
			for (int i = 0; i < nextEvents.length; i++)
				nextEvents[i] = mNextEventBuilders.get(i).create();
			return new Event(mProbability, mDescription, mMultipliers,
					nextEvents);
		}
	}

}
