package hu.berzsenyi.exchange.client;

import hu.berzsenyi.exchange.Team;
import hu.berzsenyi.exchange.net.TCPClient;
import hu.berzsenyi.exchange.net.cmd.CmdOffer;
import hu.berzsenyi.exchange.net.cmd.CmdOfferResponse;
import hu.berzsenyi.exchange.net.cmd.CmdServerError;

import java.io.IOException;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class ActivityConnect extends Activity {

	private static final String DEFAULT_NAME = "NickName",
			DEFAULT_IP = "192.168.0.11", DEFAULT_PORT = "8080";

	private EditText editTextName, editTextIP, editTextPort;
	private Button btnConnect;

	private ExchangeClient mClient = ExchangeClient.getInstance();

	private Dialog mDialog;

	public void onClickConnect() {

		mDialog = ProgressDialog.show(this, null,
				getString(R.string.connecting), true, false);

		mClient.setName(this.editTextName.getText().toString());
		// TODO Unregister listener
		mClient.addIClientListener(new IClientListener() {

			@Override
			public void onConnectionFail(TCPClient client, IOException exception) {
				ActivityConnect.this.mClient.removeIClientListener(this);
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
				ActivityConnect.this.mClient.removeIClientListener(this);
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
			public void onOfferIn(ExchangeClient client, CmdOffer offer) {
			}

			@Override
			public void onRoundCommand(ExchangeClient client) {
			}

			@Override
			public void onMoneyChanged(Team ownTeam) {
			}

			@Override
			public void onStocksChanged(Team ownTeam, int position) {
			}

			@Override
			public void onOfferAccepted(CmdOfferResponse offer) {
			}

			@Override
			public void onErrorCommand(CmdServerError error) {
			}

		});
		mClient.connect(this.editTextIP.getText().toString(),
				Integer.parseInt(this.editTextPort.getText().toString()));
	}

	@Override
	protected void onCreate(Bundle savedInstance) {
		Log.d(this.getClass().getName(), "onCreate()");
		super.onCreate(savedInstance);
		this.setContentView(R.layout.activity_connect);

		this.editTextName = (EditText) this.findViewById(R.id.editTextName);
		this.editTextIP = (EditText) this.findViewById(R.id.editTextIP);
		this.editTextPort = (EditText) this.findViewById(R.id.editTextPort);

		this.editTextName.setText(DEFAULT_NAME);
		this.editTextIP.setText(DEFAULT_IP);
		this.editTextPort.setText(DEFAULT_PORT);

		this.btnConnect = (Button) this.findViewById(R.id.buttonConnect);
		this.btnConnect.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				onClickConnect();
			}
		});
	}

	@Override
	protected void onDestroy() {
		Log.d(this.getClass().getName(), "onDestroy()");
		super.onDestroy();
	}
}
