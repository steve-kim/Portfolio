package com.example.connexusandroid;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.NavUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

public class UseCamera extends Activity {
	private String streamId;
	private String streamName;
	private double latitude;
	private double longitude;
	
	private static final int TAKE_PICTURE = 1;
	private ImageView imageView;
	private Bitmap mImageBitmap;
	private String JPEG_FILE_PREFIX = "ConnexusAndroid";
	private String JPEG_FILE_SUFFIX= "";
	private String mCurrentPhotoPath;
	private File storageDir;
	private DecodeImage decodeImage;
	private Uri uriImage;

	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Intent intent = getIntent();
		setContentView(R.layout.activity_use_camera);
		// Show the Up button in the action bar.
		//setupActionBar();
		streamId = intent.getStringExtra(UploadImage.CAMERA_STREAM_ID);
		streamName = intent.getStringExtra(UploadImage.CAMERA_STREAM_NAME);
		latitude = intent.getDoubleExtra(UploadImage.CAMERA_LATITUDE, 0);
		longitude = intent.getDoubleExtra(UploadImage.CAMERA_LONGITUDE, 0);
		
		
		imageView = (ImageView) findViewById(R.id.showCameraPicture);
		decodeImage = new DecodeImage();
	}

	public void takePicture (View view) throws IOException {
	    Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
	    //Creates a file for the picture data to write to
	    storageDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "");
	    File f = createImageFile();
	    uriImage = Uri.fromFile(f);
	    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, uriImage);
	    
	    //Start the Camera App
	    startActivityForResult(takePictureIntent, TAKE_PICTURE);
	    //Intent usePictureIntent = getIntent();
		//Bundle extra = usePictureIntent.getExtras();
		//uriImage = (Uri) extra.get(MediaStore.EXTRA_OUTPUT);
	}
	
	public void usePicture (View view) {
		if (mCurrentPhotoPath != null) {
			new Thread(new Runnable() {
				public void run() {
					try {
						decodeImage.decodeImageFromPhone(mCurrentPhotoPath, streamId, streamName, latitude, longitude);
					} catch (UnsupportedEncodingException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}).start();
			
			Toast.makeText(this, "Upload successful!", Toast.LENGTH_LONG).show();
			//this.setResult(RESULT_OK);
			//this.finish();
			
			
		}
		
		else {
			Toast.makeText(this, "Please take a picture first", Toast.LENGTH_LONG).show();
		}
	}
	
	//Go back to the View All Streams Activity
	public void backToStreams (View view) {
		Intent intent = new Intent(UseCamera.this, ViewAllStreams.class);
    	startActivity(intent);
	}
	
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            if (requestCode == TAKE_PICTURE) {
                //display img to ImageView
            	if (data == null) {
                	ByteArrayOutputStream stream = new ByteArrayOutputStream();
        			mImageBitmap = BitmapFactory.decodeFile(mCurrentPhotoPath);
                	imageView.setImageBitmap(mImageBitmap);
            	} else {
            		Bundle extras = data.getExtras();
            		mImageBitmap = (Bitmap) extras.get("data");
            		imageView.setImageBitmap(mImageBitmap);
            	}
            }
        }
    }
    
    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = 
            new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = JPEG_FILE_PREFIX + timeStamp + "_";
        File image = File.createTempFile(imageFileName, JPEG_FILE_SUFFIX, storageDir);
        
        mCurrentPhotoPath = image.getAbsolutePath();
        return image;
    }
    
    
	/**
	 * Set up the {@link android.app.ActionBar}.
	 */
	private void setupActionBar() {

		getActionBar().setDisplayHomeAsUpEnabled(true);

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.use_camera, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			// This ID represents the Home or Up button. In the case of this
			// activity, the Up button is shown. Use NavUtils to allow users
			// to navigate up one level in the application structure. For
			// more details, see the Navigation pattern on Android Design:
			//
			// http://developer.android.com/design/patterns/navigation.html#up-vs-back
			//
			NavUtils.navigateUpFromSameTask(this);
			return true;
		}
		return super.onOptionsItemSelected(item);
	}


}
