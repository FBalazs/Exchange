package hu.berzsenyi.exchange.client;

import hu.berzsenyi.exchange.SingleEvent;
import hu.berzsenyi.exchange.Team;
import hu.berzsenyi.exchange.net.IClientConnectionListener;
import hu.berzsenyi.exchange.net.cmd.CmdServerError;

public interface IClientListener extends IClientConnectionListener {

	public void onStocksCommand(ExchangeClient client);

	public void onTeamsCommand(ExchangeClient client);

	public void onNewEvents(SingleEvent[] event);

	public void onMoneyChanged(Team ownTeam);

	public void onStocksChanged(Team ownTeam, int position);
	
	public void onOutgoingOffersChanged();

	public void onErrorCommand(CmdServerError error);
}
