package hu.berzsenyi.exchange.net.cmd;

public class CmdClientBuy extends TCPCommand {
	private static final long serialVersionUID = 3758429926195967009L;

	public int[] amount;

	public CmdClientBuy(int[] amount) {
		this.amount = amount;
	}

}
