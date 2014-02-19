package com.apt.remoteserialapp;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.view.View.OnFocusChangeListener;
import android.widget.EditText;

public class MainActivity extends Activity {
	public final static String PORT_IP_ADDRESS = "com.apt.remoteserialapp.serialPortIP";
    public final static String PORT_NUMBER = "com.apt.remoteserialapp.serialPortNumber";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	
	public void createPort(View view) {
		Intent serialIntent = new Intent(this, SerialPortInteraction.class);
		
		//Set views for this Activity
		EditText ipAddressField = (EditText) findViewById(R.id.IP_Address);
		EditText portNumberField = (EditText) findViewById(R.id.Port_Number);
		
		String ipAddress = ipAddressField.getText().toString();
		String portNumber = portNumberField.getText().toString();
		
		serialIntent.putExtra(PORT_IP_ADDRESS, ipAddress);
		serialIntent.putExtra(PORT_NUMBER, portNumber);
		
		startActivity(serialIntent);
	}

}
