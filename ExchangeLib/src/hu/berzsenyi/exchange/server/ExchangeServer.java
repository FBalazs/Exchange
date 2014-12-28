package hu.berzsenyi.exchange.server;

import hu.berzsenyi.exchange.Model;
import hu.berzsenyi.exchange.Team;
import hu.berzsenyi.exchange.net.IServerListener;
import hu.berzsenyi.exchange.net.TCPConnection;
import hu.berzsenyi.exchange.net.TCPServer;
import hu.berzsenyi.exchange.net.TCPServerClient;
import hu.berzsenyi.exchange.net.cmd.CmdClientBuy;
import hu.berzsenyi.exchange.net.cmd.CmdClientDisconnect;
import hu.berzsenyi.exchange.net.cmd.CmdClientInfo;
import hu.berzsenyi.exchange.net.cmd.CmdOffer;
import hu.berzsenyi.exchange.net.cmd.CmdOfferResponse;
import hu.berzsenyi.exchange.net.cmd.CmdServerStocks;
import hu.berzsenyi.exchange.net.cmd.ICmdHandler;
import hu.berzsenyi.exchange.net.cmd.TCPCommand;

public class ExchangeServer implements IServerListener, ICmdHandler {
	public boolean running;
	public TCPServer net;
	public Model model;
	
	public void create() {
		this.model = new Model();
		this.model.loadStocks("data/stocks");
		
		this.net = new TCPServer(8080, this, this);
	}
	
	@Override
	public void onClientConnected(TCPServerClient client) {
		System.out.println("Client connected!");
		
	}
	
	@Override
	public void handleCmd(TCPCommand cmd, TCPConnection conn) {
		System.out.println("Received command! "+cmd.getClass().getName());
		
		if(cmd instanceof CmdClientInfo) {
			if(this.model.round == 0) {
				this.model.teams.add(new Team(conn.getAddrString(), ((CmdClientInfo)cmd).name));
				conn.writeCommand(new CmdServerStocks(this.model));
			} else {
				// TODO send feedback and disconnect client
				conn.close();
			}
			return;
		}
		
		if(cmd instanceof CmdClientDisconnect) {
			this.model.removeTeam(conn.getAddrString());
			conn.close();
			return;
		}
		
		if(cmd instanceof CmdClientBuy) {
			CmdClientBuy buy = (CmdClientBuy)cmd;
			Team team = this.model.getTeamById(conn.getAddrString());
			team.stocks = buy.amount;
			team.money = this.model.startMoney;
			for(int i = 0; i < team.stocks.length; i++)
				team.money -= this.model.stockList[i].value*team.stocks[i];
			return;
		}
		
		if(cmd instanceof CmdOffer) {
			CmdOffer offer = (CmdOffer)cmd;
			String to = offer.playerID;
			offer.playerID = conn.getAddrString();
			this.net.writeCmdTo(offer, to);
			return;
		}
		
		if(cmd instanceof CmdOfferResponse) {
			CmdOfferResponse offer = (CmdOfferResponse)cmd;
			Team teamSender = this.model.getTeamById(offer.playerID);
			Team teamReceiver = this.model.getTeamById(conn.getAddrString());
			
			teamSender.stocks[offer.stockID] += offer.amount;
			teamSender.money += offer.money;
			teamReceiver.stocks[offer.stockID] -= offer.amount;
			teamReceiver.money -= offer.money;
			
			conn.writeCommand(offer);
			this.net.writeCmdTo(new CmdOfferResponse(conn.getAddrString(), offer.stockID, -offer.amount, -offer.money), offer.playerID);
			return;
		}
	}

	@Override
	public void onClientDisconnected(TCPServerClient client) {
		System.out.println("Client disconnected!");
		this.model.removeTeam(client.getAddrString());
	}
	
	public void update() {
		if(!this.net.open)
			this.net = new TCPServer(8080, this, this);
		this.net.update();
		//System.out.println("clients: "+this.net.getClientNumber());
	}
	
	public void destroy() {
		this.net.close();
	}
}
