package hu.berzsenyi.exchange.net.cmd;

import hu.berzsenyi.exchange.Model;
import hu.berzsenyi.exchange.Stock;
import hu.berzsenyi.exchange.Team;

import java.io.DataInputStream;
import java.io.DataOutputStream;

public class CmdServerInfo extends TCPCommand {
	public static final int ID = 3;
	
	public Model model;
	
	public CmdServerInfo(int length) {
		super(ID, length);
	}
	
	public CmdServerInfo(Model model) {
		super(ID, model.getCmdLength());
		this.model = model;
	}
	
	@Override
	public void read(DataInputStream in) throws Exception {
		this.model = new Model();
		this.model.stockList = new Stock[in.readInt()];
		for(int s = 0; s < this.model.stockList.length; s++)
			this.model.stockList[s] = new Stock(readString(in), in.readDouble());
		int teamNumber = in.readInt();
		for(int t = 0; t < teamNumber; t++)
			this.model.teams.add(new Team(readString(in), readString(in), in.readDouble(), this.model.stockList.length));
	}
	
	@Override
	public void write(DataOutputStream out) throws Exception {
		out.writeInt(this.model.stockList.length);
		for(int s = 0; s < this.model.stockList.length; s++) {
			writeString(out, this.model.stockList[s].name);
			out.writeDouble(this.model.stockList[s].value);
		}
		out.writeInt(this.model.teams.size());
		for(Team team : this.model.teams) {
			writeString(out, team.id);
			writeString(out, team.name);
			out.writeDouble(team.money);
		}
	}
}
