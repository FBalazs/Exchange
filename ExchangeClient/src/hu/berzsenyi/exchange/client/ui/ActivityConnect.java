package hu.berzsenyi.exchange.client.ui;

import hu.berzsenyi.exchange.client.R;
import hu.berzsenyi.exchange.client.game.ClientExchangeGame;
import hu.berzsenyi.exchange.client.game.ClientExchangeGame.IClientExchangeGameListener;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class ActivityConnect extends ActionBarActivity {

	private static final String DEFAULT_IP = "192.168.0.127",
			DEFAULT_PORT = "8080";

	private EditText editTextName, editTextPass, editTextIP, editTextPort;
	private Button btnConnect;

	private ClientExchangeGame mClient = ClientExchangeGame.getInstance();

	private Dialog mDialog;

	private IClientExchangeGameListener mListener = new IClientExchangeGameListener() {

		@Override
		public void onTrade(ClientExchangeGame exchange) {
		}

		@Override
		public void onStocksChanged(ClientExchangeGame exchange) {
		}

		@Override
		public void onStockIssueEnded(ClientExchangeGame exchange) {
		}

		@Override
		public void onShowBuy(ClientExchangeGame exchange) {
		}

		@Override
		public void onSentOfferRefused(ClientExchangeGame exchange) {
		}

		@Override
		public void onSentOfferAccepted(ClientExchangeGame exchange) {
		}

		@Override
		public void onOfferCame(ClientExchangeGame exchange) {
		}

		@Override
		public void onMyStocksChanged(ClientExchangeGame exchange) {
		}

		@Override
		public void onMyMoneyChanged(ClientExchangeGame exchange) {
		}

		@Override
		public void onConnRefused(ClientExchangeGame exchange) {
			mClient.removeListener(this);
			runOnUiThread(new Runnable() {

				@Override
				public void run() {
					mDialog.dismiss();
					new AlertDialog.Builder(ActivityConnect.this)
							.setMessage(R.string.connection_refused)
							.setPositiveButton(R.string.ok, null).create()
							.show();
				}

			});
		}
		
		@Override
		public void onConnFailed(ClientExchangeGame exchange) {
			mClient.removeListener(this);
			runOnUiThread(new Runnable() {

				@Override
				public void run() {
					mDialog.dismiss();
					new AlertDialog.Builder(ActivityConnect.this)
							.setMessage(R.string.could_not_connect)
							.setPositiveButton(R.string.ok, null).create()
							.show();
				}

			});
		}

		@Override
		public void onConnLost(ClientExchangeGame exchange) {
		}

		@Override
		public void onConnAccepted(ClientExchangeGame exchange) {
			mClient.removeListener(this);
			runOnUiThread(new Runnable() {

				@Override
				public void run() {
					mDialog.dismiss();
					startActivity(new Intent(ActivityConnect.this,
							ActivityMain.class));
				}
			});
		}

		@Override
		public void onBuyRefused(ClientExchangeGame exchange) {
		}

		@Override
		public void onBuyAccepted(ClientExchangeGame exchange) {
		}
	};

	public void onClickConnect() {

		mDialog = ProgressDialog.show(this, null,
				getString(R.string.connecting), true, false);

		mClient.addListener(mListener);
		mClient.connect(
				editTextIP.getText().toString(),
				Integer.parseInt(editTextPort.getText().toString().equals("") ? DEFAULT_PORT
						: editTextPort.getText().toString()), editTextName
						.getText().toString(), editTextPass.getText()
						.toString());
	}

	@Override
	protected void onCreate(Bundle savedInstance) {
		Log.d(this.getClass().getName(), "onCreate()");
		super.onCreate(savedInstance);
		this.setContentView(R.layout.activity_connect);

		ActionBar actionBar = getSupportActionBar();
		actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_HOME
				| ActionBar.DISPLAY_SHOW_TITLE);
		actionBar.setIcon(R.drawable.ic_launcher);
		actionBar.setElevation(getResources().getDimension(
				R.dimen.actionBar_elevation));

		editTextName = (EditText) findViewById(R.id.editTextName);
		editTextPass = (EditText) findViewById(R.id.editTextPass);
		editTextIP = (EditText) findViewById(R.id.editTextIP);
		editTextPort = (EditText) findViewById(R.id.editTextPort);

		// editTextName.setText(DEFAULT_NAME);
		if (editTextIP.getText().toString().equals(""))
			editTextIP.setText(DEFAULT_IP);
		if (editTextPort.getText().toString().equals(""))
			editTextPort.setText(DEFAULT_PORT);

		btnConnect = (Button) findViewById(R.id.buttonConnect);
		btnConnect.setEnabled(!editTextName.getText().toString().equals("")
				&& !editTextPass.getText().toString().equals(""));
		btnConnect.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				onClickConnect();
			}
		});

		TextWatcher textWatcher = new TextWatcher() {
			@Override
			public void onTextChanged(CharSequence s, int start, int before,
					int count) {

			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {

			}

			@Override
			public void afterTextChanged(Editable s) {
				btnConnect.setEnabled(!editTextName.getText().toString()
						.equals("")
						&& !editTextPass.getText().toString().equals(""));
			}
		};

		editTextName.addTextChangedListener(textWatcher);
		editTextPass.addTextChangedListener(textWatcher);
	}

	@Override
	protected void onDestroy() {
		Log.d(this.getClass().getName(), "onDestroy()");
		super.onDestroy();
	}
}