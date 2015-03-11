package hu.berzsenyi.exchange.client;

import hu.berzsenyi.exchange.SingleEvent;
import hu.berzsenyi.exchange.Stock;
import hu.berzsenyi.exchange.Team;
import hu.berzsenyi.exchange.net.TCPClient;
import hu.berzsenyi.exchange.net.msg.MsgOffer;

import java.io.IOException;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Formatter;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
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

import com.example.android.common.view.SlidingTabLayout;

public class ActivityMain extends ActionBarActivity {

	protected static final DecimalFormat DECIMAL_FORMAT = new DecimalFormat(
			"#0.00");

	private static final int[] LABEL_IDS = { R.string.main_tab_label_newsfeed,
			R.string.main_tab_label_stocks, R.string.main_tab_label_exchange,
			R.string.main_tab_label_stats };
	private static final int POSITION_TAB_NEWS_FEED = 0,
			POSITION_TAB_STOCKS = 1, POSITION_TAB_EXCHANGE = 2;
			//POSITION_TAB_STATS = 3;
	private static final int POSITION_OFFER_TYPE_SELL = 1;
	// POSITION_OFFER_TYPE_BUY = 0,

	private ExchangeClient mClient;
	private boolean mZerothRoundDone = false, mZerothRoundStarted = false;
	private NewsAdapter mNewsAdapter;
	private OutgoingOfferAdapter mOutgoingOfferAdapter;
	private NewOfferStockAdapter mNewOfferStockAdapter;
	private StockAdapter mStockAdapter;
	private TextView moneyTextView, stocksValueTextView;
	private IClientListener mListener = new IClientListener() {

		@Override
		public void onConnectionFail(TCPClient client, IOException exception) {
		}

		@Override
		public void onConnect(TCPClient client) {
		}

		@Override
		public void onClose(TCPClient client) {
			if(!isFinishing())
				runOnUiThread(new Runnable() {
					public void run() {
						new AlertDialog.Builder(ActivityMain.this)
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
		public void onTeamsCommand(ExchangeClient client) {
			
		}

		@Override
		public void onStocksCommand(ExchangeClient client) {
			runOnUiThread(new Runnable() {
				@Override
				public void run() {
					if (mStockAdapter != null)
						mStockAdapter.notifyDataSetChanged();
					if (mNewOfferStockAdapter != null)
						mNewOfferStockAdapter.notifyDataSetChanged();
					updateStocksValueTextView();
				}
			});
		}

		@Override
		public void onStocksChanged(Team ownTeam, int position) {
			runOnUiThread(new Runnable() {
				@Override
				public void run() {

					if (mStockAdapter != null)
						mStockAdapter.notifyDataSetChanged();
					updateStocksValueTextView();
				}
			});
		}

		@Override
		public void onNewRound(SingleEvent[] event) {
			runOnUiThread(new Runnable() {
				public void run() {
					if (mNewsAdapter != null)
						mNewsAdapter.notifyDataSetChanged();
				}
			});
		}

		@Override
		public void onOutgoingOffersChanged() {
			runOnUiThread(new Runnable() {
				@Override
				public void run() {
					mOutgoingOfferAdapter.notifyDataSetChanged();
				}
			});
		}

		@Override
		public void onMoneyChanged(Team ownTeam) {
			runOnUiThread(new Runnable() {

				@Override
				public void run() {

					updateMoneyTextView();
				}
			});

		}

		@Override
		public void onOfferFailed() {
			new AlertDialog.Builder(getApplicationContext()).setMessage("You already have an offer for this stock!").setNeutralButton("OK", null).create().show();
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		mClient = ExchangeClient.getInstance();

		ActionBar actionBar = getSupportActionBar();
		actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_HOME
				| ActionBar.DISPLAY_SHOW_CUSTOM);
		actionBar.setIcon(R.drawable.ic_launcher);
		actionBar.setElevation(0);

		actionBar.setCustomView(R.layout.action_bar);
		View customView = actionBar.getCustomView();
		((TextView) customView.findViewById(R.id.action_bar_title))
				.setText(R.string.app_name);
		moneyTextView = (TextView) customView
				.findViewById(R.id.action_bar_money);
		stocksValueTextView = (TextView) customView
				.findViewById(R.id.action_bar_stocks_value);

		updateMoneyTextView();
		updateStocksValueTextView();

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

	@Override
	protected void onDestroy() {
		mClient.removeIClientListener(mListener);
		mClient.disconnect();
		super.onDestroy();
	}

	@TargetApi(Build.VERSION_CODES.LOLLIPOP)
	private static void setElevation(View view, float elevation) {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
			view.setElevation(elevation);
	}

	@Override
	protected void onStart() {
		super.onStart();
		if (mZerothRoundStarted && !mZerothRoundDone) { // ActivityZerothRound
														// was cancelled
			finish();
		} else if (!mZerothRoundStarted && mClient.isInZerothRound()) {
			startActivityForResult(new Intent(this, ActivityZerothRound.class),
					ActivityZerothRound.REQUEST_CODE);
			mZerothRoundStarted = true;
		} else { // Now NewActivityMain is really shown

			mClient.addIClientListener(mListener);
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == ActivityZerothRound.REQUEST_CODE) {
			mZerothRoundDone = resultCode == Activity.RESULT_OK;
		}
	}

	@Override
	public void onBackPressed() {
		new AlertDialog.Builder(this)
				.setTitle(R.string.exit_question)
				.setMessage(R.string.exit_message)
				.setPositiveButton(R.string.yes,
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								ActivityMain.super.onBackPressed();
							}
						})
				.setNegativeButton(R.string.no,
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog,
									int which) {

							}
						}).create().show();
	}

	private void updateMoneyTextView() {
		if (mClient.getOwnTeam() == null)
			return;

		Formatter formatter = new Formatter();

		moneyTextView.setText(formatter.format(
				getString(R.string.action_bar_money),
				DECIMAL_FORMAT.format(mClient.getOwnTeam().getMoney()))
				.toString());
		formatter.close();
	}

	private void updateStocksValueTextView() {
		if (mClient.getOwnTeam() == null)
			return;

		Formatter formatter = new Formatter();
		stocksValueTextView.setText(formatter.format(
				getString(R.string.action_bar_stocks_value),
				DECIMAL_FORMAT.format(mClient.getOwnTeam()
						.calculateStocksValue())).toString());
		formatter.close();
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
			View view;

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
						new AlertDialog.Builder(ActivityMain.this)
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
			default:
				view = getLayoutInflater().inflate(R.layout.eraseme, container,
						false);
			}

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
			return mClient.getModel().stocks.length + 2;
		}
		
