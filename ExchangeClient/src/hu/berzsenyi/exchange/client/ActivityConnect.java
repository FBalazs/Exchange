package hu.berzsenyi.exchange.client;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class ActivityConnect extends Activity {
	public EditText editTextName, editTextIP, editTextPort;
	public Button btnConnect;
	
	public void onClickConnect() {
		Intent intent = new Intent(this.getApplicationContext(), ActivityMain.class);
		intent.putExtra(ActivityMain.EXTRA_NAME, this.editTextName.getText().toString());
		intent.putExtra(ActivityMain.EXTRA_IP, this.editTextIP.getText().toString());
		intent.putExtra(ActivityMain.EXTRA_PORT, Integer.parseInt(this.editTextPort.getText().toString()));
		this.startActivity(intent);
	}
	
	@Override
	protected void onCreate(Bundle savedInstance) {
		Log.d(this.getClass().getName(), "onCreate()");
		super.onCreate(savedInstance);
		this.setContentView(R.layout.activity_connect);
		
		this.editTextName = (EditText)this.findViewById(R.id.editTextName);
		this.editTextIP = (EditText)this.findViewById(R.id.editTextIP);
		this.editTextPort = (EditText)this.findViewById(R.id.editTextPort);
		
		this.editTextName.setText("NickName");
		this.editTextIP.setText("192.168.0.11");
		this.editTextPort.setText("8080");
		
		this.btnConnect = (Button)this.findViewById(R.id.buttonConnect);
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
