package hu.berzsenyi.exchange.client;

import hu.berzsenyi.exchange.Team;
import hu.berzsenyi.exchange.net.IClientConnectionListener;
import hu.berzsenyi.exchange.net.cmd.CmdClientOffer;
import hu.berzsenyi.exchange.net.cmd.CmdServerError;

public interface IClientListener extends IClientConnectionListener {

	public void onStocksCommand(ExchangeClient client);
	
	public void onTeamsCommand(ExchangeClient client);
	
	public void onRoundCommand(ExchangeClient client);
	
	public void onMoneyChanged(Team ownTeam);
	
	public void onStocksChanged(Team ownTeam, int position);
	
	public void onErrorCommand(CmdServerError error);
}
