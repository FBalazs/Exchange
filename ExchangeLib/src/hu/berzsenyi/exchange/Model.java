package hu.berzsenyi.exchange;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class Model {
	public int round = 0;
	public double startMoney = 10000;
	public Stock[] stocks;
	public List<Team> teams = new ArrayList<Team>();
	public String eventMessage = "2-t fizet 3-at kap akció a Trióban!!!"; // TODO
	public Event[] events;

	/**
	 * Loads the stocks from the data files.
	 * 
	 * @param stockFolder
	 *            The folder where the files are located.
	 */
	public void loadStocks(String stockFolder) {
		File[] files = new File(stockFolder).listFiles();
		this.stocks = new Stock[files.length];
		for (int i = 0; i < files.length; i++) {
			try {
				DatParser parser = new DatParser(files[i].getAbsolutePath());
				parser.parse();
				this.stocks[i] = new Stock(files[i].getName().substring(0,
						files[i].getName().lastIndexOf('.')),
						parser.getValue("name"), Double.parseDouble(parser
								.getValue("initvalue")));
				this.stocks[i].buyOffers = new ArrayList<Offer>();
				this.stocks[i].saleOffers = new ArrayList<Offer>();
			} catch (Exception e) {
				e.printStackTrace();
				System.err.println("Failed to parse stock: "
						+ files[i].getName());
			}
		}
	}

	
	public void incomingOffer(String clientID, int stockID, int amount, double money) {
		if(money < 0) { // buy offer
			money = -money;
			boolean flag = true;
			while(0 < amount && flag && this.stocks[stockID].saleOffers.size() != 0) {
				int mi = 0;
				for(int i = 1; i < this.stocks[stockID].saleOffers.size(); i++)
					if(this.stocks[stockID].saleOffers.get(i).money < this.stocks[stockID].saleOffers.get(mi).money)
						mi = i;
				Offer minOffer = this.stocks[stockID].saleOffers.get(mi);
				if(money < minOffer.money)
					flag = false;
				else {
					if(amount < minOffer.amount) {
						Team teamBuyer = this.getTeamById(clientID);
						Team teamSeller = this.getTeamById(minOffer.clientID);
						teamBuyer.setStock(stockID, teamBuyer.getStock(stockID)+amount);
						teamSeller.setStock(stockID, teamSeller.getStock(stockID)-amount);
						teamBuyer.setMoney(teamBuyer.getMoney()-(money+minOffer.money)/2*amount);
						teamSeller.setMoney(teamSeller.getMoney()+(money+minOffer.money)/2*amount);
						this.stocks[stockID].boughtAmount += amount;
						this.stocks[stockID].boughtFor += (money+minOffer.money)/2*amount;
						// TODO stock value
						this.stocks[stockID].saleOffers.set(mi, new Offer(minOffer.clientID, minOffer.amount-amount, minOffer.money));
						amount = 0;
					} else if(amount == minOffer.amount) {
						Team teamBuyer = this.getTeamById(clientID);
						Team teamSeller = this.getTeamById(minOffer.clientID);
						teamBuyer.setStock(stockID, teamBuyer.getStock(stockID)+amount);
						teamSeller.setStock(stockID, teamSeller.getStock(stockID)-amount);
						teamBuyer.setMoney(teamBuyer.getMoney()-(money+minOffer.money)/2*amount);
						teamSeller.setMoney(teamSeller.getMoney()+(money+minOffer.money)/2*amount);
						this.stocks[stockID].boughtAmount += amount;
						this.stocks[stockID].boughtFor += (money+minOffer.money)/2*amount;
						// TODO stock value
						this.stocks[stockID].saleOffers.remove(mi);
						amount = 0;
					} else {
						Team teamBuyer = this.getTeamById(clientID);
						Team teamSeller = this.getTeamById(minOffer.clientID);
						teamBuyer.setStock(stockID, teamBuyer.getStock(stockID)+minOffer.amount);
						teamSeller.setStock(stockID, teamSeller.getStock(stockID)-minOffer.amount);
						teamBuyer.setMoney(teamBuyer.getMoney()-(money+minOffer.money)/2*minOffer.amount);
						teamSeller.setMoney(teamSeller.getMoney()+(money+minOffer.money)/2*minOffer.amount);
						this.stocks[stockID].boughtAmount += minOffer.amount;
						this.stocks[stockID].boughtFor += (money+minOffer.money)/2*minOffer.amount;
						// TODO stock value
						this.stocks[stockID].saleOffers.remove(mi);
						amount -= minOffer.amount;
					}
				}
			}
			if(0 < amount)
				this.stocks[stockID].buyOffers.add(new Offer(clientID, amount, money));
		} else { // sell offer
			boolean flag = true;
			while(0 < amount && flag && this.stocks[stockID].buyOffers.size() != 0) {
				int mi = 0;
				for(int i = 1; i < this.stocks[stockID].buyOffers.size(); i++)
					if(this.stocks[stockID].buyOffers.get(mi).money < this.stocks[stockID].buyOffers.get(i).money)
						mi = i;
				Offer maxOffer = this.stocks[stockID].saleOffers.get(mi);
				if(money < maxOffer.money)
					flag = false;
				else {
					if(amount < maxOffer.amount) {
						Team teamBuyer = this.getTeamById(maxOffer.clientID);
						Team teamSeller = this.getTeamById(clientID);
						teamBuyer.setStock(stockID, teamBuyer.getStock(stockID)+amount);
						teamSeller.setStock(stockID, teamSeller.getStock(stockID)-amount);
						teamBuyer.setMoney(teamBuyer.getMoney()-(money+maxOffer.money)/2*amount);
						teamSeller.setMoney(teamSeller.getMoney()+(money+maxOffer.money)/2*amount);
						this.stocks[stockID].boughtAmount += amount;
						this.stocks[stockID].boughtFor += (money+maxOffer.money)/2*amount;
						// TODO stock value
						this.stocks[stockID].buyOffers.set(mi, new Offer(maxOffer.clientID, maxOffer.amount-amount, maxOffer.money));
						amount = 0;
					} else if(amount == maxOffer.amount) {
						Team teamBuyer = this.getTeamById(maxOffer.clientID);
						Team teamSeller = this.getTeamById(clientID);
						teamBuyer.setStock(stockID, teamBuyer.getStock(stockID)+amount);
						teamSeller.setStock(stockID, teamSeller.getStock(stockID)-amount);
						teamBuyer.setMoney(teamBuyer.getMoney()-(money+maxOffer.money)/2*amount);
						teamSeller.setMoney(teamSeller.getMoney()+(money+maxOffer.money)/2*amount);
						this.stocks[stockID].boughtAmount += amount;
						this.stocks[stockID].boughtFor += (money+maxOffer.money)/2*amount;
						// TODO stock value
						this.stocks[stockID].buyOffers.remove(mi);
						amount = 0;
					} else {
						Team teamBuyer = this.getTeamById(maxOffer.clientID);
						Team teamSeller = this.getTeamById(clientID);
						teamBuyer.setStock(stockID, teamBuyer.getStock(stockID)+maxOffer.amount);
						teamSeller.setStock(stockID, teamSeller.getStock(stockID)-maxOffer.amount);
						teamBuyer.setMoney(teamBuyer.getMoney()-(money+maxOffer.money)/2*maxOffer.amount);
						teamSeller.setMoney(teamSeller.getMoney()+(money+maxOffer.money)/2*maxOffer.amount);
						this.stocks[stockID].boughtAmount += maxOffer.amount;
						this.stocks[stockID].boughtFor += (money+maxOffer.money)/2*maxOffer.amount;
						// TODO stock value
						this.stocks[stockID].buyOffers.remove(mi);
						amount -= maxOffer.amount;
					}
				}
			}
			if(0 < amount)
				this.stocks[stockID].saleOffers.add(new Offer(clientID, amount, money));
		}
	}

	public void nextRound(String eventDesc, double[] multipliers) {
		this.eventMessage = eventDesc;
		for (int i = 0; i < multipliers.length; i++) {
			this.stocks[i].value *= multipliers[i];
			this.stocks[i].change = multipliers[i];
		}
	}

	public int getStockCmdLength() {
		int ret = 0;
		ret += 4;
		for (int s = 0; s < this.stocks.length; s++)
			ret += this.stocks[s].getCmdLength();
		return ret;
	}

	public Team getTeamById(String id) {
		for (Team team : this.teams)
			if (team.id.equals(id))
				return team;
		return null;
	}

	public Team getTeamByName(String name) {
		for (Team team : this.teams)
			if (team.name.equals(name))
				return team;
		return null;
	}

	public void removeTeam(String id) {
		for (int i = 0; i < this.teams.size(); i++)
			if (this.teams.get(i).id.equals(id))
				this.teams.remove(i--);
	}

	/**
	 * Only in the zeroth round!
	 * 
	 * @param amounts
	 * @return
	 */
	public double calculateMoneyAfterPurchase(int[] amounts) {
		double sum = 0.0;
		for (int i = 0; i < amounts.length; i++)
			sum += amounts[i] * stocks[i].value;
		return this.startMoney - sum;
	}
}
