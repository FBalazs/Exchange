package hu.berzsenyi.exchange;

public abstract class Event {

	private static final String
			BAD_DESCRIPTION = "Event description must not be null";

	private String mDescription;
	private double[] mMultipliers;

	public Event(String description, double[] multipliers) {
		if (description == null)
			throw new NullPointerException(BAD_DESCRIPTION);
		mDescription = description;
		mMultipliers = multipliers.clone();
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

}
