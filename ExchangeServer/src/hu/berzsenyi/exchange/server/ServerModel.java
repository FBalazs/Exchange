package hu.berzsenyi.exchange.server;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.xmlpull.v1.XmlPullParserException;

import hu.berzsenyi.exchange.EventQueue;
import hu.berzsenyi.exchange.Model;

public class ServerModel extends Model {

	public List<EventQueue> currentEvents = new ArrayList<EventQueue>();

	public EventQueue[] allEvents;
	public int round = 0;

	public void loadEvents(File file) {
		try {
			allEvents = EventParser.parseEvents(this, new InputStreamReader(
					new FileInputStream(file), "UTF-8"));
		} catch (XmlPullParserException | IOException e) {
			e.printStackTrace();
		}
	}
}
