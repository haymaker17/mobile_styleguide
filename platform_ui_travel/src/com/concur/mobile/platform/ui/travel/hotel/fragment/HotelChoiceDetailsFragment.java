package com.concur.mobile.platform.ui.travel.hotel.fragment;

import java.net.URI;
import java.util.List;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TabHost;
import android.widget.TabHost.OnTabChangeListener;
import android.widget.TabHost.TabSpec;
import android.widget.TabWidget;
import android.widget.TextView;

import com.concur.mobile.platform.travel.search.hotel.HotelImagePair;
import com.concur.mobile.platform.ui.common.fragment.PlatformFragmentV1;
import com.concur.mobile.platform.ui.common.util.ImageCache;
import com.concur.mobile.platform.ui.travel.R;
import com.concur.mobile.platform.ui.travel.util.Const;
import com.google.android.gms.maps.model.LatLng;

/**
 * Fragment for Hotel Choice Details screen with tabs
 * 
 * @author tejoa
 * 
 */

public class HotelChoiceDetailsFragment extends PlatformFragmentV1 implements OnTabChangeListener {

    public static final String CLS_TAG = HotelChoiceDetailsFragment.class.getSimpleName();

    public static final String TAB_DETAILS = "DETAILS";
    public static final String TAB_ROOMS = "ROOMS";
    public static final String TAB_IMAGES = "IMAGES";

    private HotelSearchResultListItem hotelListItem;
    private HotelChoiceDetailsFragmentListener callBackListener;

    private View mRoot;
    public TabHost mTabHost;
    public int mCurrentTab;

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
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setRetainInstance(true);

        mTabHost.setOnTabChangedListener(this);
        mCurrentTab = 1;
        mTabHost.setCurrentTab(mCurrentTab);
        // manually start loading stuff in the first tab
        // callBackListener.updateTab(TAB_ROOMS, R.id.tab_rooms);
        Log.d(Const.LOG_TAG, " ***** HotelChoiceDetailsFragment, in onSaveInstanceState *****  ");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        // inflate the details fragment
        mRoot = inflater.inflate(R.layout.hotel_choice_list_item, container, false);
        // mRoot = inflater.inflate(R.layout.hotel_choice_list_item, container, false);

        View hotelView = mRoot.findViewById(R.id.hotel_row);
        // View hotelView = inflater.inflate(R.layout.hotel_search_result_row, null);
        Intent i = getActivity().getIntent();
        final Bundle bundle = i.getExtras();
        // hotel = new HotelSearchResultListItem();
        hotelListItem = (HotelSearchResultListItem) bundle.getSerializable(Const.EXTRA_HOTELS_DETAILS);
        setActionBar();
        // TODO don't show rate
        hotelListItem.getHotel().lowestRate = null;

        ((HotelSearchResultListItem) hotelListItem).buildView(getActivity().getApplicationContext(), hotelView, null);
        showHideHomeImage();
        // mTabHost =
        setupTabs();

