package hu.berzsenyi.exchange.server;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowFocusListener;
import java.awt.event.WindowListener;
import java.awt.event.WindowStateListener;
import java.io.IOException;
import java.text.DecimalFormat;

import javax.imageio.ImageIO;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JRadioButton;

public class ServerDisplay extends JFrame implements WindowListener,
		WindowStateListener, WindowFocusListener, IServerDisplay {
	private static final long serialVersionUID = 8256191100104297255L;
	protected static final DecimalFormat DECIMAL_FORMAT = new DecimalFormat(
			"#0.00");

	public ExchangeServer server;

	public int width = 1280, height = 720;

	public CompStocks compStocks;
	public CompTeams compTeams;
	public JButton btnNextRound;
	public JRadioButton radioStocks, radioTeams;
	public JLabel labelRound, labelEvent;

	public void radioStocks() {
		this.compStocks.setVisible(true);
		this.compTeams.setVisible(false);
	}

	public void radioTeams() {
		this.compStocks.setVisible(false);
		this.compTeams.setVisible(true);
	}

	@Override
	public void onRoundBegin(int round) {
		this.labelRound.setText("Round: " + round);
		// this.labelEvent.setText(this.server.model.eventMessage);
		// TODO
	}

	@Override
	public void onRoundEnd(int round) {

	}

	public ServerDisplay() {
		super("Exchange Server");
		
		this.server = new ExchangeServer();
		this.server.setDisplay(this);
		this.server.create();
		
		try {
			this.setIconImage(ImageIO.read(getClass().getResource("/hu/berzsenyi/exchange/server/res/ic_launcher.png")));
		} catch (IOException e) {
			e.printStackTrace();
		}

		this.setSize(this.width, this.height);
		this.setLayout(null);
		this.setLocationRelativeTo(null);
		this.setBackground(Color.lightGray);

		this.compStocks = new CompStocks(this);
		this.add(this.compStocks);

		this.compTeams = new CompTeams(this);
		this.compTeams.setVisible(false);
		this.add(this.compTeams);

		this.btnNextRound = new JButton("Next Round");
		this.btnNextRound.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				ServerDisplay.this.server.nextRound();
			}
		});
		this.add(this.btnNextRound);

		this.radioStocks = new JRadioButton("Stocks", true);
		this.radioStocks.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				ServerDisplay.this.radioStocks();
			}
		});
		this.add(this.radioStocks);

		this.radioTeams = new JRadioButton("Teams", false);
		// this.radioTeams.setEnabled(false);
		this.radioTeams.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				ServerDisplay.this.radioTeams();
			}
		});
		this.add(this.radioTeams);

		ButtonGroup bg = new ButtonGroup();
		bg.add(this.radioStocks);
		bg.add(this.radioTeams);

		this.labelRound = new JLabel("Round: 0");
		this.add(this.labelRound);

		this.labelEvent = new JLabel("Eventdesc");
		this.add(this.labelEvent);

		this.onResize();

		this.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		this.addWindowListener(this);
		this.addWindowStateListener(this);
		this.addWindowFocusListener(this);
		this.setVisible(true);
		
		String saveFile = JOptionPane.showInputDialog(this, "File to load:");
		Backup.load(this.server, saveFile);
	}

	public void onResize() {
		int minSize = Math.min(this.width, this.height);
		this.compStocks.setBounds(minSize / 10, minSize / 10, this.width
				- minSize / 5, this.height - minSize / 3);
		this.compTeams.setBounds(this.compStocks.getBounds());
		this.btnNextRound.setBounds(this.compStocks.getX(),
				this.compStocks.getY() + this.compStocks.getHeight() + minSize
						/ 20, minSize / 5, minSize / 15);
		this.radioStocks.setBounds(
				this.btnNextRound.getX() + this.btnNextRound.getWidth()
						+ minSize / 20, this.btnNextRound.getY(), minSize / 5,
				minSize / 50);
		this.radioTeams.setBounds(this.radioStocks.getX(),
				this.radioStocks.getY() + this.radioStocks.getHeight(),
				radioStocks.getWidth(), radioStocks.getHeight());
		this.labelRound.setBounds(
				this.radioStocks.getX() + this.radioStocks.getWidth(),
				this.radioStocks.getY(), minSize, this.radioStocks.getHeight());
		this.labelEvent.setBounds(
				this.radioTeams.getX() + this.radioTeams.getWidth(),
				this.radioTeams.getY(), minSize, this.radioTeams.getHeight());
	}

	@Override
	public void paint(Graphics g) {
		if (this.getWidth() != this.width || this.getHeight() != this.height) {
			this.width = this.getWidth();
			this.height = this.getHeight();
			this.onResize();
		}
		super.paint(g);
	}

	@Override
	public void windowActivated(WindowEvent event) {
	}

	@Override
	public void windowClosed(WindowEvent event) {
	}

	@Override
	public void windowClosing(WindowEvent event) {
		this.server.destroy();
		System.exit(0);
	}

	@Override
	public void windowDeactivated(WindowEvent event) {
	}

	@Override
	public void windowDeiconified(WindowEvent event) {
	}

	@Override
	public void windowIconified(WindowEvent event) {
	}

	@Override
	public void windowOpened(WindowEvent event) {
	}

	@Override
	public void windowStateChanged(WindowEvent event) {
		this.repaint();
	}

	@Override
	public void windowGainedFocus(WindowEvent event) {
		this.repaint();
	}

	@Override
	public void windowLostFocus(WindowEvent event) {
	}

	public static void main(String[] args) {
		new ServerDisplay();
	}
}
