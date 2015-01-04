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
import hu.berzsenyi.exchange.net.cmd.CmdServerInfo;
import hu.berzsenyi.exchange.net.cmd.CmdServerNextRound;
import hu.berzsenyi.exchange.net.cmd.CmdServerStocks;
import hu.berzsenyi.exchange.net.cmd.CmdServerTeams;
import hu.berzsenyi.exchange.net.cmd.ICmdHandler;
import hu.berzsenyi.exchange.net.cmd.TCPCommand;

public class ExchangeServer implements IServerListener, ICmdHandler {
	public boolean running;
	public TCPServer net;
	public Model model;
	
	public IServerDisplay display;
	
	public void setDisplay(IServerDisplay display) {
		this.display = display;
	}
	
	public void create() {
		this.model = new Model();
		this.model.loadStocks("data/stocks");
		
		this.net = new TCPServer(8080, this, this);
		
		if(this.display != null)
			this.display.repaint();
	}
	
	public void nextRound() {
		if(this.model.round == 0) {
			this.net.writeCmdToAll(new CmdServerTeams(this.model));
		}
		
		this.model.round++;
		this.net.writeCmdToAll(new CmdServerNextRound());
		if(this.display != null)
			this.display.repaint();
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
				conn.writeCommand(new CmdServerInfo(this.model.startMoney, conn.getAddrString()));
				conn.writeCommand(new CmdServerStocks(this.model));
			} else {
				// TODO send feedback
				conn.close();
			}
		}
		
		if(cmd instanceof CmdClientDisconnect) {
			this.model.removeTeam(conn.getAddrString());
			conn.close();
		}
		
		if(cmd instanceof CmdClientBuy) {
			CmdClientBuy buy = (CmdClientBuy)cmd;
			Team team = this.model.getTeamById(conn.getAddrString());
			team.stocks = buy.amount;
			team.money = this.model.startMoney;
			for(int i = 0; i < team.stocks.length; i++)
				team.money -= this.model.stockList[i].value*team.stocks[i];
		}
		
		if(cmd instanceof CmdOffer) {
			CmdOffer offer = (CmdOffer)cmd;
			String to = offer.teamID;
			offer.teamID = conn.getAddrString();
			this.net.writeCmdTo(offer, to);
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
		}
		
		if(this.display != null)
			this.display.repaint();
	}

	@Override
	public void onClientDisconnected(TCPServerClient client) {
		System.out.println("Client disconnected!");
		this.model.removeTeam(client.getAddrString());
		
		if(this.display != null)
			this.display.repaint();
	}
	
	public void destroy() {
		this.net.close();
	}
}
