package hu.berzsenyi.exchange.client;

import hu.berzsenyi.exchange.SingleEvent;
import hu.berzsenyi.exchange.Team;
import hu.berzsenyi.exchange.net.IClientConnectionListener;

public interface IClientListener extends IClientConnectionListener {

	public void onStocksCommand(ExchangeClient client);

	public void onTeamsCommand(ExchangeClient client);

	public void onNewRound(SingleEvent[] event);

	public void onMoneyChanged(Team ownTeam);

	public void onStocksChanged(Team ownTeam, int position);
	
	public void onOutgoingOffersChanged();
	
	public void onOfferFailed();
}
