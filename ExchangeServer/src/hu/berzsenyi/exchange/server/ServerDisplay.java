package hu.berzsenyi.exchange.server;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowFocusListener;
import java.awt.event.WindowListener;
import java.awt.event.WindowStateListener;

import javax.swing.JButton;
import javax.swing.JFrame;

public class ServerDisplay extends JFrame implements WindowListener, WindowStateListener, WindowFocusListener, IServerDisplay {
	private static final long serialVersionUID = 8256191100104297255L;
	
	public ExchangeServer server;
	
	public int width = 1280, height = 720;
	
	public Rectangle rectStocks, rectTeams;
	public JButton btnNextRound;
	
	public ServerDisplay() {
		super("Exchange Server");
		this.server = new ExchangeServer();
		this.server.setDisplay(this);
		
		this.setSize(this.width, this.height);
		this.setLayout(null);
		this.setLocationRelativeTo(null);
		this.setBackground(Color.white);
		
		this.btnNextRound = new JButton("Next Round");
		this.btnNextRound.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				ServerDisplay.this.server.nextRound();
			}
		});
		this.add(this.btnNextRound);
		
		this.onResize();
		
		this.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		this.addWindowListener(this);
		this.addWindowStateListener(this);
		this.addWindowFocusListener(this);
		this.setVisible(true);
		
		this.server.create();
		this.server.running = true;
	}
	
	public void onResize() {
		int minSize = Math.min(this.width, this.height);
		this.rectStocks = new Rectangle(minSize/10, minSize/10, this.width/2-minSize/5, this.height/2-minSize/5);
		this.rectTeams = new Rectangle(this.rectStocks.x, this.rectStocks.y+this.rectStocks.height+minSize/5, this.rectStocks.width, this.rectStocks.height);
		this.btnNextRound.setBounds(this.width/2-minSize/10, this.height/2-minSize/20, minSize/5, minSize/10);
	}
	
	@Override
	public void paint(Graphics g) {
		if(this.getWidth() != this.width || this.getHeight() != this.height) {
			this.width = this.getWidth();
			this.height = this.getHeight();
			this.onResize();
		}
		super.paint(g);
		
		try {
			int minSize = Math.min(this.getWidth(), this.getHeight());
			
			g.setColor(Color.white);
			g.fillRect(this.rectStocks.x, this.rectStocks.y, this.rectStocks.width, this.rectStocks.height);
			g.setColor(Color.black);
			g.drawLine(this.rectStocks.x, this.rectStocks.y, this.rectStocks.x, this.rectStocks.y+this.rectStocks.height);
			g.drawLine(this.rectStocks.x, this.rectStocks.y+this.rectStocks.height, this.rectStocks.x+this.rectStocks.width, this.rectStocks.y+this.rectStocks.height);
			double maxPrice = 0;
			for(int i = 0; i < this.server.model.stockList.length; i++)
				if(maxPrice < this.server.model.stockList[i].value)
					maxPrice = this.server.model.stockList[i].value;
			for(int i = 0; i < this.server.model.stockList.length; i++) {
				g.drawRect(this.rectStocks.x+this.rectStocks.width*i/this.server.model.stockList.length, this.rectStocks.y+this.rectStocks.height-(int)(this.rectStocks.height*this.server.model.stockList[i].value/maxPrice), this.rectStocks.width/2/this.server.model.stockList.length, (int)(this.rectStocks.height*this.server.model.stockList[i].value/maxPrice));
				g.drawString(""+this.server.model.stockList[i].value, this.rectStocks.x+this.rectStocks.width*i/this.server.model.stockList.length, this.rectStocks.y+this.rectStocks.height-(int)(this.rectStocks.height*this.server.model.stockList[i].value/maxPrice));
				g.drawString(this.server.model.stockList[i].name, this.rectStocks.x+this.rectStocks.width*i/this.server.model.stockList.length, this.rectStocks.y+this.rectStocks.height+g.getFontMetrics().getHeight());
			}
			g.drawString(""+maxPrice, this.rectStocks.x-(int)g.getFontMetrics().getStringBounds(""+maxPrice, g).getWidth(), this.rectStocks.y);
			
			g.setColor(Color.white);
			g.fillRect(this.rectTeams.x, this.rectTeams.y, this.rectTeams.width, this.rectTeams.height);
			g.setColor(Color.black);
			for(int i = 0; i < this.server.model.teams.size(); i++) {
				g.drawString(this.server.model.teams.get(i).id+" "+this.server.model.teams.get(i).name+" "+this.server.model.teams.get(i).money, this.rectTeams.x, this.rectTeams.y+this.rectTeams.height*i/this.server.model.teams.size());
			}
			
			g.setColor(Color.black);
			g.drawString("Round "+this.server.model.round, minSize/10, this.getHeight()/2);
		} catch(Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void windowActivated(WindowEvent event) {}

	@Override
	public void windowClosed(WindowEvent event) {}

	@Override
	public void windowClosing(WindowEvent event) {
		this.server.destroy();
		System.exit(0);
	}

	@Override
	public void windowDeactivated(WindowEvent event) {}

	@Override
	public void windowDeiconified(WindowEvent event) {}

	@Override
	public void windowIconified(WindowEvent event) {}

	@Override
	public void windowOpened(WindowEvent event) {}
	
	@Override
	public void windowStateChanged(WindowEvent event) {
		this.repaint();
	}

	@Override
	public void windowGainedFocus(WindowEvent event) {
		this.repaint();
	}

	@Override
	public void windowLostFocus(WindowEvent event) {}
	
	public static void main(String[] args) {
		new ServerDisplay();
	}
}
