package com.concur.mobile.platform.ui.travel.hotel.fragment;

import java.net.URI;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;

import com.concur.mobile.platform.travel.search.hotel.HotelImagePair;
import com.concur.mobile.platform.ui.common.util.ImageCache;
import com.concur.mobile.platform.ui.common.view.ListItem;
import com.concur.mobile.platform.ui.travel.R;

/**
 * An extension of <code>ListItem</code> for displaying a Image.
 * 
 * @author tejoa
 * 
 */
public class ImageListItem extends ListItem {

    private HotelImagePair hotelImage;
    private View hotelImageView;

    public HotelImagePair getHotelImage() {
        return hotelImage;
    }

    public void setHotelImage(HotelImagePair hotelImage) {
        this.hotelImage = hotelImage;
    }

    public ImageListItem(HotelImagePair hotelImage) {
        this.hotelImage = hotelImage;
    }

    @Override
    public View buildView(Context context, View convertView, ViewGroup parent) {
        LayoutInflater inflater = null;

        if (convertView == null) {
            inflater = LayoutInflater.from(context);
            hotelImageView = inflater.inflate(R.layout.image_view, null, false);
        } else {
            hotelImageView = convertView;
        }

        // Set the vendor image, or hide it and set the vendor name.
        if (hotelImage != null && hotelImage.image != null) {

            hotelImageView.setTag(R.id.picture, hotelImageView.findViewById(R.id.picture));
            ImageView thumbNailImg = (ImageView) hotelImageView.findViewById(R.id.picture);
            if (thumbNailImg != null) {
                thumbNailImg.setVisibility(View.VISIBLE);
                // Set the list item tag to the uri, this tag value is used in 'ListItemAdapter.refreshView'
                // to refresh the appropriate view items once images have been loaded.
                URI uri = URI.create(hotelImage.image);
                listItemTag = uri;
                // Attempt to load the image from the image cache, if not there, then the
                // ImageCache will load it asynchronously and this view will be updated via
                // the ImageCache broadcast receiver available in BaseActivity.
                ImageCache imgCache = ImageCache.getInstance(context);
                Bitmap bitmap = imgCache.getBitmap(uri, null);
                if (bitmap != null) {
                    thumbNailImg.setImageBitmap(bitmap);
                }

            }

        }
        return hotelImageView;
    }

    @Override
    public boolean isEnabled() {
        // TODO Auto-generated method stub
        return false;
    }

    public View previewPhoto(View view) {
        ((ImageView) hotelImageView).setScaleType(ScaleType.FIT_XY);

        // fullScreenIntent = new Intent(view.getContext(),FullImageActivity.class);
        // fullScreenIntent.putExtra
        // ProfilePageNormalUser.this.startActivity(fullScreenIntent);
        return hotelImageView;
    }
}