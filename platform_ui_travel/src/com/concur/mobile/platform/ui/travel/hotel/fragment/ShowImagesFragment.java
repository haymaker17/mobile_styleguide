package com.concur.mobile.platform.ui.travel.hotel.fragment;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;
import android.widget.TextView;

import com.concur.mobile.platform.travel.search.hotel.HotelImagePair;
import com.concur.mobile.platform.ui.common.fragment.PlatformFragmentV1;
import com.concur.mobile.platform.ui.common.view.ListItemAdapter;
import com.concur.mobile.platform.ui.travel.R;
import com.concur.mobile.platform.ui.travel.hotel.fragment.HotelChoiceDetailsFragment.HotelChoiceDetailsFragmentListener;
import com.concur.mobile.platform.ui.travel.util.Const;

/**
 * fragment for photos tab
 * 
 * @author tejoa
 * 
 */
public class ShowImagesFragment extends PlatformFragmentV1 {

    // private ListView hotelImageView;
    private List<ImageListItem> imagePairs;
    private ImageListItem item;
    private ListItemAdapter<ImageListItem> listItemAdapater;
    private GridView gridView;
    private HotelChoiceDetailsFragmentListener callBackListener;

    public ShowImagesFragment(List<HotelImagePair> images) {
        imagePairs = new ArrayList<ImageListItem>();
        for (HotelImagePair image : images) {
            item = new ImageListItem(image);
            imagePairs.add(item);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        super.onCreateView(inflater, container, savedInstanceState);

        // inflate the details fragment
        View mainView = inflater.inflate(R.layout.hotel_images_layout, container, false);
        if (imagePairs != null && imagePairs.size() > 0) {
            gridView = (GridView) mainView.findViewById(R.id.gridview);
            // Intent i = new Intent(getActivity(), ImageActivity.class);
            // Bundle bundle = new Bundle();
            // bundle.putSerializable(Const.EXTRA_HOTEL_IMAGES, (Serializable) imagePairs);
            listItemAdapater = new ListItemAdapter<ImageListItem>(getActivity().getApplicationContext(), imagePairs);
            gridView.setAdapter(listItemAdapater);
            // gridView.setOnItemClickListener(this);

            // gridView.setOnItemClickListener(new OnItemClickListener() {
            //
            // @Override
            // public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
            //
            // Toast.makeText(getActivity(), position, Toast.LENGTH_SHORT).show();
            // // ImageListItem imageItem = (ImageListItem) parent.getItemAtPosition(position);
            // // callBackListener.onImageClicked(imageItem);
            // }
            // });

        } else {

            TextView tv = (TextView) mainView.findViewById(R.id.no_photos);
            tv.setVisibility(View.VISIBLE);

        }
        return mainView;
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

        // retainer.put(STATE_HOTEL_LIST_ITEMS_KEY, hotelListItems);
    }

    @Override
    public void onResume() {
        // TODO Auto-generated method stub
        super.onResume();

        // if (retainer.contains(STATE_HOTEL_LIST_ITEMS_KEY)) {
        // hotelListItems = (List<HotelSearchResultListItem>) retainer.get(STATE_HOTEL_LIST_ITEMS_KEY);
        // }

        // Log.d(Const.LOG_TAG, " ***** HotelSearchResultFragment, in onResume *****  hotelListItems = "
        // + (hotelListItems != null ? hotelListItems.size() : 0));
    }

    // @Override
    // public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
    //
    // ImageListItem imageItem = (ImageListItem) parent.getItemAtPosition(position);
    // callBackListener.onImageClicked(imageItem);
    // // final Intent i = new Intent(getActivity(), ImageDetailActivity.class);
    // i.putExtra(ImageDetailActivity.EXTRA_IMAGE, (int) id);
    // if (Utils.hasJellyBean()) {
    // // makeThumbnailScaleUpAnimation() looks kind of ugly here as the loading spinner may
    // // show plus the thumbnail image in GridView is cropped. so using
    // // makeScaleUpAnimation() instead.
    // ActivityOptions options = ActivityOptions.makeScaleUpAnimation(v, 0, 0, v.getWidth(), v.getHeight());
    // getActivity().startActivity(i, options.toBundle());
    // } else {
    // startActivity(i);
    // }

    // }

}
