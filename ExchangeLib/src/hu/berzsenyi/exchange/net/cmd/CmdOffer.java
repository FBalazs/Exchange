package hu.berzsenyi.exchange.net.cmd;

import hu.berzsenyi.exchange.Model;

import java.io.DataInputStream;
import java.io.DataOutputStream;

public class CmdOffer extends TCPCommand {
	public static final int ID = 5;

	public String teamID;
	public int stockID, amount;
	public double money;

	public CmdOffer(int length) {
		super(ID, length);
	}

	public CmdOffer(String playerID, int stockID, int amount, double money) {
		super(ID, 4 + stringLength(playerID) + 4 + 4 + 8);
		this.teamID = playerID;
		this.stockID = stockID;
		this.amount = amount;
		this.money = money;
	}

	@Override
	public void read(DataInputStream in) throws Exception {
		this.teamID = readString(in);
		this.stockID = in.readInt();
		this.amount = in.readInt();
		this.money = in.readDouble();
	}

	@Override
	public void write(DataOutputStream out) throws Exception {
		writeString(out, this.teamID);
		out.writeInt(this.stockID);
		out.writeInt(this.amount);
		out.writeDouble(this.money);
	}

	public String toString(Model model) {
		return "name=" + model.getTeamById(this.teamID).name + " stockName="
				+ model.stockList[this.stockID].name + " amount=" + this.amount
				+ " money=" + this.money;
	}
}
