package hu.berzsenyi.exchange.client;

import hu.berzsenyi.exchange.Team;
import hu.berzsenyi.exchange.net.IClientConnectionListener;
import hu.berzsenyi.exchange.net.cmd.CmdOffer;
import hu.berzsenyi.exchange.net.cmd.CmdOfferResponse;
import hu.berzsenyi.exchange.net.cmd.CmdServerError;

public interface IClientListener extends IClientConnectionListener {

	public void onStocksCommand(ExchangeClient client);
	
	public void onTeamsCommand(ExchangeClient client);
	
	public void onOfferIn(ExchangeClient client, CmdOffer offer);
	
	public void onRoundCommand(ExchangeClient client);
	
	public void onMoneyChanged(Team ownTeam);
	
	public void onStocksChanged(Team ownTeam, int position);
	
	public void onOfferAccepted(CmdOfferResponse offer);
	
	public void onErrorCommand(CmdServerError error);
}
