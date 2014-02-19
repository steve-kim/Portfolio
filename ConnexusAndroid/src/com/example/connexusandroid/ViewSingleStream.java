package com.example.connexusandroid;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.GridView;
import android.widget.Toast;

import com.octo.android.robospice.SpiceManager;
import com.octo.android.robospice.persistence.exception.SpiceException;
import com.octo.android.robospice.request.listener.RequestListener;

public class ViewSingleStream extends Activity {
    private final SpiceManager mSpiceManager = new SpiceManager(ConnexusService.class);
    private RequestSingleStream mRequest;
    private static ConnexusPhoto.List allRequestedPhotos = new ConnexusPhoto.List();
    private SingleStreamAdapter singleStream;
    private String streamId;
    private String streamName;
    private boolean userLoggedIn;
    
    public final static String PHOTO_STREAM_ID = "com.example.connexusandroid.selectedStreamId";
    public final static String PHOTO_STREAM_NAME = "com.example.connexusandroid.selectedStreamName";
    public final static String USER_LOGGED_IN = "com.example.connexusandroid.viewSingleStreamUserLoggedIn";
    //private GridView gridView = (GridView) findViewById(R.id.singleStream);
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		Intent intent = getIntent();
		setContentView(R.layout.activity_view_single_stream);
		
		streamId = intent.getStringExtra(ViewAllStreams.STREAM_ID);
		streamName = intent.getStringExtra(ViewAllStreams.STREAM_NAME);
		userLoggedIn = intent.getBooleanExtra(ViewAllStreams.USER_LOGGED_IN, false);
		if (userLoggedIn)
			System.out.println("We are logged in");
		else
			System.out.println("We are not logged in");
		
		mRequest = new RequestSingleStream(Long.valueOf(streamId));		
	}

	@Override
    public void onStart() {
        super.onStart();
        mSpiceManager.start(this);
        mSpiceManager.execute(mRequest, new ConnexusPhotoRequestListener());
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
	
    private void showResults(ConnexusPhoto.List photos) {
        //Copy the results of the WebAPI fetch to the global variable
    	allRequestedPhotos = photos;
    	singleStream = new SingleStreamAdapter(ViewSingleStream.this);
    	//Create gridview instance to display on phone
    	GridView gridView = (GridView) findViewById(R.id.singleStream);
    	//gridView.setAdapter(new SingleStreamAdapter(this));
    	gridView.setAdapter(singleStream);
    }
    
    public static ConnexusPhoto.List getPhotos() {
    	return allRequestedPhotos;
    }
    
    //Load more pictures if there are more than 16
    public void morePictures(View view) {
    	//allRequestedPhotos has been set by the time user can click on button
    	singleStream.setNextPictureSet();
    	
    	GridView gridView = (GridView) findViewById(R.id.singleStream);
    	gridView.invalidateViews();
    	gridView.setAdapter(singleStream);
    }
    
    //Go to Upload Image activity
    public void uploadImage(View view) {
    	if (userLoggedIn == false) {
    		Toast.makeText(this, "You must be logged in to upload photos", Toast.LENGTH_LONG).show();
    	}
    	else {
    		Intent intent = new Intent(ViewSingleStream.this, UploadImage.class);
        	intent.putExtra(PHOTO_STREAM_ID, streamId);
    		intent.putExtra(PHOTO_STREAM_NAME, streamName);
        	startActivity(intent);
    	}
    	
    	/*Intent intent = new Intent(ViewSingleStream.this, UploadImage.class);
    	intent.putExtra(PHOTO_STREAM_ID, streamId);
		intent.putExtra(PHOTO_STREAM_NAME, streamName);
    	startActivity(intent);*/
    }
    
    //Go back to View Streams activity
    public void streams(View view) {
    	Intent intent = new Intent(ViewSingleStream.this, ViewAllStreams.class);
    	intent.putExtra(USER_LOGGED_IN, userLoggedIn);
    	startActivity(intent);
    }
    
    
    
	private final class ConnexusPhotoRequestListener implements RequestListener<ConnexusPhoto.List> {

	        @Override
	        public void onRequestFailure(SpiceException spiceException) {
	            // Something bad happened
	        }

	        @Override
	        public void onRequestSuccess(final ConnexusPhoto.List result) {
	            ViewSingleStream.this.showResults(result);
	        }
	    }


}
