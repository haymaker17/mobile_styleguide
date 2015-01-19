package com.concur.mobile.platform.ui.travel.hotel.fragment;

import android.app.ActionBar;
import android.app.Activity;
import android.app.FragmentManager;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TabHost;
import android.widget.TabHost.OnTabChangeListener;
import android.widget.TabHost.TabSpec;
import android.widget.TextView;

import com.concur.mobile.platform.travel.search.hotel.Hotel;
import com.concur.mobile.platform.ui.common.fragment.PlatformFragmentV1;
import com.concur.mobile.platform.ui.travel.R;
import com.concur.mobile.platform.ui.travel.util.Const;

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

    private View mRoot;
    private TabHost mTabHost;
    private int mCurrentTab;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        // inflate the details fragment
        mRoot = inflater.inflate(R.layout.hotel_choice_list_item, null, false);
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
        setupTabs();

        return mRoot;
    }

    private void setActionBar() {
        ActionBar actionBar = getActivity().getActionBar();
        actionBar.setTitle(hotelListItem.getHotel().name);

    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setRetainInstance(true);

        mTabHost.setOnTabChangedListener(this);
        mTabHost.setCurrentTab(mCurrentTab);
        // manually start loading stuff in the first tab
        updateTab(TAB_DETAILS, R.id.tab_details);
    }

    private void setupTabs() {
        mTabHost = (TabHost) mRoot.findViewById(android.R.id.tabhost);
        mTabHost.setup(); // you must call this before adding your tabs!

        mTabHost.addTab(newTab(TAB_DETAILS, R.string.hotel_tab_details, R.id.tab_details));
        mTabHost.addTab(newTab(TAB_ROOMS, R.string.hotel_tab_rooms, R.id.tab_rooms));
        mTabHost.addTab(newTab(TAB_IMAGES, R.string.hotel_tab_images, R.id.tab_images));
    }

    private TabSpec newTab(String tag, int labelId, int tabContentId) {
        Log.d(CLS_TAG, "buildTab(): tag=" + tag);

        View indicator = LayoutInflater.from(getActivity()).inflate(R.layout.tab,
                (ViewGroup) mRoot.findViewById(android.R.id.tabs), false);
        ((TextView) indicator.findViewById(R.id.text)).setText(labelId);

        TabSpec tabSpec = mTabHost.newTabSpec(tag);
        tabSpec.setIndicator(indicator);
        tabSpec.setContent(tabContentId);
        return tabSpec;
    }

    @Override
    public void onTabChanged(String tabId) {
        Log.d(CLS_TAG, "onTabChanged(): tabId=" + tabId);
        if (TAB_DETAILS.equals(tabId)) {
            updateTab(tabId, R.id.tab_details);
            mCurrentTab = 0;
            return;
        }
        if (TAB_ROOMS.equals(tabId)) {
            updateTab(tabId, R.id.tab_rooms);
            mCurrentTab = 1;
            return;
        }
        if (TAB_IMAGES.equals(tabId)) {
            updateTab(tabId, R.id.tab_images);
            mCurrentTab = 2;
            return;
        }
    }

    private void updateTab(String tabId, int placeholder) {
        FragmentManager fm = getFragmentManager();
        if (fm.findFragmentByTag(tabId) == null) {
            Hotel hotel = hotelListItem.getHotel();
            if (TAB_DETAILS.equals(tabId)) {
                fm.beginTransaction().replace(placeholder, new HotelDetailsFragment(hotel), tabId).commit();
            }
            if (TAB_ROOMS.equals(tabId)) {
                fm.beginTransaction().replace(placeholder, new HotelRoomDetailFragment(hotel.rates), tabId).commit();
            }
            if (TAB_IMAGES.equals(tabId)) {
                fm.beginTransaction().replace(placeholder, new ShowImagesFragment(hotel.imagePairs), tabId).commit();
            }
        }
    }
}