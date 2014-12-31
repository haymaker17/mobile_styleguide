package com.concur.mobile.platform.ui.travel.hotel.fragment;

import java.util.ArrayList;
import java.util.List;

import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;
import android.widget.TextView;

import com.concur.mobile.platform.travel.search.hotel.HotelImagePair;
import com.concur.mobile.platform.ui.common.fragment.PlatformFragmentV1;
import com.concur.mobile.platform.ui.common.view.ListItemAdapter;

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
        // View mainView = inflater.inflate(R.layout.hotel_images_layout, null, false);
        GridView gridView = new GridView(getActivity().getApplicationContext()); // mainView.findViewById(R.id.gridview);

        // if (config.orientation == Configuration.ORIENTATION_LANDSCAPE) {
        // gridView.setNumColumns(3);
        // } else {
        gridView.setNumColumns(2);
        // }
        gridView.setHorizontalSpacing(4);
        gridView.setVerticalSpacing(4);
        gridView.setPadding(6, 12, 12, 12);

        gridView.setOnItemClickListener(new OnItemClickListener() {

            public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
                // this 'mActivity' parameter is Activity object, you can send the current activity.
                // Intent i = new Intent(mActivity, ActvityToCall.class);
                // mActivity.startActivity(i);
                // ImageView icon = (ImageView) findViewById(R.id.myImage);
                // BitmapFactory.Options options = new BitmapFactory.Options();
                // options.inTempStorage = new byte[3*1024];
                //
                // Bitmap ops = BitmapFactory.decodeFile(path, options);
                // icon.setImageBitmap(ops)
            }
        });

        // Configuration config = getResources().getConfiguration();
        // if (config.orientation == Configuration.ORIENTATION_LANDSCAPE) {
        // gridView.setNumColumns(3);
        // } else {
        // gridView.setNumColumns(2);
        // }
        // gridView.setPadding(6, 12, 12, 12);
        if (imagePairs != null && imagePairs.size() > 0) {

            listItemAdapater = new ListItemAdapter<ImageListItem>(getActivity().getApplicationContext(), imagePairs);
            gridView.setAdapter(listItemAdapater);

            return gridView;
        } else {
            TextView tv = new TextView(getActivity());
            tv.setTextSize(25);
            tv.setGravity(Gravity.CENTER_VERTICAL);
            tv.setText("  No Photos Available");
            return tv;

        }
    }
}
