package hu.berzsenyi.exchange;

import java.io.Serializable;
import java.util.List;

public class Stock implements Serializable {
	private static final long serialVersionUID = -7698800698738451407L;
	
	public static interface IOfferCallback {
		public void onOffersPaired(int stockId, Offer offerBuy, Offer offerSell);
	}
	
	public String id, name;
	public double value;
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
		if(sell) {
			if(model.getTeamByName(teamName).getStock(stockId) < amount) {
				sellOffers.add(offer);
				return;
			}
			int best = -1;
			for(int i = 0; i < buyOffers.size(); i++)
				if(!buyOffers.get(i).clientName.equals(teamName) && (best == -1 || buyOffers.get(best).money < buyOffers.get(i).money) && buyOffers.get(i).money*buyOffers.get(i).amount <= model.getTeamByName(buyOffers.get(i).clientName).getMoney())
					best = i;
			if(best != -1 && offer.money <= buyOffers.get(best).money)
				callback.onOffersPaired(stockId, buyOffers.remove(best), offer);
			else
				sellOffers.add(offer);
		} else {
			if(model.getTeamByName(teamName).getMoney() < amount*price) {
				buyOffers.add(offer);
				return;
			}
			int best = -1;
			for(int i = 0; i < sellOffers.size(); i++)
				if(!sellOffers.get(i).clientName.equals(teamName) && (best == -1 || sellOffers.get(i).money < sellOffers.get(best).money) && sellOffers.get(i).amount <= model.getTeamByName(sellOffers.get(i).clientName).getStock(stockId))
					best = i;
			if(best != -1 && sellOffers.get(best).money <= offer.money)
				callback.onOffersPaired(stockId, offer, sellOffers.remove(best));
			else
				buyOffers.add(offer);
		}
	}
}
