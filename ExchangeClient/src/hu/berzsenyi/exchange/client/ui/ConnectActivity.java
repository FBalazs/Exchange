package hu.berzsenyi.exchange.client.ui;

import hu.berzsenyi.exchange.client.R;
import hu.berzsenyi.exchange.client.game.ClientExchange;
import hu.berzsenyi.exchange.client.game.ClientExchange.IClientExchangeListener;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

public class ConnectActivity extends ActionBarActivity {

	private static final String DEFAULT_IP = "192.168.0.12",
			DEFAULT_PORT = "8080";

	private TextView mName, mPassword, mIp, mPort;
	private Button mConnectButton;
	private ProgressDialog mDialog;
	private ClientExchange mClient = ClientExchange.INSTANCE;
	private TextWatcher tw = new TextWatcher() {

		@Override
		public void onTextChanged(CharSequence s, int start, int before,
				int count) {
			refreshButtonState();
		}

		@Override
		public void beforeTextChanged(CharSequence s, int start, int count,
				int after) {
		}

		@Override
		public void afterTextChanged(Editable s) {
		}
	};
	private IClientExchangeListener mListener = new IClientExchangeListener() {

		@Override
		public void onConnRefused(ClientExchange exchange) {
			//mClient.removeListener(this); - nope
			runOnUiThread(new Runnable() {

				@Override
				public void run() {
					mDialog.dismiss();
					new AlertDialog.Builder(ConnectActivity.this)
							.setMessage(R.string.could_not_connect)
							.setPositiveButton(R.string.ok, null).create()
							.show();
				}
			});
		}

		@Override
		public void onConnAccepted(ClientExchange exchange) {
			mClient.removeListener(this);
			runOnUiThread(new Runnable() {

				@Override
				public void run() {
					mDialog.dismiss();
					startActivity(new Intent(ConnectActivity.this,
							MainActivity.class));
				}
			});
		}

		@Override
		public void onTrade(ClientExchange exchange) {
		}

		@Override
		public void onStocksChanged(ClientExchange exchange) {
		}

		@Override
		public void onShowBuy(ClientExchange exchange) {
		}

		@Override
		public void onSentOfferRefused(ClientExchange exchange) {
		}

		@Override
		public void onSentOfferAccepted(ClientExchange exchange) {
		}

		@Override
		public void onOfferCame(ClientExchange exchange) {
		}

		@Override
		public void onMyStocksChanged(ClientExchange exchange) {
		}

		@Override
		public void onMyMoneyChanged(ClientExchange exchange) {
		}

		@Override
		public void onConnLost(ClientExchange exchange) {
			//mClient.removeListener(this); - nope
			runOnUiThread(new Runnable() {

				@Override
				public void run() {
					mDialog.dismiss();
					new AlertDialog.Builder(ConnectActivity.this)
							.setMessage(R.string.could_not_connect)
							.setPositiveButton(R.string.ok, null).create()
							.show();
				}
			});
		}

		@Override
		public void onBuyRefused(ClientExchange exchange) {
		}

		@Override
		public void onBuyAccepted(ClientExchange exchange) {
		}

		@Override
		public void onBuyEnd(ClientExchange exchange) {
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_connect);

		ActionBar actionBar = getSupportActionBar();
		actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_HOME
				| ActionBar.DISPLAY_SHOW_TITLE);
		actionBar.setIcon(R.drawable.ic_launcher);
		actionBar.setElevation(getResources().getDimension(
				R.dimen.actionBar_elevation));

		mName = (TextView) findViewById(R.id.connect_name);
		mPassword = (TextView) findViewById(R.id.connect_password);
		mIp = (TextView) findViewById(R.id.connect_ip);
		mPort = (TextView) findViewById(R.id.connect_port);
		mConnectButton = (Button) findViewById(R.id.connect_connectButton);

		mIp.setText(DEFAULT_IP);
		mPort.setText(DEFAULT_PORT);
		refreshButtonState();
		mName.addTextChangedListener(tw);
		mPassword.addTextChangedListener(tw);

		mConnectButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				connect();
			}
		});
		
		System.out.println("adding listener");
		mClient.addListener(mListener);
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		mClient.close();
	}

	private void refreshButtonState() {
		mConnectButton.setEnabled(!mName.getText().toString().equals("")
				&& !mPassword.getText().toString().equals(""));
	}

	private void connect() {
		mDialog = ProgressDialog.show(this, null,
				getString(R.string.connecting), true, false);

		mClient.connect(mIp.getText().toString(), Integer.parseInt(mPort
				.getText().toString()), mName.getText().toString(), mPassword
				.getText().toString());
	}

}
