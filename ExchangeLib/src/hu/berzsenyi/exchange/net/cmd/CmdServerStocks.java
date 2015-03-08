package hu.berzsenyi.exchange.net.cmd;

import hu.berzsenyi.exchange.Model;
import hu.berzsenyi.exchange.Stock;

public class CmdServerStocks extends TCPCommand {

	private static final long serialVersionUID = 4479127084065564228L;
	public Stock[] stockList;

	public CmdServerStocks(Model model) {
		this.stockList = model.stocks;
	}

}
