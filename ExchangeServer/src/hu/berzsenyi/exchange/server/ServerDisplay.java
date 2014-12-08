package hu.berzsenyi.exchange.server;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

import javax.swing.JFrame;

public class ServerDisplay extends JFrame implements WindowListener {
	private static final long serialVersionUID = 8256191100104297255L;
	
	public ExchangeServer server;
	
	public ServerDisplay(ExchangeServer server) {
		super("Exchange Server");
		this.server = server;
		
		this.setSize(1280, 720);
		this.setLocationRelativeTo(null);
		this.setBackground(Color.white);
		
		this.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		this.addWindowListener(this);
		this.setVisible(true);
	}
	
	@Override
	public void paint(Graphics g) {
		g.setColor(Color.white);
		g.fillRect(0, 0, this.getWidth(), this.getHeight());
//		super.paint(g);
		
		try {
			int dx = this.getWidth()/10;
			int dy = this.getHeight()/10;
			int dw = this.getWidth()*4/10;
			int dh = this.getHeight()*8/10;
			g.setColor(Color.black);
			g.drawLine(dx, dy, dx, dy+dh);
			g.drawLine(dx, dy+dh, dx+dw, dy+dh);
			double maxPrice = 0;
			for(int i = 0; i < this.server.model.stockList.length; i++)
				if(maxPrice < this.server.model.stockList[i].value)
					maxPrice = this.server.model.stockList[i].value;
			for(int i = 0; i < this.server.model.stockList.length; i++) {
				g.drawRect(dx+dw*i/this.server.model.stockList.length, dy+dh-(int)(dh*this.server.model.stockList[i].value/maxPrice), dw/2/this.server.model.stockList.length, (int)(dh*this.server.model.stockList[i].value/maxPrice));
				g.drawString(""+this.server.model.stockList[i].value, dx+dw*i/this.server.model.stockList.length, dy+dh-(int)(dh*this.server.model.stockList[i].value/maxPrice));
				g.drawString(this.server.model.stockList[i].name, dx+dw*i/this.server.model.stockList.length, dy+dh+g.getFontMetrics().getHeight());
			}
			g.drawString(""+maxPrice, dx-(int)g.getFontMetrics().getStringBounds(""+maxPrice, g).getWidth(), dy);
			
			int cx = dx+dw+dx;
			int cy = dy;
			int cw = dw;
			int ch = dh;
			for(int i = 0; i < this.server.model.teams.size(); i++) {
				g.drawString(this.server.model.teams.get(i).id+" "+this.server.model.teams.get(i).name+" "+this.server.model.teams.get(i).money, cx, cy+ch*i/this.server.model.teams.size());
			}
			System.out.println("paint end");
		} catch(Exception e) {
			
		}
	}

	@Override
	public void windowActivated(WindowEvent event) {}

	@Override
	public void windowClosed(WindowEvent event) {}

	@Override
	public void windowClosing(WindowEvent event) {
		this.server.running = false;
	}

	@Override
	public void windowDeactivated(WindowEvent event) {}

	@Override
	public void windowDeiconified(WindowEvent event) {}

	@Override
	public void windowIconified(WindowEvent event) {}

	@Override
	public void windowOpened(WindowEvent event) {}
}
