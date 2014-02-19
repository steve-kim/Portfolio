package com.example.connexusandroid;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;

import com.octo.android.robospice.SpiceManager;
import com.octo.android.robospice.persistence.exception.SpiceException;
import com.octo.android.robospice.request.listener.RequestListener;

public class ViewAllStreams extends Activity {
    private final SpiceManager mSpiceManager = new SpiceManager(ConnexusService.class);
    private RequestAllStreams mRequest;
    private static ConnexusStream.List allRequestedStreams = new ConnexusStream.List();
    private boolean userLoggedIn = false;
    public final static String STREAM_ID = "com.example.connexusandroid.requestedStreamId";
    public final static String STREAM_NAME = "com.example.connexusandroid.requestedStreamName";
    public final static String USER_LOGGED_IN = "com.example.connexusandroid.viewAllStreamsUserLoggedIn";
    

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Intent intent = getIntent();
		//Set boolean flag to track if user is logged in or not
		userLoggedIn = intent.getBooleanExtra(LoginActivity.USER_LOGGED_IN, false);
		if (userLoggedIn)
			System.out.println("We are logged in");
		else
			System.out.println("We are not logged in");
		
		setContentView(R.layout.activity_view_all_streams);
		
		mRequest = new RequestAllStreams();		
	}
	
	@Override
    public void onStart() {
        super.onStart();
        mSpiceManager.start(this);
        mSpiceManager.execute(mRequest, new ConnexusStreamRequestListener());
    }

    @Override
    public void onStop() {
        // Please review https://github.com/octo-online/robospice/issues/96 for
        // the reason of that ugly if statement.
        if (mSpiceManager.isStarted()) {
            mSpiceManager.shouldStop();
        }
        super.onStop();
    }
	
    private void showResults(ConnexusStream.List streams) {
        //Copy the results of the WebAPI fetch to the global variable
    	allRequestedStreams = streams;
    	
    	//Create gridview instance to display on phone
    	GridView gridView = (GridView) findViewById(R.id.allStreams);
    	final ImageAdapter showStreams = new ImageAdapter(this); 
    	gridView.setAdapter(showStreams);
    	//Create Listener to go to single stream
    	gridView.setOnItemClickListener(new OnItemClickListener() {
    		public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
    			Intent intent = new Intent(ViewAllStreams.this, ViewSingleStream.class);
    			intent.putExtra(STREAM_ID, String.valueOf(showStreams.getStreamId(position)));
    			intent.putExtra(STREAM_NAME, showStreams.getStreamName(position));
    			intent.putExtra(USER_LOGGED_IN, userLoggedIn);
    			startActivity(intent);
    		}
    	});
    }
    
    public static ConnexusStream.List getStreams() {
    	return allRequestedStreams;
    }
    
    public void searchStreams() {
    	
    }
    
    
	private final class ConnexusStreamRequestListener implements RequestListener<ConnexusStream.List> {

	        @Override
	        public void onRequestFailure(SpiceException spiceException) {
	            // Something bad happened
	        }

	        @Override
	        public void onRequestSuccess(final ConnexusStream.List result) {
	            ViewAllStreams.this.showResults(result);
	        }
	    }

}
