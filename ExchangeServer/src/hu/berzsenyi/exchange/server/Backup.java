package hu.berzsenyi.exchange.server;

import hu.berzsenyi.exchange.Team;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;

public class Backup {
	public static void save(ExchangeServer server, String fileName) {
		try {
			new File(fileName.substring(0, fileName.lastIndexOf('/'))).mkdirs();
			//System.out.println(new File(fileName.substring(0, fileName.lastIndexOf('/'))).getAbsolutePath());
			FileOutputStream fout = new FileOutputStream(fileName);
			DataOutputStream dout = new DataOutputStream(fout);
			for(int s = 0; s < server.model.stocks.length; s++) {
				dout.writeDouble(server.model.stocks[s].value);
				dout.writeInt(server.model.stocks[s].boughtAmount);
				dout.writeDouble(server.model.stocks[s].boughtFor);
			}
			dout.writeInt(server.ceventMult.length);
			for(int i = 0; i < server.ceventMult.length; i++)
				dout.writeDouble(server.ceventMult[i]);
			dout.writeInt(server.shuffledEvents.length);
			for(int i = 0; i < server.shuffledEvents.length; i++)
				dout.writeInt(server.shuffledEvents[i]);
			// TODO save event queue - not important
			dout.writeInt(server.model.round);
			dout.writeInt(server.model.teams.size());
			for(int t = 0; t < server.model.teams.size(); t++) {
				dout.writeUTF(server.model.teams.get(t).name);
				dout.writeUTF(server.model.teams.get(t).pass);
				dout.writeDouble(server.model.teams.get(t).getMoney());
				dout.writeInt(server.model.teams.get(t).getStocks().length);
				for(int i = 0; i < server.model.teams.get(t).getStocks().length; i++)
					dout.writeInt(server.model.teams.get(t).getStocks()[i]);
			}
			dout.close();
			fout.close();
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void load(ExchangeServer server, String fileName) {
		try {
			DataInputStream din = new DataInputStream(new FileInputStream(fileName));
			for(int s = 0; s < server.model.stocks.length; s++) {
				server.model.stocks[s].value = din.readDouble();
				server.model.stocks[s].boughtAmount = din.readInt();
				server.model.stocks[s].boughtFor = din.readDouble();
			}
			server.ceventMult = new double[din.readInt()];
			for(int i = 0; i < server.ceventMult.length; i++)
				server.ceventMult[i] = din.readDouble();
			server.shuffledEvents = new int[din.readInt()];
			for(int i = 0; i < server.shuffledEvents.length; i++)
				server.shuffledEvents[i] = din.readInt();
			// TODO load event queue - not important
			server.model.round = din.readInt();
			int teamN = din.readInt();
			for(int t = 0; t < teamN; t++) {
				Team team = new Team(server.model, null, din.readUTF(), din.readUTF());
				team.setMoney(din.readDouble());
				int[] stocks = new int[din.readInt()];
				for(int i = 0; i < stocks.length; i++)
					stocks[i] = din.readInt();
				team.setStocks(stocks);
				server.model.teams.add(team);
			}
			din.close();
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
}
