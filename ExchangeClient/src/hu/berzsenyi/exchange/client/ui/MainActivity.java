package hu.berzsenyi.exchange.client.ui;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Formatter;

import com.example.android.common.view.SlidingTabLayout;

import hu.berzsenyi.exchange.client.R;
import hu.berzsenyi.exchange.client.game.ClientExchange;
import hu.berzsenyi.exchange.client.game.ClientExchange.IClientExchangeListener;
import hu.berzsenyi.exchange.game.Offer;
import hu.berzsenyi.exchange.game.Stock;
import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
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
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends ActionBarActivity {

	protected static final DecimalFormat DECIMAL_FORMAT = new DecimalFormat(
			"#0.00");

	private static final int[] LABEL_IDS_INDIRECT = {
			R.string.main_tab_label_newsfeed, R.string.main_tab_label_stocks,
			R.string.main_tab_label_exchange, R.string.main_tab_label_stats };
	private static final int[] LABEL_IDS_DIRECT = {
			R.string.main_tab_label_newsfeed, R.string.main_tab_label_stocks,
			R.string.main_tab_label_outgoing, R.string.main_tab_label_incoming,
			R.string.main_tab_label_stats };

	private static final int POSITION_INDIRECT_TAB_NEWS_FEED = 0,
			POSITION_INDIRECT_TAB_STOCKS = 1,
			POSITION_INDIRECT_TAB_EXCHANGE = 2,
			POSITION_INDIRECT_TAB_STATS = 3;

	private static final int POSITION_DIRECT_TAB_NEWS_FEED = 0,
			POSITION_DIRECT_TAB_STOCKS = 1, POSITION_DIRECT_TAB_OUTGOING = 2,
			POSITION_DIRECT_TAB_INCOMING = 3, POSITION_DIRECT_TAB_STATS = 4;

	private static final int POSITION_INDIRECT_OFFER_TYPE_SELL = 1;

	private ClientExchange mClient = ClientExchange.INSTANCE;
	private NewsAdapter mNewsAdapter;
	private OutgoingOfferAdapter mOutgoingOfferAdapter, mIncomingOfferAdapter;
	private NewOfferStockAdapter mNewOfferStockAdapter;
	private StockAdapter mStockAdapter;
	private TextView moneyTextView1, stocksValueTextView1, moneyTextView2,
			stocksValueTextView2;
	private PlayerAdapter mPlayerAdapter;

	private ProgressDialog mSendingOfferDialog;

	private IClientExchangeListener mListener = new IClientExchangeListener() {

		@Override
		public void onTradeDirect(ClientExchange exchange, String partner,
				int stockId, int amount, double price, boolean sold) {
			// TODO write out partner's name?
			onTradeIndirect(exchange, stockId, amount, price, sold);
		}

		@Override
		public void onTradeIndirect(ClientExchange exchange, final int stockId,
				final int amount, final double price, final boolean sold) {
			runOnUiThread(new Runnable() {
				@Override
				public void run() {
					if (sold)
						Toast.makeText(
								getApplicationContext(),
								getString(R.string.toast_trade_sell_0)
										+ amount
										+ getString(R.string.toast_trade_sell_1)
										+ mClient.getStockName(stockId)
										+ getString(R.string.toast_trade_sell_2)
										+ price
										+ getString(R.string.toast_trade_sell_3),
								Toast.LENGTH_LONG).show();
					else
						Toast.makeText(
								getApplicationContext(),
								getString(R.string.toast_trade_buy_0) + amount
										+ getString(R.string.toast_trade_buy_1)
										+ mClient.getStockName(stockId)
										+ getString(R.string.toast_trade_buy_2)
										+ price
										+ getString(R.string.toast_trade_buy_3),
								Toast.LENGTH_LONG).show();
				}
			});
		}

		@Override
		public void onStocksChanged(ClientExchange exchange) {
			runOnUiThread(new Runnable() {
				@Override
				public void run() {

					if (mStockAdapter != null)
						mStockAdapter.notifyDataSetChanged();
					if (mNewOfferStockAdapter != null)
						mNewOfferStockAdapter.notifyDataSetChanged();
					updateStocksValueTextViews();
				}
			});
		}

		@Override
		public void onShowBuy(ClientExchange exchange) {
			runOnUiThread(new Runnable() {

				@Override
				public void run() {
					startActivity(new Intent(MainActivity.this,
							StockBuyActivity.class));
				}
			});
		}

		@Override
		public void onSentOfferRefused(ClientExchange exchange) {
			runOnUiThread(new Runnable() {
				@Override
				public void run() {
					mSendingOfferDialog.dismiss();
					new AlertDialog.Builder(MainActivity.this)
							.setMessage(R.string.not_enough_money_or_stock)
							.setPositiveButton(R.string.ok, null).create()
							.show();
				}
			});
		}

		@Override
		public void onSentOfferAccepted(ClientExchange exchange) {
			runOnUiThread(new Runnable() {
				@Override
				public void run() {
					mSendingOfferDialog.dismiss();
					mOutgoingOfferAdapter.notifyDataSetChanged();
				}
			});
		}

		@Override
		public void onOfferCame(ClientExchange exchange) {
			// TODO Auto-generated method stub

		}

		@Override
		public void onMyStocksChanged(ClientExchange exchange) {

			runOnUiThread(new Runnable() {
				@Override
				public void run() {
					if (mStockAdapter != null)
						mStockAdapter.notifyDataSetChanged();
					updateStocksValueTextViews();
				}
			});
		}

		@Override
		public void onMyMoneyChanged(ClientExchange exchange) {
			runOnUiThread(new Runnable() {

				@Override
				public void run() {

					updateMoneyTextViews();
				}
			});
		}

		@Override
		public void onConnRefused(ClientExchange exchange) {
		}

		@Override
		public void onConnLost(ClientExchange exchange) {
			runOnUiThread(new Runnable() {
				@Override
				public void run() {
					new AlertDialog.Builder(MainActivity.this)
							.setMessage(R.string.connection_lost)
							.setPositiveButton(R.string.ok,
									new DialogInterface.OnClickListener() {

										@Override
										public void onClick(
												DialogInterface dialog,
												int which) {
											finish();
										}

									}).create().show();
				}
			});
		}

		@Override
		public void onConnAccepted(ClientExchange exchange) {
		}

		@Override
		public void onBuyRefused(ClientExchange exchange) {
		}

		@Override
		public void onBuyEnd(ClientExchange exchange) {
		}

		@Override
		public void onBuyAccepted(ClientExchange exchange) {
		}

		@Override
		public void onEventsChanged(ClientExchange exchange) {
			runOnUiThread(new Runnable() {

				@Override
				public void run() {
					mNewsAdapter.notifyDataSetChanged();
				}
			});
		}

		@Override
		public void onPlayersChanged(ClientExchange exchange) {
			runOnUiThread(new Runnable() {

				@Override
				public void run() {
					mPlayerAdapter.notifyDataSetChanged();
				}
			});
		}

		@Override
		public void onOfferDeleted(ClientExchange exchange) {
			runOnUiThread(new Runnable() {
				@Override
				public void run() {
					mOutgoingOfferAdapter.notifyDataSetChanged();
				}
			});
		}
	};

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

		mClient.addListener(mListener);
		if (mClient.isBuyRequested()) {

			startActivity(new Intent(MainActivity.this, StockBuyActivity.class));
		}
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		mClient.removeListener(mListener);
	}

	@TargetApi(Build.VERSION_CODES.LOLLIPOP)
	private static void setElevation(View view, float elevation) {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
			view.setElevation(elevation);
	}

	private void updateMoneyTextViews() {

		Formatter formatter = new Formatter();

		String string = formatter.format(getString(R.string.action_bar_money),
				DECIMAL_FORMAT.format(mClient.getMoney())).toString();
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
				DECIMAL_FORMAT.format(mClient.getStocksValue())).toString();
		formatter.close();

		if (stocksValueTextView1 != null)
			stocksValueTextView1.setText(string);
		if (stocksValueTextView2 != null)
			stocksValueTextView2.setText(string);
	}

	private class MainPagerAdapter extends PagerAdapter {

		@Override
		public int getCount() {
			return getStringResourceArray().length;
		}

		@Override
		public boolean isViewFromObject(View view, Object object) {
			return view == object;
		}

		@Override
		public CharSequence getPageTitle(int position) {
			return getString(getStringResourceArray()[position]);
		}

		@Override
		public Object instantiateItem(ViewGroup container, int position) {
			View view = null;

			if (mClient.getGameMode() == ClientExchange.GAMEMODE_DIRECT) {
				switch (position) {
				case POSITION_DIRECT_TAB_NEWS_FEED:
					view = newsFeed(container);
					break;
				case POSITION_DIRECT_TAB_STOCKS:
					view = stocks(container);
					break;
				case POSITION_DIRECT_TAB_OUTGOING:
					view = outgoingOffers(container);
					break;
				case POSITION_DIRECT_TAB_INCOMING:
					view = incomingOffers(container);
					break;
				case POSITION_DIRECT_TAB_STATS:
					view = stats(container);
				}
			} else {
				switch (position) {
				case POSITION_INDIRECT_TAB_NEWS_FEED:
					view = newsFeed(container);
					break;
				case POSITION_INDIRECT_TAB_STOCKS:
					view = stocks(container);
					break;
				case POSITION_INDIRECT_TAB_EXCHANGE:
					view = outgoingOffers(container);
					break;
				case POSITION_INDIRECT_TAB_STATS:
					view = stats(container);
					break;
				}
			}

			if (view != null)
				container.addView(view);
			return view;
		}

		@Override
		public void destroyItem(ViewGroup container, int position, Object object) {
			container.removeView((View) object);
		}

		private int[] getStringResourceArray() {
			return mClient.getGameMode() == ClientExchange.GAMEMODE_DIRECT ? LABEL_IDS_DIRECT
					: LABEL_IDS_INDIRECT;
		}

		private View newsFeed(ViewGroup container) {
			View view = getLayoutInflater().inflate(
					R.layout.activity_main_tab_news, container, false);
			ListView newsList = (ListView) view
					.findViewById(R.id.main_tab_news_list);
			mNewsAdapter = new NewsAdapter();
			newsList.setAdapter(mNewsAdapter);
			return view;
		}

		private View stocks(ViewGroup container) {
			View view = getLayoutInflater().inflate(
					R.layout.activity_main_tab_stocks, container, false);

			ListView stockList = (ListView) view
					.findViewById(R.id.main_tab_stocks_list);
			mStockAdapter = new StockAdapter();
			stockList.setAdapter(mStockAdapter);
			return view;
		}

		private View outgoingOffers(ViewGroup container) {
			View view = getLayoutInflater().inflate(
					R.layout.activity_main_tab_exchange, container, false);

			ListView offerList = (ListView) view
					.findViewById(R.id.main_tab_exchange_list);
			mOutgoingOfferAdapter = new OutgoingOfferAdapter();
			offerList.setAdapter(mOutgoingOfferAdapter);
			offerList.setOnItemClickListener(new OnItemClickListener() {

				@Override
				public void onItemClick(final AdapterView<?> parent, View view,
						final int position, long id) {
					new AlertDialog.Builder(MainActivity.this)
							.setMessage(R.string.main_tab_exchange_cancel_offer)
							.setNegativeButton(R.string.no, null)
							.setPositiveButton(R.string.yes,
									new DialogInterface.OnClickListener() {

										@Override
										public void onClick(
												DialogInterface dialog,
												int which) {
											mClient.deleteOffer((Offer) parent
													.getItemAtPosition(position));
										}

									}).create().show();
				}
			});
			return view;
		}

		private View incomingOffers(ViewGroup container) {
			View view = getLayoutInflater().inflate(
					R.layout.activity_main_tab_exchange, container, false);

			ListView offerList = (ListView) view
					.findViewById(R.id.main_tab_exchange_list);
			mIncomingOfferAdapter = new OutgoingOfferAdapter();
			offerList.setAdapter(mIncomingOfferAdapter);
			offerList.setOnItemClickListener(new OnItemClickListener() {

				@Override
				public void onItemClick(final AdapterView<?> parent, View view,
						final int position, long id) {
					new AlertDialog.Builder(MainActivity.this)
							.setMessage(R.string.main_tab_exchange_accept_offer)
							.setNegativeButton(R.string.no, null)
							.setPositiveButton(R.string.yes,
									new DialogInterface.OnClickListener() {

										@Override
										public void onClick(
												DialogInterface dialog,
												int which) {
											mClient.acceptOffer((Offer) parent
													.getItemAtPosition(position));
										}

									}).create().show();
				}
			});
			return view;
		}

		private View stats(ViewGroup container) {

			View view = getLayoutInflater().inflate(
					R.layout.activity_main_tab_stats, container, false);
			((TextView) view.findViewById(R.id.main_tab_stats_team_name))
					.setText(mClient.getName());
			moneyTextView2 = (TextView) view
					.findViewById(R.id.main_tab_stats_money);
			stocksValueTextView2 = (TextView) view
					.findViewById(R.id.main_tab_stats_stocks_value);
			updateMoneyTextViews();
			updateStocksValueTextViews();
			return view;
		}
	}

	private class StockAdapter extends BaseAdapter {

		@Override
		public int getCount() {
			return mClient.getStocksNumber() + 2;
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

	private class OutgoingOfferAdapter extends BaseAdapter {

		private Offer[] mOffers;

		public OutgoingOfferAdapter() {
			mOffers = mClient.getOutgoingOffers();
		}

		@Override
		public void notifyDataSetChanged() {
			mOffers = mClient.getOutgoingOffers();
			super.notifyDataSetChanged();
		}

		@Override
		public int getCount() {
			return mOffers.length + 1;
		}

		@Override
		public int getItemViewType(int position) {
			return position == 0 ? 0 : 1;
		}

		@Override
		public int getViewTypeCount() {
			return 2;
		}

		@Override
		public Offer getItem(int position) {
			if (position == 0)
				return null;
			return mOffers[position - 1];
		}

		@Override
		public long getItemId(int position) {
			return position - 1;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			View view;
			if (position == 0) { // New offer

				if (convertView != null)
					view = convertView;
				else {
					view = getLayoutInflater()
							.inflate(
									mClient.getGameMode() == ClientExchange.GAMEMODE_DIRECT ? R.layout.activity_main_tab_exchange_direct_new_offer_card
											: R.layout.activity_main_tab_exchange_indirect_new_offer_card,
									parent, false);

					// ================== New offer ===================
					// The View objects
					final Spinner stocks = (Spinner) view
							.findViewById(R.id.main_tab_exchange_new_offer_stock);
					final Spinner player = mClient.getGameMode() == ClientExchange.GAMEMODE_DIRECT ? (Spinner) view
							.findViewById(R.id.main_tab_exchange_new_offer_player)
							: null;
					final Spinner type = (Spinner) view
							.findViewById(R.id.main_tab_exchange_new_offer_type);
					final TextView price = (TextView) view
							.findViewById(R.id.main_tab_exchange_new_offer_price);
					final TextView amount = (TextView) view
							.findViewById(R.id.main_tab_exchange_new_offer_amount);
					Button sendButton = (Button) view
							.findViewById(R.id.main_tab_exchange_new_offer_send);

					// Type Spinner
					type.setAdapter(new ArrayAdapter<String>(MainActivity.this,
							android.R.layout.simple_spinner_dropdown_item,
							android.R.id.text1, getResources().getStringArray(
									R.array.main_tab_exchange_new_offer_type)));
					type.setOnItemSelectedListener(new OnItemSelectedListener() {

						@Override
						public void onItemSelected(AdapterView<?> parent,
								View view, int position, long id) {

							((NewOfferStockAdapter) stocks.getAdapter())
									.setSell(position == POSITION_INDIRECT_OFFER_TYPE_SELL);
						}

						@Override
						public void onNothingSelected(AdapterView<?> parent) {
						}
					});

					// Stocks Spinner
					mNewOfferStockAdapter = new NewOfferStockAdapter(false);
					stocks.setAdapter(mNewOfferStockAdapter);
					stocks.setOnItemSelectedListener(new OnItemSelectedListener() {

						@Override
						public void onItemSelected(AdapterView<?> parent,
								View view, int position, long id) {
							price.setText(DECIMAL_FORMAT.format(mClient
									.getStockPrice((int) parent.getAdapter()
											.getItemId(position))));
							amount.setText(1 + "");
						}

						@Override
						public void onNothingSelected(AdapterView<?> parent) {
						}
					});

					if (mClient.getGameMode() == ClientExchange.GAMEMODE_DIRECT) {
						// Player spinner
						mPlayerAdapter = new PlayerAdapter();
						player.setAdapter(mPlayerAdapter);
					}

					// Send Button
					sendButton.setOnClickListener(new OnClickListener() {

						@Override
						public void onClick(View v) {
							try {
								mSendingOfferDialog = ProgressDialog.show(
										MainActivity.this, null,
										getString(R.string.sending_offer),
										true, false);
								int stockId = (int) stocks.getSelectedItemId(), amountValue = Integer
										.parseInt(amount.getText().toString());
								double priceValue = NumberFormat.getInstance()
										.parse(price.getText().toString())
										.doubleValue();
								boolean sell = type.getSelectedItemPosition() == POSITION_INDIRECT_OFFER_TYPE_SELL;

								if (mClient.getGameMode() == ClientExchange.GAMEMODE_DIRECT)
									mClient.offerTo(stockId, amountValue,
											priceValue, sell,
											player.getSelectedItemPosition());
								else
									mClient.offer(stockId, amountValue,
											priceValue, sell);

							} catch (NumberFormatException e) {
								e.printStackTrace();
							} catch (ParseException e) {
								e.printStackTrace();
							}
						}
					});
				}

			} else { // Outgoing offer

				if (convertView != null)
					view = convertView;
				else
					view = getLayoutInflater().inflate(
							R.layout.activity_main_tab_exchange_card, parent,
							false);
				Offer offer = getItem(position);

				((TextView) view.findViewById(R.id.main_tab_exchange_card_type))
						.setText(offer.sell ? R.string.main_tab_exchange_offer_type_sell
								: R.string.main_tab_exchange_offer_type_buy);

				Formatter formatter = new Formatter();
				((TextView) view.findViewById(R.id.main_tab_exchange_card_name))
						.setText(formatter
								.format(getString(R.string.main_tab_exchange_offer_stock_name),
										mClient.getStockName(offer.stockId))
								.toString());
				formatter.close();

				formatter = new Formatter();
				((TextView) view
						.findViewById(R.id.main_tab_exchange_card_price))
						.setText(formatter
								.format(getString(R.string.main_tab_exchange_offer_price),
										DECIMAL_FORMAT.format(offer.price))
								.toString());
				formatter.close();

				formatter = new Formatter();
				((TextView) view
						.findViewById(R.id.main_tab_exchange_card_amount))
						.setText(formatter
								.format(getString(R.string.main_tab_exchange_offer_amount),
										offer.amount).toString());
				formatter.close();

			}
			return view;
		}
	}

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
			mNews = mClient.getEvents();
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

	private class PlayerAdapter extends BaseAdapter {

		private String[] players;
		private int myId;

		public PlayerAdapter() {
			refreshPlayers();
		}

		@Override
		public void notifyDataSetChanged() {
			refreshPlayers();
			super.notifyDataSetChanged();
		}

		private void refreshPlayers() {
			myId = -1;
			players = mClient.getPlayers();
			if (players == null || players.length < 1)
				players = new String[] { mClient.getName() };
			for (int i = 0; i < players.length; i++)
				if (players[i].equals(mClient.getName())) {
					myId = i;
					break;
				}
		}

		@Override
		public int getCount() {
			return players.length - 1;
		}

		@Override
		public String getItem(int position) {
			return position < myId ? players[position] : players[position + 1];
		}

		@Override
		public long getItemId(int position) {
			return 0;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			View view;
			if (convertView != null)
				view = convertView;
			else {
				view = getLayoutInflater().inflate(
						android.R.layout.simple_spinner_dropdown_item, parent,
						false);
			}
			((TextView) view.findViewById(android.R.id.text1))
					.setText(getItem(position));
			;
			return view;
		}

	}
}
