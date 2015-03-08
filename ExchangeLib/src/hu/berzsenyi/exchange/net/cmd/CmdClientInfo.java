package hu.berzsenyi.exchange.net.cmd;

public class CmdClientInfo extends TCPCommand {
	private static final long serialVersionUID = -7317720180092841795L;

	public String name, password;
	public CmdClientInfo(String name, String password) {
		this.name = name;
		this.password = password;
	}
}
