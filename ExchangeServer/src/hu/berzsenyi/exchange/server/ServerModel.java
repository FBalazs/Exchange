package hu.berzsenyi.exchange.server;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.xmlpull.v1.XmlPullParserException;

import hu.berzsenyi.exchange.DatParser;
import hu.berzsenyi.exchange.EventQueue;
import hu.berzsenyi.exchange.Model;
import hu.berzsenyi.exchange.Stock;

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
	

	/**
	 * Loads the stocks from the data files.
	 * 
	 * @param stockFolder
	 *            The folder where the files are located.
	 */
	public void loadStocks(String stockFolder) {
		File[] files = new File(stockFolder).listFiles();
		this.stocks = new Stock[files.length];
		for (int i = 0; i < files.length; i++) {
			try {
				DatParser parser = new DatParser(files[i].getAbsolutePath());
				parser.parse();
				this.stocks[i] = new Stock(files[i].getName().substring(0,
						files[i].getName().lastIndexOf('.')),
						parser.getValue("name"), Double.parseDouble(parser
								.getValue("initvalue")));
				this.stocks[i].sellOffers = new ArrayList<>();
				this.stocks[i].buyOffers = new ArrayList<>();
			} catch (Exception e) {
				e.printStackTrace();
				System.err.println("Failed to parse stock: "
						+ files[i].getName());
			}
		}
	}
}
