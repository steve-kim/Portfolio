package com.example.connexusandroid;

import java.io.UnsupportedEncodingException;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.location.LocationClient;

public class UploadImage extends Activity {
	public final static String CAMERA_STREAM_ID = "com.example.connexusandroid.cameraStreamId";
    public final static String CAMERA_STREAM_NAME = "com.example.connexusandroid.cameraStreamName";
    public final static String CAMERA_LATITUDE = "com.example.connexusandroid.cameraLatitude";
    public final static String CAMERA_LONGITUDE = "com.example.connexusandroid.cameraLongitude";
    
	
    //YOU CAN EDIT THIS TO WHATEVER YOU WANT
    private static final int SELECT_PICTURE = 1;
    private static final int USE_CAMERA = 2;
    private static LocationManager locationManager;
    private static DecodeImage decodeImage;
    private Location currentLocation;
    private double latitude;
    private double longitude;
    private String streamId;
    private String streamName;
    
    private final static int
    CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000;
    private static LocationClient mLocationClient;

    private String selectedImagePath;
    //ADDED
    private String filemanagerstring;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		Intent intent = getIntent();
		streamId = intent.getStringExtra(ViewSingleStream.PHOTO_STREAM_ID);
		streamName = intent.getStringExtra(ViewSingleStream.PHOTO_STREAM_NAME);
		
		setContentView(R.layout.activity_upload_image);
		
		decodeImage = new DecodeImage();
		
		locationManager = (LocationManager)getSystemService(LOCATION_SERVICE);
		locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                1000, 0, onLocationChange);
	}

	public void chooseLibrary(View view) {
		Intent galleryIntent = new Intent();
		galleryIntent.setType("image/*");
		galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
		startActivityForResult(Intent.createChooser(galleryIntent, "Select Picture"), SELECT_PICTURE);
	}
	
	public void useCamera(View view) {
		Intent cameraIntent = new Intent(UploadImage.this, UseCamera.class);
		cameraIntent.putExtra(CAMERA_STREAM_ID, streamId);
		cameraIntent.putExtra(CAMERA_STREAM_NAME, streamName);
		cameraIntent.putExtra(CAMERA_LATITUDE, latitude);
		cameraIntent.putExtra(CAMERA_LONGITUDE, longitude);
		//startActivityForResult(cameraIntent, USE_CAMERA);
		startActivity(cameraIntent);
	}
	
	public void uploadFile(View view) throws UnsupportedEncodingException {
		Context context = UploadImage.this;
		
		if ((filemanagerstring == null) || (selectedImagePath == null)) {
			Toast.makeText(context, "Please select an image first", Toast.LENGTH_LONG).show();
		}
		else {
			Toast.makeText(context, selectedImagePath, Toast.LENGTH_LONG).show();
			//Converts URI to picture byte array and creates an Image class to upload to API
			new Thread(new Runnable() {
				public void run() {
					try {
						decodeImage.decodeImageFromPhone(selectedImagePath, streamId, streamName, latitude, longitude);
					} catch (UnsupportedEncodingException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}).start();
			//decodeImage.decodeImageFromPhone(selectedImagePath, streamId, streamName);
			System.out.println("File Uploaded");
		}
	}
	
    //UPDATED
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            if (requestCode == SELECT_PICTURE) {
                Uri selectedImageUri = data.getData();

                //OI FILE Manager
                filemanagerstring = selectedImageUri.getPath();

                //MEDIA GALLERY
                selectedImagePath = getPath(selectedImageUri);

                //DEBUG PURPOSE - you can delete this if you want
                if(selectedImagePath!=null)
                    System.out.println(selectedImagePath);
                else System.out.println("selectedImagePath is null");
                if(filemanagerstring!=null)
                    System.out.println(filemanagerstring);
                else System.out.println("filemanagerstring is null");

                //NOW WE HAVE OUR WANTED STRING
                if(selectedImagePath!=null)
                    System.out.println("selectedImagePath is the right one for you!");
                else
                    System.out.println("filemanagerstring is the right one for you!");
            }
            
            else if (requestCode == USE_CAMERA) {
            	//no need to do anything for camera useage
            }
        }
    }

    //UPDATED!
    public String getPath(Uri uri) {
        String[] projection = { MediaStore.Images.Media.DATA };
        Cursor cursor = managedQuery(uri, projection, null, null, null);
        if(cursor!=null)
        {
            //HERE YOU WILL GET A NULLPOINTER IF CURSOR IS NULL
            //THIS CAN BE, IF YOU USED OI FILE MANAGER FOR PICKING THE MEDIA
            int column_index = cursor
            .getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            cursor.moveToFirst();
            return cursor.getString(column_index);
        }
        else return null;
    }


    LocationListener onLocationChange=new LocationListener() {
        public void onLocationChanged(Location fix) {
        	latitude = fix.getLatitude();
            longitude = fix.getLongitude();

            System.out.println("Latitude = " + latitude);
    		System.out.println("Longitude = " + longitude);
            
            //locationManager.removeUpdates(onLocationChange);
        }
        
        public void onProviderDisabled(String provider) {
          // required for interface, not used
        }
        
        public void onProviderEnabled(String provider) {
          // required for interface, not used
        }
        
        public void onStatusChanged(String provider, int status,
                                      Bundle extras) {
          // required for interface, not used
        }
      };    
    
}

