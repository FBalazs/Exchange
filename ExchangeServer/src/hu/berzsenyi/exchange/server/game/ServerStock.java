package hu.berzsenyi.exchange.server.game;

import java.util.Vector;

import hu.berzsenyi.exchange.game.Offer;
import hu.berzsenyi.exchange.game.Stock;
import hu.berzsenyi.exchange.net.msg.MsgServerOfferToYou;
import hu.berzsenyi.exchange.net.msg.MsgServerStockOfferUpdate;

public class ServerStock extends Stock {
	public static interface IOfferCallback {
		public void onOffersPaired(Offer buyOffer, Offer sellOffer, double price, int amount);
	}
	
	public final String id;
	private long tradeAmount;
	private double tradeMoney;
	private Vector<Offer> offers = new Vector<Offer>();
	
	public ServerStock(String id, String name, double price) {
		super(name, price);
		this.id = id;
		tradeAmount = 10;
		tradeMoney = tradeAmount*price;
	}
	
	public long getTradeAmount() {
		return tradeAmount;
	}
	
	public void setTradeAmount(long value) {
		tradeAmount = value;
	}
	
	public double getTradeMoney() {
		return tradeMoney;
	}
	
	public void setTradeMoney(double value) {
		tradeMoney = value;
	}
	
	private void calculateOfferValues() {
		minSellOffer = maxBuyOffer = -1;
		for(int i = 0; i < offers.size(); i++)
			if(offers.get(i).sell) {
				if(minSellOffer == -1 || offers.get(i).price < minSellOffer)
					minSellOffer = offers.get(i).price;
			} else if(maxBuyOffer == -1 || maxBuyOffer < offers.get(i).price)
				maxBuyOffer = offers.get(i).price;
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
					int tradeAmount = Math.min(Math.min(offer.amount, amount), Math.min(playerSeller.getStockAmount(stockId), (int)Math.floor(playerBuyer.getMoney()/tradePrice)));
					if(0 < tradeAmount) {
						bestI = i;
						bestPrice = tradePrice;
						bestAmount = tradeAmount;
					}
				} else if(!sell && offer.price <= price && (bestI == -1 || tradePrice < bestPrice)) {
					ServerPlayer playerSeller = ServerExchange.INSTANCE.getPlayerByName(offer.sender);
					ServerPlayer playerBuyer = ServerExchange.INSTANCE.getPlayerByName(player);
					int tradeAmount = Math.min(Math.min(offer.amount, amount), Math.min(playerSeller.getStockAmount(stockId), (int)Math.floor(playerBuyer.getMoney()/tradePrice)));
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
		calculateOfferValues();
		ServerExchange.INSTANCE.net.sendMsgToAll(new MsgServerStockOfferUpdate(stockId, minSellOffer, maxBuyOffer));
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
		calculateOfferValues();
		ServerExchange.INSTANCE.net.sendMsgToXY(new MsgServerOfferToYou(sender, stockId, amount, price, !sell), ServerExchange.INSTANCE.getPlayerByName(target).getNetId());
	}
	
	public boolean removeOffer(String sender, int amount, double price, boolean sell) {
		for(int i = 0; i < offers.size(); i++)
			if(offers.get(i).sender.equals(sender)
				&& offers.get(i).amount == amount
				&& offers.get(i).price == price
				&& offers.get(i).sell == sell) {
				offers.remove(i);
				return true;
			}
		return false;
	}
	
	public boolean removeOfferTo(String sender, String target, int amount, double price, boolean sell) {
		for(int i = 0; i < offers.size(); i++)
			if(offers.get(i).sender.equals(sender)
				&& offers.get(i).target.equals(target)
				&& offers.get(i).amount == amount
				&& offers.get(i).price == price
				&& offers.get(i).sell == sell) {
				offers.remove(i);
				return true;
			}
		return false;
	}
}
