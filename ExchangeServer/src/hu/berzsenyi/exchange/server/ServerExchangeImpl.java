package hu.berzsenyi.exchange.server;

import java.awt.Frame;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

public class ServerExchangeImpl implements ServerExchange.IServerExchangeListener, WindowListener {
	public static void main(String[] args) {
		new ServerExchangeImpl();
	}
	
	private int port = 8080;
	private Frame frame;
	
	public ServerExchangeImpl() {
		frame = new Frame("Exchange");
		frame.addWindowListener(this);
		frame.setSize(800, 600);
		frame.setLocationRelativeTo(null);
		frame.setVisible(true);
		ServerExchange.INSTANCE.addListener(this);
		ServerExchange.INSTANCE.open(port);
	}

	@Override
	public void windowOpened(WindowEvent e) {
		
	}

	@Override
	public void windowClosing(WindowEvent e) {
		ServerExchange.INSTANCE.close();
		System.exit(0);
	}

	@Override
	public void windowClosed(WindowEvent e) {
		
	}

	@Override
	public void windowIconified(WindowEvent e) {
		
	}

	@Override
	public void windowDeiconified(WindowEvent e) {
		
	}

	@Override
	public void windowActivated(WindowEvent e) {
		
	}

	@Override
	public void windowDeactivated(WindowEvent e) {
		
	}

	@Override
	public void onOpened(ServerExchange exchange) {
		
	}

	@Override
	public void onConnAccepted(ServerExchange exchange) {
		
	}

	@Override
	public void onEvent(ServerExchange exchange) {
		
	}

	@Override
	public void onClosed(ServerExchange exchange) {
		
	}
}
