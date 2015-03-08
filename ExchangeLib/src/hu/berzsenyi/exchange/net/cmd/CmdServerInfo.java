package hu.berzsenyi.exchange.net.cmd;

public class CmdServerInfo extends TCPCommand {
	private static final long serialVersionUID = -71777313752126279L;
	
	public double startMoney;
	public String clientID;
	
	
	public CmdServerInfo(double startMoney, String clientID) {
		this.startMoney = startMoney;
		this.clientID = clientID;
	}
	
}
