package hu.berzsenyi.exchange;

import java.io.File;
import java.util.PriorityQueue;

public class Stock {
	private static final String STOCKFOLDER = "/stocks";
	
	private static Stock[] stockList = null;
	
	public static boolean isLoaded() {
		return stockList != null;
	}
	
	public static void load() {
		File[] files = new File(STOCKFOLDER).listFiles();
		stockList = new Stock[files.length];
		for (int i = 0; i < files.length; i++) {
			try {
				DatParser parser = new DatParser(files[i].getAbsolutePath());
				parser.parse();
				stockList[i] = new Stock(files[i].getName().substring(0,
						files[i].getName().lastIndexOf('.')),
						parser.getValue("name"),
						Double.parseDouble(parser.getValue("initvalue")));
				stockList[i].buyOffers = new PriorityQueue<>();
				stockList[i].saleOffers = new PriorityQueue<>();
			} catch (Exception e) {
				e.printStackTrace();
				System.err.println("Failed to parse stock: "
						+ files[i].getName());
			}
		}
	}
	
	public static interface IOfferCallback {
		public void onOffersPaired(Offer offerBuy, Offer offerSell);
	}
	
	private final String id, name;
	private double price;
	private int tradeAmount;
	private double tradeMoney;
	private PriorityQueue<Offer> buyOffers, saleOffers;
	
	private Stock(String id, String name, double price) {
		this.id = id;
		this.name = name;
		this.price = price;
		tradeMoney = tradeAmount = 0;
	}
	
	public String getId() {
		return id;
	}
	
	public String getName() {
		return name;
	}
	
	public PriorityQueue<Offer> getBuyOffers() {
		return buyOffers;
	}
	
	public PriorityQueue<Offer> getSaleOffers() {
		return saleOffers;
	}
	
	public void updatePrice(double multiplyer) {
		price = (price + tradeMoney/tradeAmount)/2*multiplyer;
	}
	
	public void addOffer(String teamName, int stockId, int stockAmount, double price, boolean sell, IOfferCallback callback) {
		Offer offer = new Offer(teamName, stockId, stockAmount, price, sell);
		if(sell)
			if(buyOffers.size() != 0)
				callback.onOffersPaired(buyOffers.poll(), offer);
			else
				saleOffers.add(offer);
		else
			if(saleOffers.size() != 0)
				callback.onOffersPaired(offer, saleOffers.poll());
			else
				buyOffers.add(offer);
	}
}