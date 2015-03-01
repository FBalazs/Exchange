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
import hu.berzsenyi.exchange.log.LogEventConnAttempt;
import hu.berzsenyi.exchange.log.LogEventDisconnect;
import hu.berzsenyi.exchange.net.IServerListener;
import hu.berzsenyi.exchange.net.TCPConnection;
import hu.berzsenyi.exchange.net.TCPServer;
import hu.berzsenyi.exchange.net.TCPServerClient;
import hu.berzsenyi.exchange.net.cmd.CmdClientBuy;
import hu.berzsenyi.exchange.net.cmd.CmdClientDisconnect;
import hu.berzsenyi.exchange.net.cmd.CmdClientInfo;
import hu.berzsenyi.exchange.net.cmd.CmdServerError;
import hu.berzsenyi.exchange.net.cmd.CmdServerEvent;
import hu.berzsenyi.exchange.net.cmd.CmdServerInfo;
import hu.berzsenyi.exchange.net.cmd.CmdServerStocks;
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
	
	public IServerDisplay display;
	
	private int[] shuffledEvents;

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


		this.model = new Model();
		this.model.loadStocks("data/stocks");
		this.model.loadEvents("data/events");
		
		this.shuffledEvents = new int[this.model.events.length];
		for(int i=0;i<this.shuffledEvents.length;i++)
			this.shuffledEvents[i] = i;
		shuffle(this.shuffledEvents);

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

		int eventNum = this.shuffledEvents[model.round];
		// TODO ArrayIndexOutOfBoundsException -> not enough events!

		this.model.round++;

		double[] multipliers = new double[this.model.stocks.length];
		if (this.ceventMult != null) {
			for (int i = 0; i < this.model.stocks.length; i++) {
				if (this.model.stocks[i].boughtAmount == 0) {
					multipliers[i] = this.ceventMult[i];
				} else {
					double pvalue = this.model.stocks[i].value;
					double nvalue = (this.model.stocks[i].value
							* this.ceventMult[i] + this.model.stocks[i].boughtFor
							/ this.model.stocks[i].boughtAmount) / 2D;
					multipliers[i] = nvalue / pvalue;
					this.model.stocks[i].boughtFor = this.model.stocks[i].boughtAmount = 0;
				}
			}
		} else {
			Arrays.fill(multipliers, 1.0d);
		}
		this.ceventMult = this.model.events[eventNum].multipliers;

		this.model.nextRound(this.model.events[eventNum].desc, multipliers);
		this.net.writeCmdToAll(new CmdServerEvent(this.model.eventMessage,
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
			CmdClientInfo info = (CmdClientInfo) cmd;
			Team team = this.model.getTeamByName(info.name);
			if (team == null) {
				this.model.teams.add(new Team(conn.getAddrString(), info.name,
						info.password));
				conn.writeCommand(new CmdServerInfo(this.model.startMoney, conn
						.getAddrString()));
				conn.writeCommand(new CmdServerStocks(this.model));
			} else if (team.pass.equals(info.password)) {
				team.id = conn.getAddrString();
				conn.writeCommand(new CmdServerInfo(this.model.startMoney, conn
						.getAddrString()));
				conn.writeCommand(new CmdServerStocks(this.model));
			} else {
				conn.writeCommand(new CmdServerError(
						CmdServerError.ERROR_NAME_DUPLICATE, null));
			}
		}

		if (cmd instanceof CmdClientDisconnect) {
			this.log(new LogEventDisconnect(conn.getAddrString(), this.model
					.getTeamById(conn.getAddrString()).name));
			//this.model.removeTeam(conn.getAddrString());
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

		// if (cmd instanceof CmdClientOffer) {
		// CmdClientOffer offer = (CmdClientOffer) cmd;
		// String to = offer.teamID;
		// offer.teamID = conn.getAddrString();
		// this.net.writeCmdTo(offer, to);
		// }
		//
		// if (cmd instanceof CmdOfferResponse) {
		// CmdOfferResponse offer = (CmdOfferResponse) cmd;
		// Team teamSender = this.model.getTeamById(offer.teamID);
		// Team teamReceiver = this.model.getTeamById(conn.getAddrString());
		//
		// // System.out.println(teamSender.getStock(offer.stockID) +
		// // offer.amount);
		// // System.out.println(teamSender.getMoney() + offer.money);
		// // System.out.println(teamReceiver.getStock(offer.stockID) -
		// // offer.amount);
		// // System.out.println(teamReceiver.getMoney() - offer.money);
		//
		// if (0 <= teamSender.getStock(offer.stockID) + offer.amount
		// && 0 <= teamSender.getMoney() + offer.money
		// * Math.abs(offer.amount)
		// && 0 <= teamReceiver.getStock(offer.stockID) - offer.amount
		// && 0 <= teamReceiver.getMoney() - offer.money
		// * Math.abs(offer.amount)) {
		// teamSender.setStock(offer.stockID,
		// teamSender.getStock(offer.stockID) + offer.amount);
		// teamSender.setMoney(teamSender.getMoney() + offer.money
		// * Math.abs(offer.amount));
		// teamReceiver.setStock(offer.stockID,
		// teamReceiver.getStock(offer.stockID) - offer.amount);
		// teamReceiver.setMoney(teamReceiver.getMoney() - offer.money
		// * Math.abs(offer.amount));
		//
		// this.model.stockList[offer.stockID].boughtAmount += Math
		// .abs(offer.amount);
		// this.model.stockList[offer.stockID].boughtFor += Math
		// .abs(offer.money * Math.abs(offer.amount));
		//
		// this.net.writeCmdTo(new CmdOfferResponse(conn.getAddrString(),
		// offer.stockID, offer.amount, offer.money), offer.teamID);
		// conn.writeCommand(new CmdOfferResponse(offer.teamID,
		// offer.stockID, -offer.amount, -offer.money));
		// } else {
		// // TODO
		// conn.writeCommand(new CmdServerError(
		// CmdServerError.ERROR_OFFER, null));
		// this.net.writeCmdTo(new CmdServerError(
		// CmdServerError.ERROR_OFFER, null), offer.teamID);
		// }
		// }

		if (this.display != null)
			this.display.repaint();
	}

	@Override
	public void onClientDisconnected(TCPServerClient client) {
		System.out.println("Client disconnected!");
		// if(this.model.round == 0)
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
		// this.log(new LogEvent("Server close", "Server closed."));
	}
	
	private static void shuffle(int[] arr) {
		Random rand = new Random();
		for(int i = arr.length; i>1;i--)
			swap(arr, i-1, rand.nextInt(i));
		
	}
	
	private static void swap(int[] arr, int a, int b) {
		int t = arr[a];
		arr[a] = arr[b];
		arr[b] = t;
	}
}
