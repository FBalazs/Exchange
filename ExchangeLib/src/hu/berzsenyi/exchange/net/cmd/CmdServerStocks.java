package hu.berzsenyi.exchange.net.cmd;

import hu.berzsenyi.exchange.Model;
import hu.berzsenyi.exchange.Stock;

import java.io.DataInputStream;
import java.io.DataOutputStream;

public class CmdServerStocks extends TCPCommand {
	public static final int ID = 3;
	
	public double startMoney;
	public Stock[] stockList;
	
	public CmdServerStocks(int length) {
		super(ID, length);
	}
	
	public CmdServerStocks(Model model) {
		super(ID, 8+model.getStockCmdLength());
		this.startMoney = model.startMoney;
		this.stockList = model.stockList;
	}
	
	@Override
	public void read(DataInputStream in) throws Exception {
		this.startMoney = in.readDouble();
		this.stockList = new Stock[in.readInt()];
		for(int s = 0; s < this.stockList.length; s++)
			this.stockList[s] = new Stock(readString(in), in.readDouble());
	}
	
	@Override
	public void write(DataOutputStream out) throws Exception {
		out.writeDouble(this.startMoney);
		out.writeInt(this.stockList.length);
		for(int s = 0; s < this.stockList.length; s++) {
			writeString(out, this.stockList[s].name);
			out.writeDouble(this.stockList[s].value);
		}
	}
}
