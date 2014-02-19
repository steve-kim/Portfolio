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
import com.google.common.collect.Lists;

public class SingleStreamAdapter extends BaseAdapter{
	private Context mContext;
	private ConnexusPhoto.List photosToShow;
	private int subsets;
	private int photoSubsetsIndex;
	
	
	//Constructors
	//Constructor
	public SingleStreamAdapter (Context c) {
		mContext = c;
		photosToShow = ViewSingleStream.getPhotos();
		
		if (photosToShow.isEmpty()) {
			mThumbIds = null;
			return;
		}
		
		photoSubsets = Lists.partition(photosToShow, 16);
		
		subsets = photoSubsets.size();
		photoSubsetsIndex = 0;
		
		mThumbIds = photoSubsets.get(photoSubsetsIndex);
		photoSubsetsIndex = photoSubsetsIndex + 1;
			
	}
	
	
	public void setNextPictureSet () {
		if (photoSubsetsIndex < subsets) {
			mThumbIds = photoSubsets.get(photoSubsetsIndex);
			photoSubsetsIndex = photoSubsetsIndex + 1;
		} else {
			//We have reached end of subsets, reset back to beginning
			photoSubsetsIndex = 0;
			mThumbIds = photoSubsets.get(photoSubsetsIndex);
		}
		
	}
	
	public int getCount() {
		if (mThumbIds != null) {
			return mThumbIds.size();	
		} else {
			return 0;
		}
		
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
		//ConnexusStream.List streamsToShow = ViewAllStreams.getStreams();
		
		if (convertView == null) {
			imageView = new ImageView(mContext);
			imageView.setLayoutParams(new GridView.LayoutParams(100, 100));
			imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
			imageView.setPadding(8, 8, 8, 8);
		} else {
			imageView = (ImageView) convertView;
		}
		
		if (mThumbIds != null) {
			loadBitmap(mThumbIds.get(position).bkUrl, imageView);	
		}
		
		
		return imageView;
		
		
	}

	/*public long getStreamId(int position) {
		return mThumbIds.get(position).id;
	}*/
	
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
	private List<ConnexusPhoto> mThumbIds;
	private List<List<ConnexusPhoto>> photoSubsets;
	

}
