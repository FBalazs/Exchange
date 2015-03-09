package hu.berzsenyi.exchange.server;

import java.util.ArrayList;
import java.util.List;

import hu.berzsenyi.exchange.Offer;
import hu.berzsenyi.exchange.Stock;
import hu.berzsenyi.exchange.Stock.IOfferCallback;
import hu.berzsenyi.exchange.Team;
import hu.berzsenyi.exchange.net.NetServer;
import hu.berzsenyi.exchange.net.NetServer.INetServerListener;
import hu.berzsenyi.exchange.net.NetServer.NetServerClient;
import hu.berzsenyi.exchange.net.msg.MsgConnAccept;
import hu.berzsenyi.exchange.net.msg.MsgConnRefuse;
import hu.berzsenyi.exchange.net.msg.MsgConnRequest;
import hu.berzsenyi.exchange.net.msg.MsgNewTeam;
import hu.berzsenyi.exchange.net.msg.MsgOffer;

public class ExchangeServer implements INetServerListener, IOfferCallback {
	public static interface IExchangeServerListener {
		public void onOpend(ExchangeServer server);
		public void onDataChanged(ExchangeServer server);
		public void onClosed(ExchangeServer server);
	}
	
	public static final ExchangeServer INSTANCE = new ExchangeServer();
	
	private NetServer net = new NetServer();
	private List<Team> teams = null;
	
	public Team getTeamByNetId(String netId) {
		for(Team team : teams)
			if(netId.equals(team.netId))
				return team;
		return null;
	}
	
	public Team getTeamByName(String name) {
		for(Team team : teams)
			if(name.equals(team.name))
				return team;
		return null;
	}
	
	public void open(int port) {
		if(!Stock.isLoaded())
			Stock.load();
		teams = new ArrayList<>();
		
		net.open(port);
	}
	
	public void close() {
		net.close();
		
	}

	@Override
	public void onOpened(NetServer net) {
		
	}

	@Override
	public void onClientConnected(NetServer net, NetServerClient netClient) {
		
	}

	@Override
	public void onObjectReceived(NetServer net, NetServerClient netClient, Object o) {
		if(o instanceof MsgConnRequest) {
			MsgConnRequest msg = (MsgConnRequest)o;
			Team team = getTeamByName(msg.nickName);
			if(team == null) {
				MsgConnAccept msgAccept = new MsgConnAccept(teams);
				teams.add(new Team(msg.nickName, msg.password, netClient.getId()));
				netClient.sendObject(msgAccept);
				net.sendObjectToAllExceptXY(new MsgNewTeam(msg.nickName), netClient.getId());
			} else if(msg.password.equals(team.password)) {
				team.netId = netClient.getId();
				netClient.sendObject(new MsgConnAccept(teams));
			} else {
				netClient.sendObject(new MsgConnRefuse());
				netClient.close();
			}
		} else if(o instanceof MsgOffer) {
			MsgOffer msgOffer = (MsgOffer)o;
			Stock.stockList[msgOffer.stockId].addOffer(getTeamByNetId(netClient.getId()).name,
					msgOffer.stockId,
					msgOffer.stockAmount,
					msgOffer.price,
					msgOffer.sell,
					this);
		}
	}
	
	@Override
	public void onOffersPaired(Offer offerBuy, Offer offerSell) {
		int tradeAmount = Math.min(offerBuy.amount, offerSell.amount);
		double tradePrice = (offerBuy.price+offerSell.price)/2;
		Stock.stockList[offerBuy.stockId].addTrade(tradeAmount, tradePrice);
		// TODO trade
	}

	@Override
	public void onClientClosed(NetServer net, NetServerClient netClient) {
		
	}

	@Override
	public void onClosed(NetServer net) {
		
	}
}