        return mRoot;
    }

    private void setActionBar() {
        ActionBar actionBar = getActivity().getActionBar();
        actionBar.setTitle(hotelListItem.getHotel().name);

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

    private void setupTabs() {
        mTabHost = (TabHost) mRoot.findViewById(android.R.id.tabhost);
        mTabHost.setup(); // you must call this before adding your tabs!
        mTabHost.getTabWidget().setShowDividers(TabWidget.SHOW_DIVIDER_MIDDLE);
        // mTabHost.getTabWidget().setDividerDrawable(R.drawable.tab_divider);

        mTabHost.addTab(newTab(TAB_DETAILS, R.string.hotel_tab_details, R.id.tab_details));
        mTabHost.addTab(newTab(TAB_ROOMS, R.string.hotel_tab_rooms, R.id.tab_rooms));
        mTabHost.addTab(newTab(TAB_IMAGES, R.string.hotel_tab_images, R.id.tab_images));
        // manually start loading stuff in the first tab
        // callBackListener.updateTab(TAB_ROOMS, R.id.tab_rooms);
    }

    private TabSpec newTab(String tag, int labelId, int tabContentId) {
        Log.d(CLS_TAG, "buildTab(): tag=" + tag);

        View indicator = LayoutInflater.from(getActivity()).inflate(R.layout.tab,
                (ViewGroup) mRoot.findViewById(android.R.id.tabs), false);
        TextView tv = ((TextView) indicator.findViewById(R.id.text));
        tv.setText(labelId);
        // setting tabs width to 1/3
        DisplayMetrics displaymetrics = new DisplayMetrics();
        getActivity().getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
        int screenWidth = displaymetrics.widthPixels;
        indicator.setMinimumWidth(screenWidth / 3);
        TabSpec tabSpec = mTabHost.newTabSpec(tag);
        tabSpec.setIndicator(indicator);
        tabSpec.setContent(tabContentId);
        return tabSpec;
    }

    //
    // @Override
    // public void onTabChanged(String tabId) {
    // Log.d(CLS_TAG, "onTabChanged(): tabId=" + tabId);
    // if (TAB_DETAILS.equals(tabId)) {
    // updateTab(tabId, R.id.tab_details);
    // mCurrentTab = 0;
    // return;
    // }
    // if (TAB_ROOMS.equals(tabId)) {
    // updateTab(tabId, R.id.tab_rooms);
    // mCurrentTab = 1;
    // return;
    // }
    // if (TAB_IMAGES.equals(tabId)) {
    // updateTab(tabId, R.id.tab_images);
    // mCurrentTab = 2;
    // return;
    // }
    // }
    //
    // private void updateTab(String tabId, int placeholder) {
    // FragmentManager fm = getFragmentManager();
    // if (fm.findFragmentByTag(tabId) == null) {
    // Hotel hotel = hotelListItem.getHotel();
    // if (TAB_DETAILS.equals(tabId)) {
    // fm.beginTransaction().replace(placeholder, new HotelDetailsFragment(hotel), tabId).commit();
    // }
    // if (TAB_ROOMS.equals(tabId)) {
    // fm.beginTransaction().replace(placeholder, new HotelRoomDetailFragment(hotel.rates), tabId).commit();
    // }
    // if (TAB_IMAGES.equals(tabId)) {
    // fm.beginTransaction().replace(placeholder, new ShowImagesFragment(hotel.imagePairs), tabId).commit();
    // }
    // }
    // }

    // Container Activity must implement this call back interface
    public interface HotelChoiceDetailsFragmentListener {

        public void updateTab(String tabId, int placeholder);

        public void onImageClicked(ImageListItem imageListItem);

        public void onFindRoomsClicked();

        public void roomItemClicked(HotelRoomListItem roomListItem);

        public void onMapsClicked(LatLng post);

    }

    @Override
    public void onTabChanged(String tabId) {
        Log.d(CLS_TAG, "onTabChanged(): tabId=" + tabId);
        if (TAB_DETAILS.equals(tabId)) {
            callBackListener.updateTab(tabId, R.id.tab_details);
            mCurrentTab = 0;
            return;
        }
        if (TAB_ROOMS.equals(tabId)) {
            callBackListener.updateTab(tabId, R.id.tab_rooms);
            mCurrentTab = 1;
            return;
        }
        if (TAB_IMAGES.equals(tabId)) {
            callBackListener.updateTab(tabId, R.id.tab_images);
            mCurrentTab = 2;
            return;
        }

    }

    /**
     * 
     * Show or Hide Home Image based on orientation change.
     * 
     */
    private void showHideHomeImage() {
        // Setup the cityscape image switcher and put the placeholder image in place
        ImageView headerImage = (ImageView) mRoot.findViewById(R.id.travelCityscape);
        List<HotelImagePair> imagepairs = hotelListItem.getHotel().imagePairs;
        int orientation = getResources().getConfiguration().orientation;
        switch (orientation) {
        case Configuration.ORIENTATION_PORTRAIT:
            headerImage.setVisibility(View.VISIBLE);
            if (headerImage != null && imagepairs != null && imagepairs.size() > 0) {
                HotelImagePair image2 = imagepairs.get(1);
                URI uri = URI.create(image2.image);
                ImageCache imgCache = ImageCache.getInstance(getActivity());
                Bitmap bitmap = imgCache.getBitmap(uri, null);
                if (bitmap != null) {
                    headerImage.setImageBitmap(bitmap);
                }
            } else {
                headerImage.setImageResource(R.drawable.cityscape_placeholder);
            }

            break;
        case Configuration.ORIENTATION_LANDSCAPE:
            headerImage.setVisibility(View.GONE);
            break;
        }
    }

}
