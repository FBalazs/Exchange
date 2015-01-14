package hu.berzsenyi.exchange.server;

public interface IServerDisplay {
	public void onRoundBegin(int round);
	public void onRoundEnd(int round);
	public void repaint();
}
