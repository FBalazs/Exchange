package hu.berzsenyi.exchange;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class Model {
	public int round = 0;
	public double startMoney = 1000;
	public Stock[] stockList;
	public List<Team> teams = new ArrayList<Team>();
	public String eventMessage = "2-t fizet 3-at kap akció a Trióban!!!"; // TODO
	public Event[] eventList;
	
	/**
	 * Loads the stocks from the data files.
	 * @param stockFolder The folder where the files are located.
	 */
	public void loadStocks(String stockFolder) {
		File[] files = new File(stockFolder).listFiles();
		this.stockList = new Stock[files.length];
		for(int i = 0; i < files.length; i++) {
			DatParser parser = new DatParser(files[i].getAbsolutePath());
			parser.parse();
			this.stockList[i] = new Stock(files[i].getName().substring(0, files[i].getName().lastIndexOf('.')), parser.getValue("name"), Double.parseDouble(parser.getValue("initvalue")));
		}
	}
	
	public void loadEvents(String eventFolder) {
		File[] files = new File(eventFolder).listFiles();
		this.eventList = new Event[files.length];
		for(int i = 0; i < files.length; i++) {
			DatParser parser = new DatParser(files[i].getAbsolutePath());
			parser.parse();
			this.eventList[i] = new Event(files[i].getName().substring(0, files[i].getName().lastIndexOf('.')), parser.getValue("desc"), Integer.parseInt(parser.getValue("howmany")));
			this.eventList[i].multipliers = new double[this.stockList.length];
			for(int s = 0; s < this.stockList.length; s++) {
				String var = parser.getValue(this.stockList[s].id);
				if(var != null)
					this.eventList[i].multipliers[s] = Double.parseDouble(var);
				else
					this.eventList[i].multipliers[s] = 1;
			}
		}
	}
	
	public void nextRound(int eventNum) {
		this.nextRound(this.eventList[eventNum].desc, this.eventList[eventNum].multipliers);
	}
	
	public void nextRound(String eventDesc, double[] multipliers) {
		this.eventMessage = eventDesc;
		for(int i = 0; i < this.stockList.length; i++)
			this.stockList[i].value *= multipliers[i];
	}
	
	public int getStockCmdLength() {
		int ret = 0;
		ret += 4;
		for(int s = 0; s < this.stockList.length; s++)
			ret += this.stockList[s].getCmdLength();
		return ret;
	}
	
	public int getTeamCmdLength() {
		int ret = 0;
		ret += 4;
		for(int t = 0; t < this.teams.size(); t++)
			ret += this.teams.get(t).getCmdLength();
		return ret;
	}
	
	public Team getTeamById(String id) {
		for(Team team : this.teams)
			if(team.id.equals(id))
				return team;
		return null;
	}
	
	@Deprecated
	public Team getTeamByName(String name) {
		for(Team team : this.teams)
			if(team.name.equals(name))
				return team;
		return null;
	}
	
	public void removeTeam(String id) {
		for(int i = 0; i < this.teams.size(); i++)
			if(this.teams.get(i).id.equals(id))
				this.teams.remove(i--);
	}
	


	/**
	 * Only in the zeroth round!
	 * @param amounts
	 * @return
	 */
	public double calculateMoneyAfterPurchase(int[] amounts) {
		double sum = 0.0;
		for (int i = 0; i < amounts.length; i++)
			sum += amounts[i] * stockList[i].value;
		return this.startMoney - sum;
	}
}
