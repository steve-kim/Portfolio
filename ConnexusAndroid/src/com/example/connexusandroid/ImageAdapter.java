package com.example.connexusandroid;

import java.util.List;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;

import com.example.connexusandroid.BitmapWorkerTask.AsyncDrawable;

public class ImageAdapter extends BaseAdapter{
	private Context mContext;
	private ConnexusStream.List streamsToShow;
	private ConnexusPhoto.List photosToShow;
	
	//Constructors
	//Constructor
	public ImageAdapter (Context c) {
		mContext = c;
		streamsToShow = ViewAllStreams.getStreams();
		
		//We will show 16 images at a time
		if (streamsToShow.size() >= 16) {
			mThumbIds = streamsToShow.subList(0, 16);
		} else {
			mThumbIds = streamsToShow;
		}
			
	}
	
	public int getCount() {
		return mThumbIds.size();
	}
	
	public Object getItem(int position) {
		return null;
	}
	
	public long getItemId(int position) {
		return 0;
	}
	
	public View getView(int position, View convertView, ViewGroup parent) {
		Bitmap bm;
		//StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
		//StrictMode.setThreadPolicy(policy);
		//DecodeImage imageDecoder = new DecodeImage();
		ImageView imageView = new ImageView(mContext);
		ConnexusStream.List streamsToShow = ViewAllStreams.getStreams();
		
		if (convertView == null) {
			imageView = new ImageView(mContext);
			imageView.setLayoutParams(new GridView.LayoutParams(100, 100));
			imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
			imageView.setPadding(8, 8, 8, 8);
		} else {
			imageView = (ImageView) convertView;
		}
		
		loadBitmap(mThumbIds.get(position).coverImageUrl, imageView);
		
		return imageView;
		
		
	}

	public long getStreamId(int position) {
		return mThumbIds.get(position).id;
	}
	
	public String getStreamName(int position) {
		return mThumbIds.get(position).name;
	}
	
	public void loadBitmap(String url, ImageView imageView) {
		final BitmapWorkerTask task = new BitmapWorkerTask(imageView);
		if (task.cancelPotentialWork(url, imageView)) {
	        Bitmap mPlaceHolderBitmap = null;
			final AsyncDrawable asyncDrawable =
	                new AsyncDrawable(url, mPlaceHolderBitmap , task);
	        imageView.setImageDrawable(asyncDrawable);
	        task.execute(url);
	    }
	}
	
	//references to our images
	//private List<ConnexusStream> streamIds;
	//private List<String> mThumbIds;
	//private List<ConnexusPhotos> pThumbIds;
	private List<ConnexusStream> mThumbIds;
	

}
