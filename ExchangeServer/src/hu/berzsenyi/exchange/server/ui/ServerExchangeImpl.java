package hu.berzsenyi.exchange.server.ui;

import hu.berzsenyi.exchange.server.game.ServerExchange;

import java.awt.Frame;
import java.awt.Graphics;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

public class ServerExchangeImpl extends Frame implements ServerExchange.IServerExchangeListener, WindowListener {
	private static final long serialVersionUID = 301475774323683701L;

	public static void main(String[] args) {
		new ServerExchangeImpl();
	}
	
	private int port = 8080;
	
	public ServerExchangeImpl() {
		super("Exchange");
		addWindowListener(this);
		setSize(800, 600);
		setLocationRelativeTo(null);
		setVisible(true);
		ServerExchange.INSTANCE.addListener(this);
		ServerExchange.INSTANCE.open(port);
	}
	
	@Override
	public void paint(Graphics g) {
		
		
		super.paint(g);
	}

	@Override
	public void windowOpened(WindowEvent e) {
		
	}

	@Override
	public void windowClosing(WindowEvent e) {
		ServerExchange.INSTANCE.close();
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
		invalidate();
	}

	@Override
	public void onEvent(ServerExchange exchange) {
		invalidate();
	}
	
	@Override
	public void onMsgReceived(ServerExchange exchange) {
		invalidate();
	}

	@Override
	public void onClosed(ServerExchange exchange) {
		System.exit(0);
	}
}
