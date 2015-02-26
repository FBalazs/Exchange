package hu.berzsenyi.exchange.client.debug;

import hu.berzsenyi.exchange.client.R;
import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.view.View;
import android.view.ViewGroup;
import com.example.android.common.view.SlidingTabLayout;

public class CardView extends ActionBarActivity {

	private static final int[] LABEL_IDS = { R.string.tab_label_newsfeed,
			R.string.tab_label_stocks, R.string.tab_label_exchange,
			R.string.tab_label_stats };
	private static final int[] LAYOUT_IDS = { R.layout.activity_debug_hello,
			R.layout.activity_debug_hello, R.layout.activity_debug_hello,
			R.layout.activity_debug_hello };

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
			View view = getLayoutInflater().inflate(LAYOUT_IDS[position],
					container, false);
			container.addView(view);
			return view;
		}

		@Override
		public void destroyItem(ViewGroup container, int position, Object object) {
			container.removeView((View) container);
		}
	}
}
