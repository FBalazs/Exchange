package hu.berzsenyi.exchange;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class Model {
	public int round = 0;
	public Stock[] stockList;
	public List<Team> teams = new ArrayList<Team>();
	
	/**
	 * Returns the number of bytes that can store this objects data.
	 * @return The number of bytes.
	 */
	public int getCmdLength() {
		int ret = 0;
		ret += 4;
		for(int s = 0; s < this.stockList.length; s++)
			ret += 4+this.stockList[s].name.length()+8;
		ret += 4;
		for(int t = 0; t < this.teams.size(); t++)
			ret += 4+this.teams.get(t).id.length()+4+this.teams.get(t).name.length()+8;
		return ret;
	}
	
	/**
	 * Loads the stocks from the data files.
	 * @param stockFolder The folder where the files are located.
	 */
	public void loadStocks(String stockFolder) {
		File[] files = new File(stockFolder).listFiles();
		this.stockList = new Stock[files.length];
		for(int i = 0; i < files.length; i++) {
			Configuration config = new Configuration(files[i].getAbsolutePath());
			config.read();
			this.stockList[i] = new Stock(config.getValue("name", "null"), Double.parseDouble(config.getValue("initvalue", "0")));
		}
	}
	
	/**
	 * Adds a new team to the game.
	 * @param id The id (the address) of the team.
	 * @param name The name of the team.
	 */
	public void newTeam(String id, String name) {
		this.teams.add(new Team(id, name, 1000, this.stockList.length));
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
}
