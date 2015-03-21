package hu.berzsenyi.exchange.server.game;

import hu.berzsenyi.exchange.game.ExchangeGame;
import hu.berzsenyi.exchange.game.Player;

public class ServerPlayer extends Player {
	public String netId;

	public ServerPlayer(ExchangeGame game, String name, String password,
			String netId) {
		super(game, name, password);
		this.netId = netId;
		setMoney(ServerExchangeGame.INSTANCE.startMoney);
		setStockAmounts(new int[ServerExchangeGame.INSTANCE.stocks.length]);
	}
}
