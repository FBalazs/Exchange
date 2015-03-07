package hu.berzsenyi.exchange.server;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;

import org.xmlpull.v1.XmlPullParserException;

import hu.berzsenyi.exchange.Model;

public class ServerModel extends Model {

	public void loadEvents(File file) {
		try {
			events = EventParser.parseEvents(this, new InputStreamReader(
					new FileInputStream(file), "UTF-8"));
		} catch (XmlPullParserException | IOException e) {
			e.printStackTrace();
		}
	}
}