		@Override
		public void notifyDataSetChanged() {
			super.notifyDataSetChanged();
		}

		@Override
		public Stock getItem(int position) {
			if (position == 0 || position == getCount() - 1)
				return null;
			return mClient.getModel().stocks[position - 1];
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
					view = new View(ActivityMain.this);
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

			Stock stock = getItem(position);

			Formatter formatter = new Formatter();
			DecimalFormat df = new DecimalFormat("+#0.00;-#");

			String start = formatter.format(
					getString(R.string.main_tab_stocks_currency_start),
					DECIMAL_FORMAT.format(stock.value)).toString();
			formatter.close();
			String middle = df.format((stock.change - 1.0) * 100) + "%";
			String end = getString(R.string.main_tab_stocks_currency_end);

			Spannable spannable = new SpannableString(start + middle + end);

			int colorID;
			if (stock.change > 1) {
				colorID = R.color.stock_change_increase;
			} else if (stock.change == 1) {
				colorID = R.color.stock_change_stagnation;
			} else {
				colorID = R.color.stock_change_decrease;
			}
			spannable.setSpan(
					new ForegroundColorSpan(getResources().getColor(colorID)),
					start.length(), start.length() + middle.length(),
					Spannable.SPAN_EXCLUSIVE_INCLUSIVE);

			((TextView) view.findViewById(R.id.main_tab_stocks_card_name))
					.setText(stock.name);
			((TextView) view.findViewById(R.id.main_tab_stocks_card_value))
					.setText(spannable);
			formatter = new Formatter();
			((TextView) view.findViewById(R.id.main_tab_stocks_card_circulated))
					.setText(formatter.format(
							getString(R.string.main_tab_stocks_circulated), -1)
							.toString());
			formatter.close();
			formatter = new Formatter();
			((TextView) view.findViewById(R.id.main_tab_stocks_card_possessed))
					.setText(formatter.format(
							getString(R.string.main_tab_stocks_possessed),
							mClient.getOwnTeam().getStock(position - 1))
							.toString());
			formatter.close();

			return view;
		}
	}

	private class OutgoingOfferAdapter extends BaseAdapter {

		private MsgOffer[] mOffers;

		public OutgoingOfferAdapter() {
			init();
		}

		private void init() {
			mOffers = mClient.getOutgoingOffers();
		}

		@Override
		public void notifyDataSetChanged() {
			init();
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
		public MsgOffer getItem(int position) {
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
					view = getLayoutInflater().inflate(
							R.layout.activity_main_tab_exchange_new_offer_card,
							parent, false);

					// ================== New offer ===================
					// The View objects
					final Spinner stocks = (Spinner) view
							.findViewById(R.id.main_tab_exchange_new_offer_stock);
					final Spinner type = (Spinner) view
							.findViewById(R.id.main_tab_exchange_new_offer_type);
					final TextView price = (TextView) view
							.findViewById(R.id.main_tab_exchange_new_offer_price);
					final TextView amount = (TextView) view
							.findViewById(R.id.main_tab_exchange_new_offer_amount);
					Button sendButton = (Button) view
							.findViewById(R.id.main_tab_exchange_new_offer_send);

					// Type Spinner
					type.setAdapter(new ArrayAdapter<String>(
							ActivityMain.this,
							android.R.layout.simple_spinner_dropdown_item,
							android.R.id.text1, getResources().getStringArray(
									R.array.main_tab_exchange_new_offer_type)));
					type.setOnItemSelectedListener(new OnItemSelectedListener() {

						@Override
						public void onItemSelected(AdapterView<?> parent,
								View view, int position, long id) {

							((NewOfferStockAdapter) stocks.getAdapter())
									.setSell(position == POSITION_OFFER_TYPE_SELL);
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
							price.setText(DECIMAL_FORMAT.format(((Stock) parent
									.getAdapter().getItem(position)).value));
							amount.setText(1 + "");
						}

						@Override
						public void onNothingSelected(AdapterView<?> parent) {
						}
					});

					// Send Button
					sendButton.setOnClickListener(new OnClickListener() {

						@Override
						public void onClick(View v) {
							try {
								mClient.sendOffer(
										(int) stocks.getSelectedItemId(),
										Integer.parseInt(amount.getText()
												.toString()),
										NumberFormat
												.getInstance()
												.parse(price.getText()
														.toString())
												.doubleValue(),
										type.getSelectedItemPosition() == POSITION_OFFER_TYPE_SELL);
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
				MsgOffer offer = getItem(position);

				((TextView) view.findViewById(R.id.main_tab_exchange_card_type))
						.setText(offer.sell ? R.string.main_tab_exchange_offer_type_sell
								: R.string.main_tab_exchange_offer_type_buy);

				Formatter formatter = new Formatter();
				((TextView) view.findViewById(R.id.main_tab_exchange_card_name))
						.setText(formatter
								.format(getString(R.string.main_tab_exchange_offer_stock_name),
										mClient.getModel().stocks[offer.stockId].name)
								.toString());
				formatter.close();

				formatter = new Formatter();
				((TextView) view
						.findViewById(R.id.main_tab_exchange_card_price))
						.setText(formatter
								.format(getString(R.string.main_tab_exchange_offer_price),
										DECIMAL_FORMAT.format(offer.price)).toString());
				formatter.close();

				formatter = new Formatter();
				((TextView) view
						.findViewById(R.id.main_tab_exchange_card_amount))
						.setText(formatter
								.format(getString(R.string.main_tab_exchange_offer_amount),
										offer.stockAmount).toString());
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
			Team ownTeam = mClient.getOwnTeam();
			int length = mClient.getModel().stocks.length;
			for (int i = 0; i < length; i++)
				if (ownTeam.getStock(i) > 0)
					possessedStocksList.add(Integer.valueOf(i));

			mPossessedStocks = new int[possessedStocksList.size()];
			for (int i = 0; i < possessedStocksList.size(); i++)
				mPossessedStocks[i] = possessedStocksList.get(i);

			super.notifyDataSetChanged();
		}

		@Override
		public int getCount() {
			return mSell ? mPossessedStocks.length
					: mClient.getModel().stocks.length;
		}

		@Override
		public Stock getItem(int position) {
			return mClient.getModel().stocks[(int) getItemId(position)];
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
			((TextView) view.findViewById(TEXT_VIEW_ID))
					.setText(getItem(position).name);
			return view;
		}

	}

}
