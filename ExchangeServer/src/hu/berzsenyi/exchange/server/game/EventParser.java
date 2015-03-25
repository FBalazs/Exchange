package hu.berzsenyi.exchange.server.game;

import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

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

	public static EventQueue[] parseEvents(ServerExchange exchange, Reader in)
			throws XmlPullParserException, IOException {
		XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
		XmlPullParser parser = factory.newPullParser();
		parser.setInput(in);

		int eventType = parser.getEventType();
		boolean firstTagFound = false, inEventQueue = false;
		int versionCode = -1;
		List<EventQueue> out = new ArrayList<EventQueue>();
		Stack<EventQueue.Builder> stack = new Stack<EventQueue.Builder>();

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
						EventQueue.Builder builder = new EventQueue.Builder(exchange);
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
				if (parser.getName().equals(TAG_EVENT)) {
					if (stack.size() > 1)
						stack.pop();
					else
						out.add(stack.pop().create());
				} else if (parser.getName().equals(TAG_EVENT_QUEUE))
					inEventQueue = false;
				break;
			}
			eventType = parser.next();
		}

		return out.toArray(new EventQueue[out.size()]);
	}

	public static boolean isVersionSupported(int versionCode) {
		return versionCode == 1;
	}

}