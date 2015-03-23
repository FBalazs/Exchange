package hu.berzsenyi.exchange.server;

import java.util.Vector;

import hu.berzsenyi.exchange.Offer;
import hu.berzsenyi.exchange.Stock;

public class ServerStock extends Stock {
	public static interface IOfferCallback {
		public void onOffersPaired(Offer buyOffer, Offer sellOffer, double price, int amount);
	}
	
	private final String id;
	private long tradeAmount;
	private double tradeMoney;
	private Vector<Offer> offers;
	
	public ServerStock(String id, String name, double price) {
		super(name, price);
		this.id = id;
		tradeAmount = 10;
		tradeMoney = tradeAmount*price;
	}
	
	public void updatePrice(double multiplier) {
		price = (price*multiplier + tradeMoney/tradeAmount)/2;
	}
	
	public void addOffer(String player, int stockId, int amount, double price, boolean sell, IOfferCallback callback) {
		int bestI = -1;
		double bestPrice = 0;
		int bestAmount = 0;
		Offer offerToAdd = new Offer(player, null, stockId, amount, price, sell);
		for(int i = 0; i < offers.size(); i++) {
			Offer offer = offers.get(i);
			if(offer.sell != sell && !offer.sender.equals(player)) {
				double tradePrice = (offer.price+price)/2;
				if(sell && price <= offer.price && (bestI == -1 || bestPrice < tradePrice)) {
					ServerPlayer playerSeller = ServerExchange.INSTANCE.getPlayerByName(player);
					ServerPlayer playerBuyer = ServerExchange.INSTANCE.getPlayerByName(offer.sender);
					int tradeAmount = Math.min(Math.min(offer.amount, amount), Math.min(playerSeller.stocks[stockId], (int)Math.floor(playerBuyer.money/tradePrice)));
					if(0 < tradeAmount) {
						bestI = i;
						bestPrice = tradePrice;
						bestAmount = tradeAmount;
					}
				} else if(!sell && offer.price <= price && (bestI == -1 || tradePrice < bestPrice)) {
					ServerPlayer playerSeller = ServerExchange.INSTANCE.getPlayerByName(offer.sender);
					ServerPlayer playerBuyer = ServerExchange.INSTANCE.getPlayerByName(player);
					int tradeAmount = Math.min(Math.min(offer.amount, amount), Math.min(playerSeller.stocks[stockId], (int)Math.floor(playerBuyer.money/tradePrice)));
					if(0 < tradeAmount) {
						bestI = i;
						bestPrice = tradePrice;
						bestAmount = tradeAmount;
					}
				}
			}
		}
		if(bestI != -1) {
			tradeAmount += bestAmount;
			tradeMoney += bestAmount*bestPrice;
			if(sell)
				callback.onOffersPaired(offers.remove(bestI), offerToAdd, bestPrice, bestAmount);
			else
				callback.onOffersPaired(offerToAdd, offers.remove(bestI), bestPrice, bestAmount);
		} else
			offers.add(offerToAdd);
	}
	
	public void addOfferTo(String sender, String target, int stockId, int amount, double price, boolean sell, IOfferCallback callback) {
		Offer offerToAdd = new Offer(sender, target, stockId, amount, price, sell);
		for(int i = 0; i < offers.size(); i++) {
			Offer offer = offers.get(i);
			if(!offer.sender.equals(sender)
				&& offer.sender.equals(target)
				&& offer.target.equals(sender)
				&& offer.amount == amount
				&& offer.price == price
				&& offer.sell != sell) {
				tradeAmount += amount;
				tradeMoney += amount*price;
				if(sell)
					callback.onOffersPaired(offers.remove(i--), offerToAdd, price, amount);
				else
					callback.onOffersPaired(offerToAdd, offers.remove(i--), price, amount);
			}
		}
		offers.add(offerToAdd);
	}
}
