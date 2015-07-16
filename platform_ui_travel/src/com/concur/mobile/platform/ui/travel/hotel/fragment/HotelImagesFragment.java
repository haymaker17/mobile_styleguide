package com.concur.mobile.platform.ui.travel.hotel.fragment;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import com.concur.mobile.platform.travel.search.hotel.HotelImagePair;
import com.concur.mobile.platform.ui.common.fragment.PlatformFragmentV1;
import com.concur.mobile.platform.ui.common.util.ImageCache;
import com.concur.mobile.platform.ui.common.util.ImageCacheReceiver;
import com.concur.mobile.platform.ui.travel.R;
import com.concur.mobile.platform.ui.travel.activity.BaseActivity;
import com.concur.mobile.platform.ui.travel.hotel.activity.ImageDetailActivity;
import com.concur.mobile.platform.ui.travel.hotel.fragment.HotelChoiceDetailsFragment.HotelChoiceDetailsFragmentListener;
import com.concur.mobile.platform.ui.travel.util.Const;

import java.net.URI;
import java.util.List;

public class HotelImagesFragment extends PlatformFragmentV1 implements AdapterView.OnItemClickListener {

    private GridView mGridView;
    private List<HotelImagePair> images;
    private HotelChoiceDetailsFragmentListener callBackListener;
    private ImageAdapter imgAdapter;

    public HotelImagesFragment() {
        super();
    }

    public HotelImagesFragment(List<HotelImagePair> images) {
        this.images = images;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.hotel_images_layout, container, false);

        mGridView = (GridView) view.findViewById(R.id.gridview);
        Activity currentActivity = getActivity();
        imgAdapter = new ImageAdapter(currentActivity);

        // Prior to setting the adapter on the view, init the image
        // cache receiver to handle
        // updating the list based on images downloaded asynchronously.
        ((BaseActivity) currentActivity).setImageCacheReceiver(new ImageCacheReceiver(imgAdapter, mGridView));
        ((BaseActivity) currentActivity).registerImageCacheReceiver();

        // initialize your grid view
        mGridView.setAdapter(imgAdapter);

        mGridView.setOnItemClickListener(this);
        if (images == null || images.size() == 0) {
            TextView tv = (TextView) view.findViewById(R.id.no_photos);
            mGridView.setVisibility(View.GONE);
            tv.setVisibility(View.VISIBLE);
        }

        return view;

    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        // This makes sure that the container activity has implemented
        // the callback interface. If not, it throws an exception
        try {
            callBackListener = (HotelChoiceDetailsFragmentListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement HotelSearchResultsFragmentListener");
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        // TODO Auto-generated method stub
        super.onSaveInstanceState(outState);

        Log.d(Const.LOG_TAG, " ***** HotelChoiceDetailsFragment, in onSaveInstanceState *****  ");
    }

    @Override
    public void onPause() {
        // TODO Auto-generated method stub
        super.onPause();

        Log.d(Const.LOG_TAG, " ***** HotelChoiceDetailsFragment, in onPause *****  ");

    }

    @Override
    public void onResume() {
        // TODO Auto-generated method stub
        super.onResume();

    }

    @Override
    public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
        final Intent i = new Intent(getActivity(), ImageDetailActivity.class);
        callBackListener.onImageClicked(v, (int) id);
    }

    /**
     * @return an adapter for the grid view
     */
    public BaseAdapter getAdapter(Context ctx) {
        return new ImageAdapter(ctx);
    }

    static class ViewHolder {

        // TextView imageTitle;
        ImageView image;
    }

    public class ImageAdapter extends BaseAdapter {

        private Context mContext;

        public ImageAdapter(Context c) {
            mContext = c;
            // mItems = items;
        }

        public int getCount() {
            return images != null ? images.size() : 0;
        }

        public Object getItem(int position) {
            return images.get(position);
        }

        // create a new ImageView for each item referenced by the Adapter
        public View getView(int position, View convertView, ViewGroup parent) {

            View cell = convertView;
            ViewHolder holder = null;

            if (cell == null) {
                LayoutInflater inflater = ((Activity) mContext).getLayoutInflater();
                cell = inflater.inflate(R.layout.image_view, parent, false);
                holder = new ViewHolder();
                holder.image = (ImageView) cell.findViewById(R.id.picture);
                cell.setTag(holder);
            } else {
                holder = (ViewHolder) cell.getTag();
            }
            if (images != null && images.size() > 0) {
                // Set the list item tag to the uri, this tag value is used in 'ListItemAdapter.refreshView'
                // to refresh the appropriate view items once images have been loaded.
                URI uri = URI.create(images.get(position).image);
                // Attempt to load the image from the image cache, if not there, then the
                // ImageCache will load it asynchronously and this view will be updated via
                // the ImageCache broadcast receiver available in BaseActivity.
                ImageCache imgCache = ImageCache.getInstance(mContext);
                Bitmap bitmap = imgCache.getBitmap(uri, null);
                if (bitmap != null) {
                    holder.image.setScaleType(ImageView.ScaleType.FIT_XY);
                    holder.image.setBackgroundColor(getResources().getColor(R.color.white_background));
                    holder.image.setImageBitmap(bitmap);
                } else {
                    holder.image.setScaleType(ImageView.ScaleType.FIT_CENTER);
                    holder.image.setBackgroundColor(getResources().getColor(R.color.grey_view_background));
                    holder.image.setImageResource(R.drawable.hotel_results_default_image);
                }

            }
            return cell;
        }

        @Override
        public long getItemId(int arg0) {
            return arg0;
        }
    }
}
