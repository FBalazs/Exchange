package hu.berzsenyi.exchange.client;

import hu.berzsenyi.exchange.SingleEvent;
import hu.berzsenyi.exchange.Team;
import hu.berzsenyi.exchange.net.TCPClient;

import java.io.IOException;

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

	private static final String DEFAULT_NAME = "NickName",
			DEFAULT_IP = "192.168.0.12", DEFAULT_PORT = "8080";

	private EditText editTextName, editTextPass, editTextIP, editTextPort;
	private Button btnConnect;

	private ExchangeClient mClient = ExchangeClient.getInstance();

	private Dialog mDialog;

	public void onClickConnect() {

		mDialog = ProgressDialog.show(this, null,
				getString(R.string.connecting), true, false);

		mClient.setName(editTextName.getText().toString());
		mClient.setPassword(editTextPass.getText().toString());
		// TODO Unregister listener
		mClient.addIClientListener(new IClientListener() {

			@Override
			public void onConnectionFail(TCPClient client, IOException exception) {
				mClient.removeIClientListener(this);
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
			public void onConnect(TCPClient client) {
				// ActivityConnect.this.mClient.removeIClientListener(this);
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
			public void onClose(TCPClient client) {
			}

			@Override
			public void onTeamsCommand(ExchangeClient client) {
			}

			@Override
			public void onStocksCommand(ExchangeClient client) {
			}

			@Override
			public void onNewRound(SingleEvent[] events) {
			}

			@Override
			public void onMoneyChanged(Team ownTeam) {
			}

			@Override
			public void onStocksChanged(Team ownTeam, int position) {
			}

			/*@Override
			public void onErrorCommand(CmdServerError error) {

				switch (error.errorId) {
				case CmdServerError.ERROR_NOT_IN_ZEROTH_ROUND:
					mClient.disconnect();
					runOnUiThread(new Runnable() {
						@Override
						public void run() {
							Toast.makeText(
									ActivityConnect.this
											.getApplicationContext(),
									R.string.game_has_started,
									Toast.LENGTH_LONG).show();
							// ActivityZerothRound.this.finish();
						}
					});
					break;
				case CmdServerError.ERROR_NAME_DUPLICATE:
					mClient.disconnect();
					runOnUiThread(new Runnable() {
						@Override
						public void run() {
							Toast.makeText(
									ActivityConnect.this
											.getApplicationContext(),
									R.string.name_duplicate, Toast.LENGTH_LONG)
									.show();
							// ActivityZerothRound.this.finish();
						}
					});
					break;
				}
			}*/

			@Override
			public void onOutgoingOffersChanged() {
			}

		});
		mClient.connect(editTextIP.getText().toString(), Integer.parseInt(editTextPort.getText().toString().equals("") ? DEFAULT_PORT : editTextPort.getText().toString()));
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

		//editTextName.setText(DEFAULT_NAME);
		if(editTextIP.getText().toString().equals(""))
			editTextIP.setText(DEFAULT_IP);
		if(editTextPort.getText().toString().equals(""))
			editTextPort.setText(DEFAULT_PORT);

		btnConnect = (Button) findViewById(R.id.buttonConnect);
		btnConnect.setEnabled(!editTextName.getText().toString().equals("") && !editTextPass.getText().toString().equals(""));
		btnConnect.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				onClickConnect();
			}
		});
		
		TextWatcher textWatcher = new TextWatcher() {
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				
			}
			
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {
				
			}
			
			@Override
			public void afterTextChanged(Editable s) {
				btnConnect.setEnabled(!editTextName.getText().toString().equals("") && !editTextPass.getText().toString().equals(""));
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
