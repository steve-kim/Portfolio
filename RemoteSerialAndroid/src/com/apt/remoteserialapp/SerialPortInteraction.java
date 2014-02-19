package com.apt.remoteserialapp;

import java.io.BufferedReader;
//import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Arrays;

import android.accounts.AccountManager;
import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.telephony.SmsManager;
import android.text.method.ScrollingMovementMethod;
import android.view.Menu;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException;
import com.google.api.client.http.FileContent;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;
import com.google.api.services.drive.model.File;

public class SerialPortInteraction extends Activity {
	//Activity view variables
	private TextView serialOutputTextView = null;
	private EditText serialInputEditText = null;
	private EditText searchForMessage = null;
	//String variables
	private String IP_Address = null;
	private String serialPort = null;
	private String serialPortPrevious = "";
	private String serialCommand = null;
	private String searchString = null;
	private String phoneNumber = "5129643364";
	private String message = "ERROR FOUND!";
	//Socket variables
	private int Port_Number = 0;
	private boolean logging = false;
	private boolean socketCreated = false;
	private static Socket serialSocket = null;
	PrintWriter serialDataOut = null;
	BufferedReader serialDataIn = null;
	BufferedReader stdIn = null;
	//File variables
	private java.io.File log = null;
	private FileWriter writer = null;
	//AsyncTask variables
	private readSerialPort serialRead = null;
	//Google Drive variables
	private static Drive service;
	private GoogleAccountCredential credential = null;
	static final int REQUEST_ACCOUNT_PICKER = 1;
	static final int REQUEST_AUTHORIZATION = 2;

	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Intent intent = getIntent();
		
		//Get relevant Socket information from previous Activity
		IP_Address = intent.getStringExtra(MainActivity.PORT_IP_ADDRESS);
		Port_Number = Integer.parseInt(intent.getStringExtra(MainActivity.PORT_NUMBER));
		
		//Set the proper Views for this application
		setContentView(R.layout.activity_serial_port_interaction);
		
		serialOutputTextView = (TextView)findViewById(R.id.SerialOutput);
		serialOutputTextView.setMovementMethod(new ScrollingMovementMethod());
		
		serialInputEditText = (EditText)findViewById(R.id.SerialInput);
		searchForMessage = (EditText)findViewById(R.id.SearchForError);
		
