package hu.berzsenyi.exchange.client;

import android.app.Activity;
import android.os.Bundle;
import android.widget.BaseAdapter;
import android.view.ViewGroup;
import android.view.View;
import hu.berzsenyi.exchange.Stock;
import android.widget.TextView;
import android.widget.SeekBar;
import android.widget.ListView;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.util.Log;

public class ActivityZerothRound extends Activity {
	protected final static int REQUEST_CODE = 1;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.d(getClass().getName(), "onCreate() started");
		
		setContentView(R.layout.activity_zeroth_round);
		
		Stock[] stocks = new Stock[0]; // TODO !!!!!!!!!!!!!!!!!!!
		
		setResult(Activity.RESULT_CANCELED);		
		ListView stockList = (ListView) findViewById(R.id.stocks);
		stockList.setAdapter(new StockAdapter(stocks));
		
		((Button) findViewById(R.id.activity_zeroth_round_done)).setOnClickListener(
			new OnClickListener() {

				@Override
				public void onClick(View p1) {
					setResult(Activity.RESULT_OK);
				}
				
				
			});
		Log.d(getClass().getName(), "onCreate() finished");
	}
	
	private class StockAdapter extends BaseAdapter {
		
		private Stock[] stocks;
		
		public StockAdapter(Stock[] stocks) {
			if (stocks == null)
				throw new NullPointerException("Stocks must not be null");
			this.stocks = stocks;
		}

		@Override
		public int getCount() {
			return stocks.length;
		}

		@Override
		public Object getItem(int position) {
			return stocks[position];
		}

		@Override
		public long getItemId(int p1) {
			return 0;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			View out;
			if (convertView == null) {
				out = getLayoutInflater().inflate(R.layout.activity_zeroth_round_stock,
							parent, false);
			} else out = convertView;
			
			Stock stock = (Stock) getItem(position);
			((TextView)out.findViewById(R.id.stock_name)).setText(stock.name);
			((TextView)out.findViewById(R.id.stock_value)).setText(
					getString(R.string.unit_price) + stock.value);
			// TODO setMax according to money
			SeekBar amount = (SeekBar)out.findViewById(R.id.stock_amount);
			amount.setProgress(0);
			
			
			
			return out;
		}
		
		
	}
}
