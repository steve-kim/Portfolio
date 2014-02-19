package com.example.connexusandroid;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLConnection;

import org.apache.commons.io.output.ByteArrayOutputStream;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicHeader;
import org.apache.http.protocol.HTTP;

import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.util.Log;

import com.google.gson.Gson;

public class DecodeImage {
	private String TAG;
	private Rect outPadding;
	//private BitmapFactory.Options options;
	private InputStream is;
	private BufferedInputStream bis;
	private String streamId, streamName, uploadApiUrl;
	//private String apiUrl = "http://apt-connexus.appspot.com/UploadServletAPI?streamId=" 
	//		+  streamId
	//		+ "&streamName="
	//		+ streamName;
	
	public DecodeImage() {
		//this.options = new BitmapFactory.Options();
		this.outPadding = new Rect();
		this.is = null;
	}
	
	//This function retrives pictures from the web service
	//We ended up not using this function, we can remove it later.
	private Bitmap getImageBitmap(String url) {
	    Bitmap bm = null;
	    try {
	        URL aURL = new URL(url);
	        URLConnection conn = aURL.openConnection();
	        conn.connect();
	        InputStream is = conn.getInputStream();
	        BufferedInputStream bis = new BufferedInputStream(is);
	        bm = BitmapFactory.decodeStream(bis);
	        bis.close();
	        is.close();
	    } catch (IOException e) {
			Log.e(TAG, "Error getting bitmap", e);
	    }
	    return bm;
	} 
	
	private void openInputStream(String url) {
		try {
			//if (url.equals("") == false) {
				URL aURL = new URL(url);
				URLConnection conn = aURL.openConnection();
				conn.connect();
				this.is = conn.getInputStream();
				this.bis = new BufferedInputStream(this.is);
			//} else {
			//	return;
			//}
		} catch (IOException e) {
			Log.e(TAG, "Error getting bitmap", e);
		}
		
	}
	
	private void closeInputStream(InputStream is, BufferedInputStream bis) {
		try {
			bis.close();
			is.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private int calculateInSampleSize(
            BitmapFactory.Options options, int reqWidth, int reqHeight) {
		// Raw height and width of image
		final int height = options.outHeight;
		final int width = options.outWidth;
		int inSampleSize = 1;

		if (height > reqHeight || width > reqWidth) {

			// Calculate ratios of height and width to requested height and width
			final int heightRatio = Math.round((float) height / (float) reqHeight);
			final int widthRatio = Math.round((float) width / (float) reqWidth);

			// Choose the smallest ratio as inSampleSize value, this will guarantee
			// a final image with both dimensions larger than or equal to the
			// requested height and width.
			inSampleSize = heightRatio < widthRatio ? heightRatio : widthRatio;
		}

		return inSampleSize;
	}
	
	//Samples the selected bitmap into something the phone can use
	public Bitmap decodeSampledBitmapFromStream(String URL,
	        int reqWidth, int reqHeight) {

	    // First decode with inJustDecodeBounds=true to check dimensions
	    final BitmapFactory.Options options = new BitmapFactory.Options();
	    
	    openInputStream(URL);
	    
	    if (this.is != null) {
	    	options.inJustDecodeBounds = true;
		    BitmapFactory.decodeStream(this.bis, this.outPadding, options);

		    // Calculate inSampleSize
		    options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

		    // Decode bitmap with inSampleSize set
		    options.inJustDecodeBounds = false;
		    //We cannot reuse the same input stream, must close and re-open again
		    closeInputStream(this.is, this.bis);
		    openInputStream(URL);
		    final Bitmap bm = BitmapFactory.decodeStream(this.bis, null, options);
		    closeInputStream(this.is, this.bis);
		    
		    return bm;
	    } else {
	    	//closeInputStream(this.is, this.bis);
	    	return null;
	    }
	    
	}
	
	//Takes image from Phone and uploads it to the server
	public void decodeImageFromPhone(String uri, String selectedStreamId, String selectedStreamName, double latitude, double longitude) throws UnsupportedEncodingException {
		HttpClient httpClient = new DefaultHttpClient();
		
		streamId = selectedStreamId;
		streamName = selectedStreamName;
		
		uploadApiUrl = "http://apt-connexus.appspot.com/UploadServletAPI?streamId=" + streamId + "&streamName=" + streamName;
		HttpPost postRequest = new HttpPost(uploadApiUrl);
		
		
		/*MultipartEntity reqEntity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);
		try {
			reqEntity.addPart("streamId", new StringBody(streamId));
		} catch (UnsupportedEncodingException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		try {
			reqEntity.addPart("streamName", new StringBody(streamName));
		} catch (UnsupportedEncodingException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}*/
		
		byte[] imageByteArray = {};
		
		//Decode the image into something we can use
		//We don't really need this part, we can probably just comment this code out
		try {
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			BitmapFactory.decodeFile(uri).compress(CompressFormat.JPEG, 75, bos);
			imageByteArray = bos.toByteArray();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		//Set image to JSON format to send to web service
		Image img = new Image(latitude, longitude, imageByteArray);
		Gson gson = new Gson();
		String tstJson = gson.toJson(img);
		StringEntity se = new StringEntity(tstJson);
		se.setContentEncoding(new BasicHeader(HTTP.CONTENT_TYPE, "application/json"));
		postRequest.setEntity(se);
		
		//Send the image to the web service for upload
		try {
			httpClient.execute(postRequest);
		} catch (ClientProtocolException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
}
