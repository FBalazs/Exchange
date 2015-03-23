package hu.berzsenyi.exchange.net.msg;

public class MsgServerStockOfferUpdate extends Msg {
	private static final long serialVersionUID = 4865133213017409443L;
	
	public int stockId;
	public double minSellOffer, maxBuyOffer;
	
	public MsgServerStockOfferUpdate(int stockId, double minSellOffer, double maxBuyOffer) {
		this.stockId = stockId;
		this.minSellOffer = minSellOffer;
		this.maxBuyOffer = maxBuyOffer;
	}
}
