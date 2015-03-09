package hu.berzsenyi.exchange.server;

import hu.berzsenyi.exchange.Offer;
import hu.berzsenyi.exchange.Stock.IOfferCallback;
import hu.berzsenyi.exchange.Team;
import hu.berzsenyi.exchange.log.LogEvent;
import hu.berzsenyi.exchange.log.LogEventConnAttempt;
import hu.berzsenyi.exchange.net.IServerListener;
import hu.berzsenyi.exchange.net.TCPConnection;
import hu.berzsenyi.exchange.net.TCPServer;
import hu.berzsenyi.exchange.net.TCPServerClient;
import hu.berzsenyi.exchange.net.msg.*;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.Random;

public class ExchangeServer implements IServerListener, ICmdHandler, IOfferCallback {
	
	public boolean running;
	public TCPServer net;
	public ServerModel model;

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


		this.model = new ServerModel();
		this.model.loadStocks("data/stocks");
		this.model.loadEvents(new File("data/events/events.xml"));
		
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

		int eventNum = this.shuffledEvents[(model.round)%this.shuffledEvents.length];
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
		this.ceventMult = this.model.events[eventNum].getMultipliers();

		this.model.nextRound(multipliers);
		this.net.writeCmdToAll(new MsgStockInfo(this.model.stocks));
		// TODO send event

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
	public void handleCmd(Object o, TCPConnection conn) {
		System.out.println("Received command! " + o.getClass().getName());
		
		if(o instanceof MsgConnRequest) {
			MsgConnRequest msg = (MsgConnRequest)o;
			Team team = this.model.getTeamByName(msg.nickName);
			if(team == null && this.model.round == 0) {
				team = new Team(this.model, conn.getAddrString(), msg.nickName, msg.password);
				team.setMoney(this.model.startMoney);
				this.model.teams.add(team);
				conn.writeCommand(new MsgConnAccept(team.getMoney(), null, this.model.teams, this.model.stocks));
				conn.writeCommand(new MsgBuyRequest());
			} else if(team != null && team.pass.equals(msg.password)) {
				team.id = conn.getAddrString();
				conn.writeCommand(new MsgConnAccept(team.getMoney(), team.getStocks(), this.model.teams, this.model.stocks));
				if(team.getStocks() == null) {
					conn.writeCommand(new MsgBuyRequest());
				}
			} else {
				conn.writeCommand(new MsgConnRefuse());
			}
		} else if(o instanceof MsgBuy) {
			MsgBuy msg = (MsgBuy)o;
			Team team = this.model.getTeamById(conn.getAddrString());
			team.setStocks(msg.amounts);
			for(int i = 0; i < msg.amounts.length; i++)
				team.setMoney(team.getMoney()-msg.amounts[i]*this.model.stocks[i].value);
		} else if(o instanceof MsgOffer) {
			MsgOffer msg = (MsgOffer)o;
			Team team = this.model.getTeamById(conn.getAddrString());
			this.model.stocks[msg.stockId].addOffer(team.name, msg.stockId, msg.stockAmount, msg.price, msg.sell, this);
		} else if(o instanceof MsgOfferDelete) {
			MsgOfferDelete msg = (MsgOfferDelete)o;
			Team team = this.model.getTeamById(conn.getAddrString());
			if(msg.sell) {
				boolean removed = false;
				for(int i = 0; !removed && i < this.model.stocks[msg.stockId].sellOffers.size(); i++) {
					Offer offer = this.model.stocks[msg.stockId].sellOffers.get(i);
					if(offer.clientName.equals(team.name) && offer.money == msg.price && offer.amount == msg.stockAmount) {
						this.model.stocks[msg.stockId].sellOffers.remove(i);
						removed = true;
					}
				}
			} else {
				boolean removed = false;
				for(int i = 0; !removed && i < this.model.stocks[msg.stockId].buyOffers.size(); i++) {
					Offer offer = this.model.stocks[msg.stockId].buyOffers.get(i);
					if(offer.clientName.equals(team.name) && offer.money == msg.price && offer.amount == msg.stockAmount) {
						this.model.stocks[msg.stockId].buyOffers.remove(i);
						removed = true;
					}
				}
			}
		}

		if (this.display != null)
			this.display.repaint();
	}
	
	@Override
	public void onOffersPaired(int stockId, Offer offerBuy, Offer offerSell) {
		Team teamBuy = this.model.getTeamByName(offerBuy.clientName);
		Team teamSell = this.model.getTeamByName(offerSell.clientName);
		int amount = Math.min(offerBuy.amount, offerSell.amount);
		double price = (offerBuy.money+offerSell.money)/2;
		this.model.stocks[stockId].boughtAmount += amount;
		this.model.stocks[stockId].boughtFor += amount*price;
		offerBuy.amount -= amount;
		offerSell.amount -= amount;
		teamBuy.setMoney(teamBuy.getMoney()-price*amount);
		teamSell.setMoney(teamSell.getMoney()+price*amount);
		teamBuy.setStock(stockId, teamBuy.getStock(stockId)+amount);
		teamSell.setStock(stockId, teamSell.getStock(stockId)-amount);
		this.net.writeCmdTo(new MsgTeamInfo(teamBuy.getMoney(), teamBuy.getStocks()), teamBuy.id);
		this.net.writeCmdTo(new MsgTeamInfo(teamSell.getMoney(), teamSell.getStocks()), teamSell.id);
		// TODO send offer change info to clients
		if(offerBuy.amount != 0)
			this.model.stocks[stockId].addOffer(offerBuy.clientName, stockId, offerBuy.amount, offerBuy.money, false, this);
		if(offerSell.amount != 0)
			this.model.stocks[stockId].addOffer(offerSell.clientName, stockId, offerSell.amount, offerSell.money, true, this);
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
