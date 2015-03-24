package hu.berzsenyi.exchange.server.ui;

import hu.berzsenyi.exchange.server.game.ServerExchange;

import java.awt.Graphics;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.awt.event.WindowStateListener;

import javax.swing.JFrame;

public class ServerExchangeImpl extends JFrame implements ServerExchange.IServerExchangeListener, WindowListener, WindowStateListener {
	private static final long serialVersionUID = 301475774323683701L;

	public static void main(String[] args) {
		new ServerExchangeImpl();
	}
	
	private int port = 8080;
	private CompStocks compStocks = new CompStocks();
	
	public ServerExchangeImpl() {
		super("Exchange");
		addWindowListener(this);
		addWindowStateListener(this);
		setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		setSize(800, 600);
		setLocationRelativeTo(null);
		setLayout(null);
		add(compStocks);
		setVisible(true);
		
		onResize();
		
		ServerExchange.INSTANCE.addListener(this);
		ServerExchange.INSTANCE.open(port);
	}
	
	private void onResize() {
		int minSize = Math.min(getWidth(), getHeight());
		compStocks.setBounds(minSize/10, minSize/10, getWidth()-minSize/5, getHeight()-minSize/5);
	}
	
	@Override
	public void paint(Graphics g) {
		onResize();
		
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

	@Override
	public void windowStateChanged(WindowEvent e) {
		onResize();
		invalidate();
	}
}
