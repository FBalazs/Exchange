package hu.berzsenyi.exchange.client;

import hu.berzsenyi.exchange.net.IClientConnectionListener;
import hu.berzsenyi.exchange.net.cmd.CmdOffer;

public interface IClientListener extends IClientConnectionListener {

	public void onStocksCommand(ExchangeClient client);
	
	public void onTeamsCommand(ExchangeClient client);
	
	public void onOfferIn(ExchangeClient client, CmdOffer offer);
	
	public void onRoundCommand(ExchangeClient client);
}
