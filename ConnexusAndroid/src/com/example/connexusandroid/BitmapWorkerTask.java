package com.example.connexusandroid;

import java.lang.ref.WeakReference;

import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.widget.ImageView;

class BitmapWorkerTask extends AsyncTask<String, Void, Bitmap> {
    private final WeakReference<ImageView> imageViewReference;
    private String data = null;

    public BitmapWorkerTask(ImageView imageView) {
        // Use a WeakReference to ensure the ImageView can be garbage collected
        imageViewReference = new WeakReference<ImageView>(imageView);
    }

    // Decode image in background.
    @Override
    protected Bitmap doInBackground(String... params) {
        data = params[0];
        DecodeImage imageDecoder = new DecodeImage();
        //We want to downsample the image to keep performance reasonable
        return imageDecoder.decodeSampledBitmapFromStream(data, 100, 100);
    }

    // Once complete, see if ImageView is still around and set bitmap.
    @Override
    protected void onPostExecute(Bitmap bitmap) {      
        if (isCancelled()) {
            bitmap = null;
        }

        if (imageViewReference != null && bitmap != null) {
            final ImageView imageView = imageViewReference.get();
            final BitmapWorkerTask bitmapWorkerTask =
                    getBitmapWorkerTask(imageView);
            if (this == bitmapWorkerTask && imageView != null) {
                imageView.setImageBitmap(bitmap);
            }
        }
    }
    
    private static BitmapWorkerTask getBitmapWorkerTask(ImageView imageView) {
    	   if (imageView != null) {
    	       final Drawable drawable = imageView.getDrawable();
    	       if (drawable instanceof AsyncDrawable) {
    	           final AsyncDrawable asyncDrawable = (AsyncDrawable) drawable;
    	           return asyncDrawable.getBitmapWorkerTask();
    	       }
    	    }
    	    return null;
    	}

	public static boolean cancelPotentialWork(String data, ImageView imageView) {
	    final BitmapWorkerTask bitmapWorkerTask = getBitmapWorkerTask(imageView);

	    if (bitmapWorkerTask != null) {
	        final String bitmapData = bitmapWorkerTask.data;
	        if (bitmapData != data) {
	            // Cancel previous task
	            bitmapWorkerTask.cancel(true);
	        } else {
	            // The same work is already in progress
	            return false;
	        }
	    }
	    // No task associated with the ImageView, or an existing task was cancelled
	    return true;
	}
    
    
    static class AsyncDrawable extends BitmapDrawable {
        private final WeakReference<BitmapWorkerTask> bitmapWorkerTaskReference;

        public AsyncDrawable(String url, Bitmap bitmap,
                BitmapWorkerTask bitmapWorkerTask) {
            super(bitmap);
            bitmapWorkerTaskReference =
                new WeakReference<BitmapWorkerTask>(bitmapWorkerTask);
        }
        
        public BitmapWorkerTask getBitmapWorkerTask() {
            return bitmapWorkerTaskReference.get();
        }

    }
}

