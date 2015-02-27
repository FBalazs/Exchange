package hu.berzsenyi.exchange.client.debug;

import hu.berzsenyi.exchange.Stock;
import hu.berzsenyi.exchange.client.R;

import java.text.DecimalFormat;
import java.util.Formatter;

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
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.example.android.common.view.SlidingTabLayout;

public class CardView extends ActionBarActivity {

	private static final int[] LABEL_IDS = { R.string.main_tab_label_newsfeed,
			R.string.main_tab_label_stocks, R.string.main_tab_label_exchange,
			R.string.main_tab_label_stats };
	private static final int POSITION_NEWS_FEED = 0, POSITION_STOCKS = 1,
			POSITION_EXCHANGE = 2, POSITION_STATS = 3;

	protected static final DecimalFormat DECIMAL_FORMAT = new DecimalFormat(
			"#0.00");

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_debug);

		ActionBar actionBar = getSupportActionBar();
		actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_HOME
				| ActionBar.DISPLAY_SHOW_TITLE);
		actionBar.setIcon(R.drawable.ic_launcher);
		actionBar.setElevation(0);

		ViewPager viewPager = (ViewPager) findViewById(R.id.viewPager);
		viewPager.setAdapter(new MainPagerAdapter());

		SlidingTabLayout slidingTabLayout = (SlidingTabLayout) findViewById(R.id.slidingTabLayout);
		slidingTabLayout.setCustomTabView(R.layout.activity_main_tab,
				R.id.tab_label);
		slidingTabLayout.setViewPager(viewPager);
		slidingTabLayout.setSelectedIndicatorColors(getResources().getColor(
				R.color.tabIndicator));

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
			case POSITION_STOCKS:
				view = getLayoutInflater().inflate(
						R.layout.activity_main_tab_stocks, container, false);

				ListView stockList = (ListView) view
						.findViewById(R.id.main_tab_stocks_list);
				stockList.setAdapter(new StockAdapter());
				break;
			default:
				view = getLayoutInflater().inflate(
						R.layout.activity_debug_hello, container, false);
			}

			container.addView(view);
			return view;
		}

		@Override
		public void destroyItem(ViewGroup container, int position, Object object) {
			container.removeView((View) container);
		}
	}

	private class StockAdapter extends BaseAdapter {

		private Stock[] stocks;

		public StockAdapter() {
			// stocks = mClient.getModel().stocks;
			stocks = new Stock[] { new Stock("a", "HTC", 20.121301),
					new Stock("b", "Lengyelvíz", 210.3),
					new Stock("cd", "Bögre", 10.0) };
			stocks[0].change = 1.2874;
			stocks[1].change = 0.23432;
		}

		@Override
		public int getCount() {
			return stocks.length + 2;
		}

		@Override
		public Object getItem(int position) {
			if (position == 0 || position == getCount() - 1)
				return null;
			return stocks[position - 1];
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
					view = new View(CardView.this);
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

			Stock stock = (Stock) getItem(position);

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

			((TextView) view.findViewById(R.id.main_tab_stocks_stock_name))
					.setText(stock.name);
			((TextView) view.findViewById(R.id.main_tab_stocks_stock_value))
					.setText(spannable);
			formatter = new Formatter();
			((TextView) view
					.findViewById(R.id.main_tab_stocks_stock_circulated))
					.setText(formatter.format(
							getString(R.string.main_tab_stocks_circulated), -1)
							.toString());
			formatter.close();
			formatter = new Formatter();
			((TextView) view.findViewById(R.id.main_tab_stocks_stock_possessed))
					.setText(formatter.format(
							getString(R.string.main_tab_stocks_possessed), -1)
							.toString());
			formatter.close();

			return view;
		}
	}
}
