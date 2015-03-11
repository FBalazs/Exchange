package hu.berzsenyi.exchange;

import java.io.Serializable;
import java.util.List;

public class Stock implements Serializable {
	private static final long serialVersionUID = -7698800698738451407L;
	
	public static interface IOfferCallback {
		public void onOffersPaired(int stockId, int amount, double price, Offer offerBuy, Offer offerSell);
	}
	
	public String id, name;
	public double value;
	public int circulated = 0;
	/**
	 * <code>value/last value</code>
	 */
	public double change = 1;
	public double boughtFor;
	public int boughtAmount;
	public List<Offer> sellOffers, buyOffers;

	public Stock(String id, String name, double value) {
		this.id = id;
		this.name = name;
		this.value = value;
	}
	
	public void addOffer(Model model, String teamName, int stockId, int amount, double price, boolean sell, IOfferCallback callback) {
		Offer offer = new Offer(teamName, amount, price);
		int bestAmount = 0;
		double bestPrice = 0;
		int best = -1;
		if(sell) {
			for(int i = 0; i < buyOffers.size(); i++)
				if(!buyOffers.get(i).clientName.equals(teamName) && (best == -1 || buyOffers.get(best).money < buyOffers.get(i).money) && offer.money <= buyOffers.get(i).money) {
					double tradePrice = (price+buyOffers.get(i).money)/2;
					int tradeAmount = (int) Math.min(Math.min(amount, model.getTeamByName(teamName).getStock(stockId)),
													Math.min(buyOffers.get(i).amount, Math.floor(model.getTeamByName(buyOffers.get(i).clientName).getMoney()/tradePrice)));
					if(0 < tradeAmount) {
						best = i;
						bestPrice = tradePrice;
						bestAmount = tradeAmount;
					}
				}
			if(best != -1)
				callback.onOffersPaired(stockId, bestAmount, bestPrice, buyOffers.remove(best), offer);
			else
				sellOffers.add(offer);
		} else {
			for(int i = 0; i < sellOffers.size(); i++)
				if(!sellOffers.get(i).clientName.equals(teamName) && (best == -1 || sellOffers.get(i).money < sellOffers.get(best).money) && sellOffers.get(i).money <= offer.money) {
					double tradePrice = (price+sellOffers.get(i).money)/2;
					int tradeAmount = (int) Math.min(Math.min(amount, Math.floor(model.getTeamByName(teamName).getMoney()/tradePrice)),
													Math.min(sellOffers.get(i).amount, model.getTeamByName(sellOffers.get(i).clientName).getStock(stockId)));
					if(0 < tradeAmount) {
						best = i;
						bestPrice = tradePrice;
						bestAmount = tradeAmount;
					}
				}
			if(best != -1)
				callback.onOffersPaired(stockId, bestAmount, bestPrice, offer, sellOffers.remove(best));
			else
				buyOffers.add(offer);
		}
	}
}
