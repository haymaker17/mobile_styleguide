package com.concur.mobile.platform.ui.travel.hotel.fragment;

import java.io.Serializable;
import java.net.URI;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.concur.mobile.platform.travel.search.hotel.HotelImagePair;
import com.concur.mobile.platform.ui.common.util.ImageCache;
import com.concur.mobile.platform.ui.common.view.ListItem;
import com.concur.mobile.platform.ui.travel.R;
import com.concur.mobile.platform.ui.travel.hotel.activity.ImageActivity;
import com.concur.mobile.platform.ui.travel.hotel.fragment.HotelChoiceDetailsFragment.HotelChoiceDetailsFragmentListener;
import com.concur.mobile.platform.ui.travel.util.Const;

/**
 * An extension of <code>ListItem</code> for displaying a Image.
 * 
 * @author tejoa
 * 
 */
public class ImageListItem extends ListItem implements Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = 65070319994788368L;
    private HotelImagePair hotelImage;
    private View hotelImageView;
    private HotelChoiceDetailsFragmentListener callBackListener;

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
    public View buildView(final Context context, final View convertView, ViewGroup parent) {
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
                thumbNailImg.setOnClickListener(new OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(context, ImageActivity.class);
                        intent.putExtra(Const.EXTRA_IMAGE_URL, hotelImage.image);
                        // hotelImageView.getContext().startActivity(intent);
                        // int id = v.getId();
                        // Toast.makeText(context, "hotelImageView" + id, Toast.LENGTH_SHORT).show(); // TODO Auto-generated
                        // method
                        // stub

                    }
                });

            }

        }
        return hotelImageView;
    }

    @Override
    public boolean isEnabled() {
        // TODO Auto-generated method stub
        return false;
    }

    // public void previewPhoto(View view) {
    // ((ImageView) view).setScaleType(ScaleType.FIT_XY);
    // Intent intent = new Intent(view.getContext(), ImageActivity.class);
    // intent.putExtra(Const.EXTRA_IMAGE_URL, hotelImage.image);
    // view.getContext().startActivity(intent);
    //
    // // return view;
    // }
}