		//Debug messages to make sure everything has successfully completed
		//System.out.println("SUCCESS!!!!");
		//System.out.println(IP_Address);
		//System.out.println(Port_Number);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.serial_port_interaction, menu);
		return true;
	}
	
	public void toggleLogging(View view) {
		//We are currently not logging anything.  Enable all the sockets to start.
		if (logging == false){
			logging = true;
			System.out.println("Begin logging");
			new createSocket().execute();
			//Wait until the socket has been created
			//NOTE: This is not a great way to do this.  Definitely a chance of an infinite loop
			//if the Socket creation fails.  Revisit and fix this later.
			while (socketCreated == false) {
				//System.out.println(socketCreated);
			}
			serialRead = new readSerialPort(); 
			serialRead.execute();
		}
		
		//We are currently logging activity.  We want to stop logging.
		else {
			logging = false;
			System.out.println("Stop logging");
			try {
				serialRead.cancel(true);
				writer.close();
				serialSocket.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			
		}
		
	}
	
	public void sendData(View view) {
		if (serialInputEditText.getText().toString() == null)
			serialCommand = "";
		else
			serialCommand = serialInputEditText.getText().toString();
		
		//executeOnExecutor allows for more than 1 background task at a time
		new writeSerialPort().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, null);
		
		serialInputEditText.setText("");
	}
	
	public void setErrorSearch(View view) {
		if ((searchForMessage.getText().toString() != null)) {
			searchString = searchForMessage.getText().toString();
			//searchString.replaceAll("\\s+","");
			
			//\W is the negated reg exp for \w.
			//Replace all "non-word characters" with empty character
			//This creates a string with effectively no white space
			searchString = searchString.replaceAll("\\W","");
			System.out.println(searchString);
		}
	}
	
	public void uploadGoogleDrive(View view) {
		//Find authorized Google accounts present on this device
		if (credential == null) {
			credential = GoogleAccountCredential.usingOAuth2(this, Arrays.asList(DriveScopes.DRIVE));
		    startActivityForResult(credential.newChooseAccountIntent(), REQUEST_ACCOUNT_PICKER);
		}
		else
			saveFileToDrive();
	}
	
	@Override
	protected void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
		switch (requestCode) {
	    case REQUEST_ACCOUNT_PICKER:
	      if (resultCode == RESULT_OK && data != null && data.getExtras() != null) {
	        String accountName = data.getStringExtra(AccountManager.KEY_ACCOUNT_NAME);
	        if (accountName != null) {
	          credential.setSelectedAccountName(accountName);
	          System.out.println("Credential selected");
	          service = getDriveService(credential);
	          System.out.println("Drive service retrieved");
	          System.out.println("Calling saveFileToDrive()");
	          saveFileToDrive();
	        }
	      }
	      break;
	    case REQUEST_AUTHORIZATION:
	      if (resultCode == Activity.RESULT_OK) {
	        saveFileToDrive();
	      } else {
	        startActivityForResult(credential.newChooseAccountIntent(), REQUEST_ACCOUNT_PICKER);
	      }
	    }
	}
	
	//Uploads file to GoogleDrive
	private void saveFileToDrive() {
		Thread t = new Thread(new Runnable() {

			@Override
			public void run() {
				// TODO Auto-generated method stub
				try {
					FileContent mediaContent = new FileContent("text/plain", log);
					
					// File's metadata.
			        File body = new File();
			        System.out.println("File Name: " + log.getName());
			        body.setTitle(log.getName());
			        body.setMimeType("text/plain");
			        
			        System.out.println("Uploading to Google Drive");
			        File file = service.files().insert(body, mediaContent).execute();
			        System.out.println("Upload successfull");
				} catch (UserRecoverableAuthIOException e) {
			          startActivityForResult(e.getIntent(), REQUEST_AUTHORIZATION);
				} catch (IOException e) {
					e.printStackTrace();
				}	
			}
			
		});
        t.start();
	}
	
	private Drive getDriveService(GoogleAccountCredential credential) {
		return new Drive.Builder(AndroidHttp.newCompatibleTransport(), new GsonFactory(), credential).build();
	}
	
	/*
	 *	Sends SMS message to phone number 
	 */
	private void sendSMS() {
		SmsManager sms = SmsManager.getDefault();
		sms.sendTextMessage(phoneNumber, null, message, null, null);
	}
	
	/*
	 * 
	 * THIS IS THE START OF OUR ASYNC TASKS
	 */

	//This ASYNC TASK CREATES THE SOCKET
	private class createSocket extends AsyncTask<Void, Void, Void> {

		@Override
		protected Void doInBackground(Void... arg0) {
				try {
					//Create the serial port Socket
					serialSocket = new Socket(IP_Address, Port_Number);
					if (serialSocket != null)
						System.out.println("Serial port opened successfully!");
					else
						System.out.println("Serial port failed to open");
					
					//IO Streams for reading/writing to our Socket
					serialDataOut = new PrintWriter(serialSocket.getOutputStream(), true);
					
					serialDataIn = new BufferedReader(new InputStreamReader(serialSocket.getInputStream()));
					stdIn = new BufferedReader(new InputStreamReader(System.in));
					
					//Create the file to write data to
					java.io.File root = new java.io.File(Environment.getExternalStorageDirectory(), "Serial Logs");
					if (!root.exists()) {
			            root.mkdirs();
			        }
					
					log = new java.io.File(root, "SerialLog");
			        writer = new FileWriter(log);
					
					socketCreated = true;
				} catch (IOException e) {
					e.printStackTrace();
				}

			return null;
		}
		
		@Override
		protected void onPostExecute(Void result) {
			super.onPostExecute(result);
			System.out.println("Socket AsyncTask has completed");
			socketCreated = true;
			System.out.println(socketCreated);
		}
	}
	
	
	//THIS ASYNC TASK CONSTANTLY READS DATA FROM THE OPENED SOCKET
	private class readSerialPort extends AsyncTask<Void, String, Void> {
		
		@Override
		protected Void doInBackground(Void... params) {
			//We will constantly receive data until the socket is closed
			System.out.println("Starting to read from serial port");
			while (serialSocket.isConnected() && !isCancelled()) {
				try {
					//This reads the data in from the serial port
					serialPort = serialDataIn.readLine();
					//\W is the negated reg exp for \w.
					//Replace all "non-word characters" with empty character
					//This creates a string with effectively no white space
					String serialPortSearch = serialPort.replaceAll("\\W","");
					
					//Check to see if the String is null to avoid Null Point Exception
					if (serialPort == null) {
						serialPort = "";
						System.out.println("null");
					}
					
					//Check to see if the previous transaction was the same as the current one
					//We probably don't need this anymore.  Check again later.
					if (serialPortPrevious.equals(serialPort) == false) {
						//This allows us to write data gathered from background thread
						//to the TextView in the main UI thread
						SerialPortInteraction.this.runOnUiThread(new Runnable() {

							public void run() {
								serialOutputTextView.append(serialPort+"\n");
					        }
					    });
						System.out.println(serialPort);
					}
					
					//Does current serial output match string to be search for?
					if (serialPortSearch.equalsIgnoreCase(searchString)) {
						System.out.println("Error message found");
						sendSMS();
						writer.append("\n" + serialPort + "\n");
					}
					//Write data to the log file
					else {
						writer.append(serialPort+"\n");
						//Flush the buffer.  We don't want unexpected characters in the next transaction
				        writer.flush();	
					}
					
					//Keep track of previous output for error checking
					//We probably don't need this anymore.  Check again later.
					serialPortPrevious = serialPort;
					
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			return null;
		}
		
	}
	
	//THIS ASYNC TASK IS FIRED WHEN USER INPUTS DATA TO BE SENT TO SERIAL PORT
	private class writeSerialPort extends AsyncTask<Void, Void, Void> {

		@Override
		protected Void doInBackground(Void... arg0) {
			// TODO Auto-generated method stub
			System.out.println("Command to be sent: " + serialCommand);
			//Send the data out to the socket
			serialDataOut.println(serialCommand);
			//Flush the stream.  We don't want any unwanted characters in the next transaction
			serialDataOut.flush();
			return null;
		}
		
	}
}
