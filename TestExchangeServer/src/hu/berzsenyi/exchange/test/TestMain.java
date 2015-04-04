package hu.berzsenyi.exchange.test;

import hu.berzsenyi.exchange.server.ui.ServerExchangeImpl;

public class TestMain extends ServerExchangeImpl {

	private static final long serialVersionUID = -8108134490680475176L;

	public static void main(String[] args) {
		new TestMain();
	}

	public TestMain() {
		super(true);
		while (true) {
			testBtnNewEvent.doClick();
			try {
				Thread.sleep(3000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}

	}

}
