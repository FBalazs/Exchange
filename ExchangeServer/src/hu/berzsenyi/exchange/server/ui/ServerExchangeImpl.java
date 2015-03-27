package hu.berzsenyi.exchange.server.ui;

import hu.berzsenyi.exchange.server.game.ServerExchange;

import java.awt.Component;
import java.awt.Container;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

import javax.imageio.ImageIO;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JRadioButton;

public class ServerExchangeImpl extends JFrame implements ServerExchange.IServerExchangeListener, WindowListener, ComponentListener {
	private static final long serialVersionUID = 301475774323683701L;

	public static void main(String[] args) {
		new ServerExchangeImpl();
	}
	
	private Container contentPane;
	private Component tabStocks, tabPlayers, tabEvents;
	private JButton btnNewEvent, btnStockUpdate;
	private ButtonGroup radioGroupTabs;
	private JRadioButton radioTabStocks, radioTabPlayers, radioTabEvents;
	
	public ServerExchangeImpl() {
		super("Exchange");
		try {
			addWindowListener(this);
			addComponentListener(this);
			setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
			setSize(800, 600);
			setLocationRelativeTo(null);
			setIconImage(ImageIO.read(getClass().getResource("/hu/berzsenyi/exchange/server/res/ic_launcher.png")));
			
			contentPane = new Container();
			contentPane.setLayout(null);
			
			tabStocks = new CompTabStocks(); 
			tabPlayers = new CompTabPlayers();
			tabPlayers.setVisible(false);
			// TODO tabEvents
			
			btnNewEvent = new JButton("New event");
			btnNewEvent.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					ServerExchange.INSTANCE.newEventAndUpdateStocks();
				}
			});
			btnStockUpdate = new JButton("Update stocks");
			btnStockUpdate.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					ServerExchange.INSTANCE.updateStocks();
				}
			});
			
			radioTabStocks = new JRadioButton("Stocks");
			radioTabStocks.setSelected(true);
			radioTabStocks.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					tabStocks.setVisible(true);
					tabPlayers.setVisible(false);
					// TODO tabEvents
				}
			});
			radioTabPlayers = new JRadioButton("Players");
			radioTabPlayers.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					tabStocks.setVisible(false);
					tabPlayers.setVisible(true);
					// TODO tabEvents
				}
			});
			radioTabEvents = new JRadioButton("Events");
			radioTabEvents.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					tabStocks.setVisible(false);
					tabPlayers.setVisible(false);
					// TODO tabEvents
				}
			});
			
			radioGroupTabs = new ButtonGroup();
			radioGroupTabs.add(radioTabStocks);
			radioGroupTabs.add(radioTabPlayers);
			radioGroupTabs.add(radioTabEvents);
			
			contentPane.add(tabStocks);
			contentPane.add(tabPlayers);
			// TODO tabEvents
			
			contentPane.add(btnNewEvent);
			contentPane.add(btnStockUpdate);
			
			contentPane.add(radioTabStocks);
			contentPane.add(radioTabPlayers);
			contentPane.add(radioTabEvents);
			
			setContentPane(contentPane);
			setVisible(true);
			
			ServerExchange.INSTANCE.addListener(this);
			String[] options = new String[]{"Direct", "Indirect"};
			ServerExchange.INSTANCE.open(Integer.parseInt(JOptionPane.showInputDialog(this, "Port", "8080")),
					JOptionPane.showOptionDialog(this, "Which gamemode?", "Choose gamemode", JOptionPane.DEFAULT_OPTION, JOptionPane.QUESTION_MESSAGE, null, options, options[0]),
					Double.parseDouble(JOptionPane.showInputDialog(this, "Startmoney", "10000")));
			ServerExchange.INSTANCE.load(JOptionPane.showInputDialog(this, "Load backup from", "backup/save0.dat"));
			repaint();
		} catch(Exception e) {
			e.printStackTrace();
			System.exit(1);
		}
	}
	
	private void onResize() {
		if(contentPane == null)
			return;
		int bx = Math.min(contentPane.getWidth(), contentPane.getHeight())/20;
		int by = bx;
		int width = contentPane.getWidth()-2*bx;
		int height = contentPane.getHeight()-2*by;
		
		int compX = bx;
		int compY = by;
		int compWidth = width;
		int compHeight = height-by*3;
		int barX = bx;
		int barY = compY+compHeight;
		int barWidth = width;
		int barHeight = by*3;
		
		tabStocks.setBounds(compX, compY, compWidth, compHeight);
		tabPlayers.setBounds(compX, compY, compWidth, compHeight);
		
		SwingHelper.splitComponentsVertical(new Rectangle(barX, barY+by/3, barWidth/6, barHeight-by/3), 0.25, btnNewEvent, btnStockUpdate);
		SwingHelper.setButtonTextSizeToFill(btnNewEvent);
		SwingHelper.setButtonTextSizeToFill(btnStockUpdate);
		if(btnNewEvent.getFont().getSize() < btnStockUpdate.getFont().getSize())
			btnStockUpdate.setFont(btnNewEvent.getFont());
		else
			btnNewEvent.setFont(btnStockUpdate.getFont());
		
		SwingHelper.splitComponentsVertical(new Rectangle(barX+barWidth/6+bx/2, barY+by/3, barWidth/8, barHeight-by/3), 0.25,
				radioTabStocks, radioTabPlayers, radioTabEvents);
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
		onResize();
		repaint();
	}

	@Override
	public void onEvent(ServerExchange exchange) {
		repaint();
	}
	
	@Override
	public void onMsgReceived(ServerExchange exchange) {
		repaint();
	}

	@Override
	public void onClosed(ServerExchange exchange) {
		System.exit(0);
	}
	
	@Override
	public void componentShown(ComponentEvent event) {
		
	}

	@Override
	public void componentHidden(ComponentEvent event) {
		
	}

	@Override
	public void componentMoved(ComponentEvent event) {
		
	}

	@Override
	public void componentResized(ComponentEvent event) {
		onResize();
		repaint();
	}
}
