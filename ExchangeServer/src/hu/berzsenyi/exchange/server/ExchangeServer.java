package hu.berzsenyi.exchange.server;

import java.util.ArrayList;
import java.util.List;

import hu.berzsenyi.exchange.Model;
import hu.berzsenyi.exchange.Team;
import hu.berzsenyi.exchange.log.LogEvent;
import hu.berzsenyi.exchange.log.LogEventConnAccept;
import hu.berzsenyi.exchange.log.LogEventConnAttempt;
import hu.berzsenyi.exchange.log.LogEventConnRefuse;
import hu.berzsenyi.exchange.log.LogEventDisconnect;
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
	public List<LogEvent> log;

	public IServerDisplay display;

	public void setDisplay(IServerDisplay display) {
		this.display = display;
	}

	public void create() {
		this.log = new ArrayList<LogEvent>();

		this.model = new Model();
		this.model.loadStocks("data/stocks");

		this.net = new TCPServer(8080, this, this);

		this.log.add(new LogEvent("Server start", "Server started."));

		if (this.display != null)
			this.display.repaint();
	}

	public void nextRound() {
		this.log.add(new LogEvent("Round end", "Round " + this.model.round
				+ " ended."));

		if (this.model.round == 0) {
			this.net.writeCmdToAll(new CmdServerTeams(this.model));
		}

		this.model.round++;
		this.net.writeCmdToAll(new CmdServerNextRound());

		this.log.add(new LogEvent("Round start", "Round " + this.model.round
				+ " started."));

		if (this.display != null)
			this.display.repaint();
	}

	@Override
	public void onClientConnected(TCPServerClient client) {
		System.out.println("Client connected!");

		this.log.add(new LogEventConnAttempt(client.getAddrString()));
	}

	@Override
	public void handleCmd(TCPCommand cmd, TCPConnection conn) {
		System.out.println("Received command! " + cmd.getClass().getName());

		if (cmd instanceof CmdClientInfo) {
			if (this.model.round == 0) {
				this.model.teams.add(new Team(conn.getAddrString(),
						((CmdClientInfo) cmd).name));
				conn.writeCommand(new CmdServerInfo(this.model.startMoney, conn
						.getAddrString()));
				conn.writeCommand(new CmdServerStocks(this.model));
				this.log.add(new LogEventConnAccept(conn.getAddrString(),
						((CmdClientInfo) cmd).name));
			} else {
				// TODO send feedback
				conn.close();
				this.log.add(new LogEventConnRefuse(conn.getAddrString(),
						((CmdClientInfo) cmd).name));
			}
		}

		if (cmd instanceof CmdClientDisconnect) {
			this.log.add(new LogEventDisconnect(conn.getAddrString(),
					this.model.getTeamById(conn.getAddrString()).name));
			this.model.removeTeam(conn.getAddrString());
			conn.close();
		}

		if (cmd instanceof CmdClientBuy) {
			CmdClientBuy buy = (CmdClientBuy) cmd;
			Team team = this.model.getTeamById(conn.getAddrString());
			team.setMoney(this.model.calculateMoneyAfterPurchase(buy.amount));
			team.setStocks(buy.amount);
			this.log.add(new LogEvent("Zeroth round buy", team.name + "("
					+ team.id + ") performed the zeroth round buy"));
		}

		if (cmd instanceof CmdOffer) {
			CmdOffer offer = (CmdOffer) cmd;
			String to = offer.teamID;
			offer.teamID = conn.getAddrString();
			this.net.writeCmdTo(offer, to);
		}

		if (cmd instanceof CmdOfferResponse) {
			CmdOfferResponse offer = (CmdOfferResponse) cmd;
			Team teamSender = this.model.getTeamById(offer.playerID);
			Team teamReceiver = this.model.getTeamById(conn.getAddrString());

			teamSender.setStock(offer.stockID,
					teamSender.getStock(offer.stockID) + offer.amount);
			teamSender.setMoney(teamSender.getMoney() + offer.money);
			teamReceiver.setStock(offer.stockID,
					teamReceiver.getStock(offer.stockID) - offer.amount);
			teamReceiver.setMoney(teamReceiver.getMoney() - offer.money);

			conn.writeCommand(offer);
			this.net.writeCmdTo(new CmdOfferResponse(conn.getAddrString(),
					offer.stockID, -offer.amount, -offer.money), offer.playerID);
		}

		if (this.display != null)
			this.display.repaint();
	}

	@Override
	public void onClientDisconnected(TCPServerClient client) {
		System.out.println("Client disconnected!");
		this.model.removeTeam(client.getAddrString());

		if (this.display != null)
			this.display.repaint();
	}

	public void destroy() {
		this.net.close();
		this.log.add(new LogEvent("Server start", "Server closed."));
	}
}
