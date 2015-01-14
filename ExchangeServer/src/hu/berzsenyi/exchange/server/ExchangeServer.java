package hu.berzsenyi.exchange.server;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.Random;

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
import hu.berzsenyi.exchange.net.cmd.CmdServerError;
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

	public double[] ceventMult = null;
	// private List<LogEvent> log;
	private SimpleDateFormat dateFormat = new SimpleDateFormat(
			"yyyy-MM-dd HH-mm-ss");
	private FileOutputStream logFile;
	public Random rand;

	public IServerDisplay display;

	public void setDisplay(IServerDisplay display) {
		this.display = display;
	}

	public void log(LogEvent event) {
		// this.log.add(event);
		try {
			this.logFile.write(("["
					+ this.dateFormat.format(new Date(event.time)) + "] "
					+ event.title + ": " + event.desc + "\n").getBytes());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void create() {
		// this.log = new ArrayList<LogEvent>();
		try {
			this.logFile = new FileOutputStream("log.txt");
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}

		this.rand = new Random(System.currentTimeMillis());

		this.model = new Model();
		this.model.loadStocks("data/stocks");
		this.model.loadEvents("data/events");

		this.net = new TCPServer(8080, this, this);

		this.log(new LogEvent("Server start", "Server started."));

		if (this.display != null)
			this.display.repaint();
	}

	public void nextRound() {
		this.log(new LogEvent("Round end", "Round " + this.model.round
				+ " ended."));

		if (this.display != null)
			this.display.onRoundEnd(this.model.round);

		if (this.model.round == 0) {
			this.net.writeCmdToAll(new CmdServerTeams(this.model));
		}

		int eventNum = this.rand.nextInt(this.model.eventList.length); // TODO
																		// howmany

		this.model.round++;

		double[] multipliers = new double[this.model.stockList.length];
		if (this.ceventMult != null) {
			for (int i = 0; i < this.model.stockList.length; i++) {
				if (this.model.stockList[i].boughtAmount == 0) {
					multipliers[i] = this.ceventMult[i];
				} else {
					double pvalue = this.model.stockList[i].value;
					double nvalue = (this.model.stockList[i].value
							* this.ceventMult[i] + this.model.stockList[i].boughtFor
							/ this.model.stockList[i].boughtAmount) / 2D;
					multipliers[i] = nvalue / pvalue;
					this.model.stockList[i].boughtFor = this.model.stockList[i].boughtAmount = 0;
				}
			}
		} else {
			Arrays.fill(multipliers, 1.0d);
		}
		this.ceventMult = this.model.eventList[eventNum].multipliers;

		this.model.nextRound(this.model.eventList[eventNum].desc, multipliers);
		this.net.writeCmdToAll(new CmdServerNextRound(this.model.eventMessage,
				multipliers));

		this.log(new LogEvent("Round start", "Round " + this.model.round
				+ " started."));

		if (this.display != null) {
			this.display.onRoundBegin(this.model.round);
			this.display.repaint();
		}
	}

	@Override
	public void onClientConnected(TCPServerClient client) {
		System.out.println("Client connected!");

		this.log(new LogEventConnAttempt(client.getAddrString()));
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
				this.log(new LogEventConnAccept(conn.getAddrString(),
						((CmdClientInfo) cmd).name));
			} else {
				// TODO send feedback
				conn.writeCommand(new CmdServerError(
						CmdServerError.ERROR_CONNECT, null));
				conn.close();
				this.log(new LogEventConnRefuse(conn.getAddrString(),
						((CmdClientInfo) cmd).name));
			}
		}

		if (cmd instanceof CmdClientDisconnect) {
			this.log(new LogEventDisconnect(conn.getAddrString(), this.model
					.getTeamById(conn.getAddrString()).name));
			this.model.removeTeam(conn.getAddrString());
			conn.close();
		}

		if (cmd instanceof CmdClientBuy) {
			CmdClientBuy buy = (CmdClientBuy) cmd;
			Team team = this.model.getTeamById(conn.getAddrString());
			team.setMoney(this.model.calculateMoneyAfterPurchase(buy.amount));
			team.setStocks(buy.amount);
			this.log(new LogEvent("Zeroth round buy", team.name + "(" + team.id
					+ ") performed the zeroth round buy"));
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

			// System.out.println(teamSender.getStock(offer.stockID) +
			// offer.amount);
			// System.out.println(teamSender.getMoney() + offer.money);
			// System.out.println(teamReceiver.getStock(offer.stockID) -
			// offer.amount);
			// System.out.println(teamReceiver.getMoney() - offer.money);

			if (0 <= teamSender.getStock(offer.stockID) + offer.amount
					&& 0 <= teamSender.getMoney() + offer.money
							* Math.abs(offer.amount)
					&& 0 <= teamReceiver.getStock(offer.stockID) - offer.amount
					&& 0 <= teamReceiver.getMoney() - offer.money
							* Math.abs(offer.amount)) {
				teamSender.setStock(offer.stockID,
						teamSender.getStock(offer.stockID) + offer.amount);
				teamSender.setMoney(teamSender.getMoney() + offer.money
						* Math.abs(offer.amount));
				teamReceiver.setStock(offer.stockID,
						teamReceiver.getStock(offer.stockID) - offer.amount);
				teamReceiver.setMoney(teamReceiver.getMoney() - offer.money
						* Math.abs(offer.amount));

				this.model.stockList[offer.stockID].boughtAmount += Math
						.abs(offer.amount);
				this.model.stockList[offer.stockID].boughtFor += Math
						.abs(offer.money * Math.abs(offer.amount));

				this.net.writeCmdTo(new CmdOfferResponse(conn.getAddrString(),
						offer.stockID, offer.amount, offer.money),
						offer.playerID);
				conn.writeCommand(new CmdOfferResponse(offer.playerID,
						offer.stockID, -offer.amount, -offer.money));
			} else {
				// TODO
				conn.writeCommand(new CmdServerError(
						CmdServerError.ERROR_OFFER, null));
				this.net.writeCmdTo(new CmdServerError(
						CmdServerError.ERROR_OFFER, null), offer.playerID);
			}
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
		try {
			this.logFile.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		this.log(new LogEvent("Server start", "Server closed."));
	}
}
