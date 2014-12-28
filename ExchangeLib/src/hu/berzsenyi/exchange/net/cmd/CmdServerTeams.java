package hu.berzsenyi.exchange.net.cmd;

import hu.berzsenyi.exchange.Model;
import hu.berzsenyi.exchange.Team;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.util.ArrayList;
import java.util.List;

public class CmdServerTeams extends TCPCommand {
	public static final int ID = 4;
	
	public List<Team> teams;
	
	public CmdServerTeams(int length) {
		super(ID, length);
	}
	
	public CmdServerTeams(Model model) {
		super(ID, model.getTeamCmdLength());
		this.teams = model.teams;
	}
	
	@Override
	public void read(DataInputStream in) throws Exception {
		int n = in.readInt();
		this.teams = new ArrayList<Team>(n);
		for(int t = 0; t < n; t++)
			this.teams.add(new Team(readString(in), readString(in)));
	}
	
	@Override
	public void write(DataOutputStream out) throws Exception {
		out.writeInt(this.teams.size());
		for(int t = 0; t < this.teams.size(); t++) {
			writeString(out, this.teams.get(t).id);
			writeString(out, this.teams.get(t).name);
		}
	}
}
