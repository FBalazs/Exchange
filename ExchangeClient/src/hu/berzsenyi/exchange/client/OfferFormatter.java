package hu.berzsenyi.exchange.client;

import hu.berzsenyi.exchange.net.cmd.CmdOffer;

import java.text.DecimalFormat;
import java.util.Formatter;

import android.content.Context;

public class OfferFormatter {

	private Context mContext;
	private ExchangeClient mClient;

	public OfferFormatter(Context context, ExchangeClient client) {
		mContext = context;
		mClient = client;
	}

	public String toString(CmdOffer offer) {

		Formatter formatter = new Formatter();
		DecimalFormat decimalFormat = new DecimalFormat("#0.00");
		if (offer.amount < 0)
			formatter.format(mContext.getString(R.string.offer_as_string_sell),
					mClient.getModel().getTeamById(offer.teamID).name,
					mClient.getModel().stockList[offer.stockID].name,
					-offer.amount, decimalFormat.format(offer.money));
		else
			formatter.format(mContext.getString(R.string.offer_as_string_buy),
					mClient.getModel().getTeamById(offer.teamID).name,
					mClient.getModel().stockList[offer.stockID].name,
					offer.amount, decimalFormat.format(-offer.money));
		String out = formatter.toString();
		formatter.close();
		return out;
	}

}
