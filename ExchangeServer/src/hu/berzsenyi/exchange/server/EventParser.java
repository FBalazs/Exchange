package hu.berzsenyi.exchange.server;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import hu.berzsenyi.exchange.Event;
import hu.berzsenyi.exchange.Model;
import hu.berzsenyi.exchange.Stock;

public class EventParser {

	private static final String TAG_ROOT = "EventQueues", TAG_EVENT = "Event",
			TAG_EVENT_QUEUE = "EventQueue";
	private static final String ATTRIBUTE_VERSION_CODE = "versionCode",
			ATTRIBUTE_DESCRIPTION = "description",
			ATTRIBUTE_MULTIPLIERS = "multipliers",
			ATTRIBUTE_PROBABILITY = "probability";

	private static final String ROOT_TAG_EXPECTED = "Root tag expected at ",
			VERSION_CODE_EXPECTED = "Version code expected at ",
			EVENT_TAG_EXPECTED = "Event tag expected at ",
			EVENT_QUEUE_TAG_EXPECTED = "EventQueue tag expected at ",
			UNSUPPORTED_VERSION = "Unsupported version";

	public static Event[] parseEvents(Model model, Reader in)
			throws XmlPullParserException, IOException {
		XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
		XmlPullParser parser = factory.newPullParser();
		parser.setInput(in);

		int eventType = parser.getEventType();
		boolean firstTagFound = false, inEventQueue = false;
		int versionCode = -1;
		List<Event> out = new ArrayList<Event>();
		Stack<Event.Builder> stack = new Stack<Event.Builder>();

		while (eventType != XmlPullParser.END_DOCUMENT) {
			switch (eventType) {

			case XmlPullParser.START_TAG:

				if (firstTagFound) {
					if (inEventQueue) {
						if (!parser.getName().equals(TAG_EVENT))
							throw new IllegalArgumentException(
									EVENT_TAG_EXPECTED + parser.getLineNumber()
											+ ":" + parser.getColumnNumber());
						// tag name is now bound to be Event
						Event.Builder builder = new Event.Builder(model);
						int attributeCount = parser.getAttributeCount();
						for (int i = 0; i < attributeCount; i++) {
							String attributeName = parser.getAttributeName(i), attributeValue = parser
									.getAttributeValue(i);
							if (attributeName.equals(ATTRIBUTE_DESCRIPTION))
								builder.setDescription(attributeValue);
							else if (attributeName
									.equals(ATTRIBUTE_MULTIPLIERS))
								builder.setMultipliers(attributeValue);
							else if (attributeName
									.equals(ATTRIBUTE_PROBABILITY))
								builder.setProbability(Integer
										.parseInt(attributeValue));
						}
						if (!stack.isEmpty())
							stack.peek().addNextEventBuilder(builder);
						stack.push(builder);

					} else {
						if (!parser.getName().equals(TAG_EVENT_QUEUE))
							throw new IllegalArgumentException(
									EVENT_QUEUE_TAG_EXPECTED
											+ parser.getLineNumber() + ":"
											+ parser.getColumnNumber());
						inEventQueue = true;
					}

				} else {
					firstTagFound = true;
					if (!parser.getName().equals(TAG_ROOT))
						throw new IllegalArgumentException(ROOT_TAG_EXPECTED
								+ parser.getLineNumber() + ":"
								+ parser.getColumnNumber());

					int attributeCount = parser.getAttributeCount();
					for (int i = 0; i < attributeCount; i++)
						if (parser.getAttributeName(i).equals(
								ATTRIBUTE_VERSION_CODE))
							versionCode = Integer.parseInt(parser
									.getAttributeValue(i));

					if (versionCode < 0)
						throw new IllegalArgumentException(
								VERSION_CODE_EXPECTED + parser.getLineNumber()
										+ ":" + parser.getColumnNumber());

					if (!isVersionSupported(versionCode))
						throw new IllegalArgumentException(UNSUPPORTED_VERSION);
				}
				break;
			case XmlPullParser.END_TAG:
				if (parser.getName() == TAG_EVENT)
					stack.pop();
				else if (parser.getName() == TAG_EVENT_QUEUE)
					out.add(stack.peek().create());
				break;
			}
			eventType = parser.next();
		}

		return out.toArray(new Event[out.size()]);
	}

	public static boolean isVersionSupported(int versionCode) {
		return versionCode == 1;
	}

	public static void main(String[] args) {
		Model model = new Model();
		model.stocks = new Stock[] { new Stock("food", "Kaja", 120.0),
				new Stock("oil", "Olaj", 90.21),
				new Stock("media", "Média", 12.0) };
		try {
			Event[] events = parseEvents(
					model,
					new StringReader(
							"<?xml version=\"1.0\" encoding=\"utf-8\"?><EventQueues versionCode=\"1\" >    <!-- No need for id, you can identify them by their order -->    <EventQueue>        <Event            description=\"Esemény1\"            multipliers=\"food:1.1;medical:0.98;\" >            <Event                description=\"Esemény2\"                multipliers=\"hotel:0.4;\" >                <Event                    description=\"Esemény3.1\"                    multipliers=\"media:1.20002;contech:3.01;\"                    probability=\"2\" >                    <Event description=\"Esemény4.1\" />                    <Event                        description=\"Esemény3.2\"                        multipliers=\"media:0.9;\"                        probability=\"5\" >                        <!-- probability is 1 by default -->                        <Event                            description=\"Esemény4.2\"                            multipliers=\"oil:2;\" />                        <Event                            description=\"Esemény4.3\"                            multipliers=\"oil:1.01;\"                            probability=\"3\" />                    </Event>                </Event>            </Event>        </Event>    </EventQueue>"));
			System.out.println("Hello");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
