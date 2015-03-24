package hu.berzsenyi.exchange.client.ui;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Formatter;

import com.example.android.common.view.SlidingTabLayout;

import hu.berzsenyi.exchange.client.R;
import hu.berzsenyi.exchange.client.game.ClientExchange;
import hu.berzsenyi.exchange.game.Stock;
import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

public class MainActivity extends ActionBarActivity {

	protected static final DecimalFormat DECIMAL_FORMAT = new DecimalFormat(
			"#0.00");

	private static final int[] LABEL_IDS = { R.string.main_tab_label_newsfeed,
			R.string.main_tab_label_stocks, R.string.main_tab_label_exchange,
			R.string.main_tab_label_stats };

	private static final int POSITION_TAB_NEWS_FEED = 0,
			POSITION_TAB_STOCKS = 1, POSITION_TAB_EXCHANGE = 2,
			POSITION_TAB_STATS = 3;

	private static final int POSITION_OFFER_TYPE_SELL = 1;

	private ClientExchange mClient = ClientExchange.INSTANCE;
	private NewsAdapter mNewsAdapter;
	private OutgoingOfferAdapter mOutgoingOfferAdapter;
	private NewOfferStockAdapter mNewOfferStockAdapter;
	private StockAdapter mStockAdapter;
	private TextView moneyTextView1, stocksValueTextView1, moneyTextView2,
			stocksValueTextView2;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_main);

		ActionBar actionBar = getSupportActionBar();
		actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_HOME
				| ActionBar.DISPLAY_SHOW_CUSTOM);
		actionBar.setIcon(R.drawable.ic_launcher);
		actionBar.setElevation(0);

		actionBar.setCustomView(R.layout.action_bar);
		View customView = actionBar.getCustomView();
		((TextView) customView.findViewById(R.id.action_bar_title))
				.setText(R.string.app_name);
		moneyTextView1 = (TextView) customView
				.findViewById(R.id.action_bar_money);
		stocksValueTextView1 = (TextView) customView
				.findViewById(R.id.action_bar_stocks_value);

		ViewPager viewPager = (ViewPager) findViewById(R.id.viewPager);
		viewPager.setAdapter(new MainPagerAdapter());

		SlidingTabLayout slidingTabLayout = (SlidingTabLayout) findViewById(R.id.slidingTabLayout);
		slidingTabLayout.setCustomTabView(R.layout.activity_main_tab_label,
				R.id.tab_label);
		slidingTabLayout.setViewPager(viewPager);
		slidingTabLayout.setSelectedIndicatorColors(getResources().getColor(
				R.color.tabIndicator));
		setElevation(slidingTabLayout,
				getResources().getDimension(R.dimen.actionBar_elevation));
	}

	@TargetApi(Build.VERSION_CODES.LOLLIPOP)
	private static void setElevation(View view, float elevation) {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
			view.setElevation(elevation);
	}

	private void updateMoneyTextViews() {

		Formatter formatter = new Formatter();

		String string = formatter.format(getString(R.string.action_bar_money),
				DECIMAL_FORMAT.format(mClient.getMoney()))
				.toString();
		formatter.close();

		if (moneyTextView1 != null)
			moneyTextView1.setText(string);
		if (moneyTextView2 != null)
			moneyTextView2.setText(string);
	}

	private void updateStocksValueTextViews() {

		Formatter formatter = new Formatter();
		String string = formatter.format(
				getString(R.string.action_bar_stocks_value),
				DECIMAL_FORMAT.format(mClient.getOwnTeam()
						.calculateStocksValue())).toString();
		formatter.close();

		if (stocksValueTextView1 != null)
			stocksValueTextView1.setText(string);
		if (stocksValueTextView2 != null)
			stocksValueTextView2.setText(string);
	}

	private class MainPagerAdapter extends PagerAdapter {

		@Override
		public int getCount() {
			return LABEL_IDS.length;
		}

		@Override
		public boolean isViewFromObject(View view, Object object) {
			return view == object;
		}

		@Override
		public CharSequence getPageTitle(int position) {
			return getString(LABEL_IDS[position]);
		}

		@Override
		public Object instantiateItem(ViewGroup container, int position) {
			View view = null;

			switch (position) {
			case POSITION_TAB_NEWS_FEED:
				view = getLayoutInflater().inflate(
						R.layout.activity_main_tab_news, container, false);
				ListView newsList = (ListView) view
						.findViewById(R.id.main_tab_news_list);
				mNewsAdapter = new NewsAdapter();
				newsList.setAdapter(mNewsAdapter);
				break;
			case POSITION_TAB_STOCKS:
				view = getLayoutInflater().inflate(
						R.layout.activity_main_tab_stocks, container, false);

				ListView stockList = (ListView) view
						.findViewById(R.id.main_tab_stocks_list);
				mStockAdapter = new StockAdapter();
				stockList.setAdapter(mStockAdapter);
				break;
			case POSITION_TAB_EXCHANGE:
				view = getLayoutInflater().inflate(
						R.layout.activity_main_tab_exchange, container, false);

				ListView offerList = (ListView) view
						.findViewById(R.id.main_tab_exchange_list);
				mOutgoingOfferAdapter = new OutgoingOfferAdapter();
				offerList.setAdapter(mOutgoingOfferAdapter);
				offerList.setOnItemClickListener(new OnItemClickListener() {

					@Override
					public void onItemClick(final AdapterView<?> parent,
							View view, final int position, long id) {
						new AlertDialog.Builder(MainActivity.this)
								.setMessage(
										R.string.main_tab_exchange_cancel_offer)
								.setNegativeButton(R.string.no, null)
								.setPositiveButton(R.string.yes,
										new DialogInterface.OnClickListener() {

											@Override
											public void onClick(
													DialogInterface dialog,
													int which) {
												mClient.deleteOffer((MsgOffer) parent
														.getItemAtPosition(position));
											}

										}).create().show();
					}
				});

				break;
			case POSITION_TAB_STATS:
				view = getLayoutInflater().inflate(
						R.layout.activity_main_tab_stats, container, false);
				((TextView) view.findViewById(R.id.main_tab_stats_team_name))
						.setText(mClient.getName());
				moneyTextView2 = (TextView) view
						.findViewById(R.id.main_tab_stats_money);
				stocksValueTextView2 = (TextView) view
						.findViewById(R.id.main_tab_stats_stocks_value);
				updateMoneyTextViews();
				updateStocksValueTextViews();
				break;
			}

			if (view != null)
				container.addView(view);
			return view;
		}

		@Override
		public void destroyItem(ViewGroup container, int position, Object object) {
			container.removeView((View) object);
		}
	}

	private class StockAdapter extends BaseAdapter {

		@Override
		public int getCount() {
			return mClient.getStocksNumber() + 2;
		}

		@Override
		public void notifyDataSetChanged() {
			super.notifyDataSetChanged();
		}

		@Override
		public Stock getItem(int position) {
			// if (position == 0 || position == getCount() - 1)
			// return null;
			// return mClient.getModel().stocks[position - 1];
			return null;
		}

		@Override
		public long getItemId(int position) {
			return 0;
		}

		@Override
		public int getItemViewType(int position) {
			return position == 0 || position == getCount() - 1 ? 0 : 1;
		}

		@Override
		public int getViewTypeCount() {
			return 2;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			View view;
			if (position == 0 || position == getCount() - 1) {
				if (convertView != null)
					return convertView;
				else {
					view = new View(MainActivity.this);
					view.setLayoutParams(new ListView.LayoutParams(
							ViewGroup.LayoutParams.MATCH_PARENT, getResources()
									.getDimensionPixelSize(
											R.dimen.cardView_margin_vertical)));
					return view;
				}
			}

			if (convertView == null)
				view = getLayoutInflater().inflate(
						R.layout.activity_main_tab_stocks_card, parent, false);
			else
				view = convertView;

			// Stock stock = getItem(position);
			int stockId = position - 1;
			double change = 1.0; // TODO Stock change!!!
			int circulated = -1;

			Formatter formatter = new Formatter();
			DecimalFormat df = new DecimalFormat("+#0.00;-#");

			String start = formatter.format(
					getString(R.string.main_tab_stocks_currency_start),
					DECIMAL_FORMAT.format(mClient.getStockPrice(stockId)))
					.toString();
			formatter.close();
			String middle = df.format((change - 1.0) * 100) + "%";
			String end = getString(R.string.main_tab_stocks_currency_end);

			Spannable spannable = new SpannableString(start + middle + end);

			int colorID;
			if (change > 1) {
				colorID = R.color.stock_change_increase;
			} else if (change == 1) {
				colorID = R.color.stock_change_stagnation;
			} else {
				colorID = R.color.stock_change_decrease;
			}
			spannable.setSpan(
					new ForegroundColorSpan(getResources().getColor(colorID)),
					start.length(), start.length() + middle.length(),
					Spannable.SPAN_EXCLUSIVE_INCLUSIVE);

			((TextView) view.findViewById(R.id.main_tab_stocks_card_name))
					.setText(mClient.getStockName(stockId));
			((TextView) view.findViewById(R.id.main_tab_stocks_card_value))
					.setText(spannable);
			formatter = new Formatter();
			((TextView) view.findViewById(R.id.main_tab_stocks_card_circulated))
					.setText(formatter.format(
							getString(R.string.main_tab_stocks_circulated),
							circulated).toString());
			formatter.close();
			formatter = new Formatter();
			((TextView) view.findViewById(R.id.main_tab_stocks_card_possessed))
					.setText(formatter.format(
							getString(R.string.main_tab_stocks_possessed),
							mClient.getStockAmount(position - 1)).toString());
			formatter.close();

			return view;
		}
	}

	/*
	 * private class OutgoingOfferAdapter extends BaseAdapter {
	 * 
	 * private MsgOffer[] mOffers;
	 * 
	 * public OutgoingOfferAdapter() { init(); }
	 * 
	 * private void init() { mOffers = mClient.getOutgoingOffers(); }
	 * 
	 * @Override public void notifyDataSetChanged() { init();
	 * super.notifyDataSetChanged(); }
	 * 
	 * @Override public int getCount() { return mOffers.length + 1; }
	 * 
	 * @Override public int getItemViewType(int position) { return position == 0
	 * ? 0 : 1; }
	 * 
	 * @Override public int getViewTypeCount() { return 2; }
	 * 
	 * @Override public MsgOffer getItem(int position) { if (position == 0)
	 * return null; return mOffers[position - 1]; }
	 * 
	 * @Override public long getItemId(int position) { return position - 1; }
	 * 
	 * @Override public View getView(int position, View convertView, ViewGroup
	 * parent) { View view; if (position == 0) { // New offer
	 * 
	 * if (convertView != null) view = convertView; else { view =
	 * getLayoutInflater().inflate(
	 * R.layout.activity_main_tab_exchange_new_offer_card, parent, false);
	 * 
	 * // ================== New offer =================== // The View objects
	 * final Spinner stocks = (Spinner) view
	 * .findViewById(R.id.main_tab_exchange_new_offer_stock); final Spinner type
	 * = (Spinner) view .findViewById(R.id.main_tab_exchange_new_offer_type);
	 * final TextView price = (TextView) view
	 * .findViewById(R.id.main_tab_exchange_new_offer_price); final TextView
	 * amount = (TextView) view
	 * .findViewById(R.id.main_tab_exchange_new_offer_amount); Button sendButton
	 * = (Button) view .findViewById(R.id.main_tab_exchange_new_offer_send);
	 * 
	 * // Type Spinner type.setAdapter(new
	 * ArrayAdapter<String>(ActivityMain.this,
	 * android.R.layout.simple_spinner_dropdown_item, android.R.id.text1,
	 * getResources().getStringArray(
	 * R.array.main_tab_exchange_new_offer_type)));
	 * type.setOnItemSelectedListener(new OnItemSelectedListener() {
	 * 
	 * @Override public void onItemSelected(AdapterView<?> parent, View view,
	 * int position, long id) {
	 * 
	 * ((NewOfferStockAdapter) stocks.getAdapter()) .setSell(position ==
	 * POSITION_OFFER_TYPE_SELL); }
	 * 
	 * @Override public void onNothingSelected(AdapterView<?> parent) { } });
	 * 
	 * // Stocks Spinner mNewOfferStockAdapter = new
	 * NewOfferStockAdapter(false); stocks.setAdapter(mNewOfferStockAdapter);
	 * stocks.setOnItemSelectedListener(new OnItemSelectedListener() {
	 * 
	 * @Override public void onItemSelected(AdapterView<?> parent, View view,
	 * int position, long id) { price.setText(DECIMAL_FORMAT.format(((Stock)
	 * parent .getAdapter().getItem(position)).value)); amount.setText(1 + "");
	 * }
	 * 
	 * @Override public void onNothingSelected(AdapterView<?> parent) { } });
	 * 
	 * // Send Button sendButton.setOnClickListener(new OnClickListener() {
	 * 
	 * @Override public void onClick(View v) { try { mClient.sendOffer( (int)
	 * stocks.getSelectedItemId(), Integer.parseInt(amount.getText()
	 * .toString()), NumberFormat .getInstance() .parse(price.getText()
	 * .toString()) .doubleValue(), type.getSelectedItemPosition() ==
	 * POSITION_OFFER_TYPE_SELL); } catch (NumberFormatException e) {
	 * e.printStackTrace(); } catch (ParseException e) { e.printStackTrace(); }
	 * } }); }
	 * 
	 * } else { // Outgoing offer
	 * 
	 * if (convertView != null) view = convertView; else view =
	 * getLayoutInflater().inflate( R.layout.activity_main_tab_exchange_card,
	 * parent, false); MsgOffer offer = getItem(position);
	 * 
	 * ((TextView) view.findViewById(R.id.main_tab_exchange_card_type))
	 * .setText(offer.sell ? R.string.main_tab_exchange_offer_type_sell :
	 * R.string.main_tab_exchange_offer_type_buy);
	 * 
	 * Formatter formatter = new Formatter(); ((TextView)
	 * view.findViewById(R.id.main_tab_exchange_card_name)) .setText(formatter
	 * .format(getString(R.string.main_tab_exchange_offer_stock_name),
	 * mClient.getModel().stocks[offer.stockId].name) .toString());
	 * formatter.close();
	 * 
	 * formatter = new Formatter(); ((TextView) view
	 * .findViewById(R.id.main_tab_exchange_card_price)) .setText(formatter
	 * .format(getString(R.string.main_tab_exchange_offer_price),
	 * DECIMAL_FORMAT.format(offer.price)) .toString()); formatter.close();
	 * 
	 * formatter = new Formatter(); ((TextView) view
	 * .findViewById(R.id.main_tab_exchange_card_amount)) .setText(formatter
	 * .format(getString(R.string.main_tab_exchange_offer_amount),
	 * offer.stockAmount).toString()); formatter.close();
	 * 
	 * } return view; } }
	 */

	private class NewsAdapter extends BaseAdapter {

		private String[] mNews;

		public NewsAdapter() {
			init();
		}

		@Override
		public void notifyDataSetChanged() {
			init();
			super.notifyDataSetChanged();
		}

		private void init() {
			SingleEvent[] events = mClient.getEvents();
			mNews = new String[events.length];
			for (int i = 0; i < events.length; i++)
				mNews[i] = events[i].getDescription();
		}

		@Override
		public int getCount() {
			return mNews.length;
		}

		@Override
		public String getItem(int pos) {
			return mNews[pos];
		}

		@Override
		public long getItemId(int pos) {
			return 0;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			View view;
			if (convertView != null)
				view = convertView;
			else
				view = getLayoutInflater().inflate(
						R.layout.activity_main_tab_news_card, parent, false);
			((TextView) view.findViewById(R.id.main_tab_news_card_text))
					.setText(getItem(position));
			return view;
		}

	}

	private class NewOfferStockAdapter extends BaseAdapter {

		private static final int LAYOUT_ID = android.R.layout.simple_spinner_dropdown_item;
		private static final int TEXT_VIEW_ID = android.R.id.text1;

		private boolean mSell;
		private int[] mPossessedStocks; // The ids of possessed stocks

		public NewOfferStockAdapter(boolean sell) {
			setSell(sell);
		}

		public void setSell(boolean sell) {
			mSell = sell;
			notifyDataSetChanged();
		}

		@Override
		public void notifyDataSetChanged() {
			ArrayList<Integer> possessedStocksList = new ArrayList<Integer>();
			int length = mClient.getStocksNumber();
			for (int i = 0; i < length; i++)
				if (mClient.getStockAmount(i) > 0)
					possessedStocksList.add(Integer.valueOf(i));

			mPossessedStocks = new int[possessedStocksList.size()];
			for (int i = 0; i < possessedStocksList.size(); i++)
				mPossessedStocks[i] = possessedStocksList.get(i);

			super.notifyDataSetChanged();
		}

		@Override
		public int getCount() {
			return mSell ? mPossessedStocks.length : mClient.getStocksNumber();
		}

		@Override
		public Stock getItem(int position) {
			// return mClient.getModel().stocks[(int) getItemId(position)];
			return null;
		}

		@Override
		public long getItemId(int position) {
			return mSell ? mPossessedStocks[position] : position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			View view;
			if (convertView != null) {
				view = convertView;
			} else {
				view = getLayoutInflater().inflate(LAYOUT_ID, parent, false);
			}
			((TextView) view.findViewById(TEXT_VIEW_ID)).setText(mClient
					.getStockName((int) getItemId(position)));
			return view;
		}

	}
}
