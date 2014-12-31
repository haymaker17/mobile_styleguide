/**
 * 
 */
package com.concur.mobile.core.travel.activity;

import java.net.URI;
import java.util.ArrayList;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Gallery;
import android.widget.ImageView;

import com.concur.core.R;
import com.concur.mobile.core.travel.data.ImagePair;
import com.concur.mobile.core.view.AsyncImageView;

/**
 * An extension of <code>BaseAdapter</code> used to provide instances of <code>AsyncImageView</code>.
 * 
 * @author AndrewK
 */
public class AsyncImageAdapter extends BaseAdapter {

    // private int mGalleryItemBackground;

    private ArrayList<ImagePair> imgPairs;

    private Context context;

    public AsyncImageAdapter(Context context, ArrayList<ImagePair> imgPairs) {
        this.context = context;
        this.imgPairs = imgPairs;
    }

    public int getCount() {
        int count = 0;
        if (imgPairs != null) {
            count = imgPairs.size();
        }
        return count;
    }

    public Object getItem(int position) {
        ImagePair imgPair = null;
        if (imgPairs != null) {
            imgPair = imgPairs.get(position);
        }
        return imgPair;
    }

    public long getItemId(int position) {
        return position;
    }

    public View getView(int position, View convertView, ViewGroup parent) {

        AsyncImageView asyncImageView = new AsyncImageView(context);
        asyncImageView.setAsyncUri(URI.create(imgPairs.get(position).thumbnail));
        asyncImageView.setLayoutParams(new Gallery.LayoutParams(200, 150));
        asyncImageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
        asyncImageView.setBackgroundResource(R.color.Black);

        return asyncImageView;
    }

}
