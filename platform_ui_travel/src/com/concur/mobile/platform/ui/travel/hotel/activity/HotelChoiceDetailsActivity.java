package com.concur.mobile.platform.ui.travel.hotel.activity;

import java.io.Serializable;
import java.util.ArrayList;

import android.app.Activity;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.TabHost;
import android.widget.Toast;

import com.concur.mobile.platform.travel.search.hotel.Hotel;
import com.concur.mobile.platform.travel.search.hotel.HotelImagePair;
import com.concur.mobile.platform.ui.travel.R;
import com.concur.mobile.platform.ui.travel.hotel.fragment.HotelChoiceDetailsFragment;
import com.concur.mobile.platform.ui.travel.hotel.fragment.HotelChoiceDetailsFragment.HotelChoiceDetailsFragmentListener;
import com.concur.mobile.platform.ui.travel.hotel.fragment.HotelDetailsFragment;
import com.concur.mobile.platform.ui.travel.hotel.fragment.HotelMapFragment;
import com.concur.mobile.platform.ui.travel.hotel.fragment.HotelRoomDetailFragment;
import com.concur.mobile.platform.ui.travel.hotel.fragment.HotelRoomListItem;
import com.concur.mobile.platform.ui.travel.hotel.fragment.HotelSearchResultListItem;
import com.concur.mobile.platform.ui.travel.hotel.fragment.ImageListItem;
import com.concur.mobile.platform.ui.travel.hotel.fragment.ShowImagesFragment;
import com.concur.mobile.platform.ui.travel.util.Const;
import com.concur.mobile.platform.ui.travel.util.ParallaxScollView;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.maps.model.LatLng;

/**
 * 
 * @author tejoa
 * 
 */
public class HotelChoiceDetailsActivity extends Activity implements HotelChoiceDetailsFragmentListener {

    public static final String CLS_TAG = HotelChoiceDetailsActivity.class.getSimpleName();
    public static final String FRAGMENT_HOTEL_DETAILS = "FRAGMENT_HOTEL_DETAILS";
    public static final String FRAGMENT_HOTEL_MAP = "FRAGMENT_HOTEL_MAP";
    public static final String TAB_DETAILS = "DETAILS";
    public static final String TAB_ROOMS = "ROOMS";
    public static final String TAB_IMAGES = "IMAGES";

    private HotelChoiceDetailsFragment hotelDetailsFrag;
    private View mRoot;
    private TabHost mTabHost;
    private int mCurrentTab;
    private HotelSearchResultListItem hotelListItem;
    private String location;
    private String durationOfStayForDisplay;
    private int numOfNights;
    private String headerImageURL;
    private ArrayList<String[]> violationReasons;

    // private ParallaxScollListView mListView;
    // private ImageView mImageView;
    // private HotelSearchResultListItem hotel;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.option_maps, menu);
        return true;
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);

        if (hasFocus) {
            hotelDetailsFrag.mListView.setViewsBounds(ParallaxScollView.ZOOM_X2);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.map) {
            Toast.makeText(getApplicationContext(), "Not implemented", Toast.LENGTH_SHORT).show();
            // onMapsClicked();
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }

    // private void onMapsClicked() {
    // // TODO Auto-generated method stub
    //
    // }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.hotel_choice_item);
        Intent i = this.getIntent();
        final Bundle bundle = i.getExtras();
        // hotel = new HotelSearchResultListItem();
        hotelListItem = (HotelSearchResultListItem) bundle.getSerializable(Const.EXTRA_HOTELS_DETAILS);

        hotelDetailsFrag = (HotelChoiceDetailsFragment) getFragmentManager().findFragmentByTag(FRAGMENT_HOTEL_DETAILS);
        if (hotelDetailsFrag == null) {
            hotelDetailsFrag = new HotelChoiceDetailsFragment();
            FragmentTransaction ft = getFragmentManager().beginTransaction();
            ft.add(R.id.tabcontainer, hotelDetailsFrag, FRAGMENT_HOTEL_DETAILS);
            ft.commit();
        }

        // mListView = (ParallaxScollListView) findViewById(R.id.layout_listview);
        // View header = LayoutInflater.from(this).inflate(R.layout.hotel_choice_header, null);
        // mImageView = (ImageView) header.findViewById(R.id.travelCityscape);
        //
        // View hotelView = header.findViewById(R.id.hotel_row);
        // hotelListItem.getHotel().lowestRate = null;
        //
        // ((HotelSearchResultListItem) hotelListItem).buildView(this, hotelView, null);
        // showHideHomeImage();
        //
        // mListView.setParallaxImageView(mImageView);
        // mListView.addHeaderView(header);
        //
        // ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_expandable_list_item_1,
        // new String[] {});
        // mListView.setAdapter(adapter);

        // will be passed on to booking activity
        location = i.getStringExtra(Const.EXTRA_TRAVEL_HOTEL_SEARCH_LOCATION);
        durationOfStayForDisplay = i.getStringExtra(Const.EXTRA_TRAVEL_HOTEL_SEARCH_DURATION_OF_STAY);
        numOfNights = i.getIntExtra(Const.EXTRA_TRAVEL_HOTEL_SEARCH_DURATION_NUM_OF_NIGHTS, 0);
        violationReasons = (ArrayList<String[]>) i.getSerializableExtra("violationReasons");

    }

    @Override
    public void onImageClicked(ImageListItem imageListItem) {
        if (imageListItem != null) {
            HotelImagePair hotelImage = imageListItem.getHotelImage();
            if (hotelImage != null) {
                Intent i = new Intent(this, ImageActivity.class);
                Bundle bundle = new Bundle();
                bundle.putSerializable(Const.EXTRA_HOTEL_IMAGES, (Serializable) hotelListItem.getHotel().imagePairs);
                i.putExtras(bundle);

                startActivity(i);
            }

        }
    }

    @Override
    public void onFindRoomsClicked() {
        FragmentManager fm = hotelDetailsFrag.getFragmentManager();
        if (fm.findFragmentByTag(TAB_ROOMS) != null) {
            hotelDetailsFrag.mTabHost.setCurrentTab(1);
        }

    }

    @Override
    public void onMapsClicked(LatLng post) {
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);

        // TODO customize ShowMaps to view single and multiple hotels
        // Intent i = new Intent(this, ShowHotelMap.class);
        if (resultCode == ConnectionResult.SUCCESS && post != null) {
            // i.putExtra(Const.EXTRA_HOTEL_LOCATION, post);
            // startActivity(i);
            // Intent i = this.getIntent();
            // i.putExtra(Const.EXTRA_HOTEL_LOCATION, post);
            HotelMapFragment hotelMapFragment = (HotelMapFragment) getFragmentManager().findFragmentByTag(
                    FRAGMENT_HOTEL_MAP);
            if (hotelMapFragment == null) {
                hotelMapFragment = new HotelMapFragment(post);
                FragmentTransaction ft = getFragmentManager().beginTransaction();
                ft.add(R.id.container, hotelMapFragment, FRAGMENT_HOTEL_MAP);
                ft.commit();
            }

            // hotel = new HotelSearchResultListItem();
            // hotelListItem = (HotelSearchResultListItem) bundle.getSerializable(Const.EXTRA_HOTELS_DETAILS);

        } else {
            Toast.makeText(this, "Map Unavailable", Toast.LENGTH_LONG).show();
        }

    }

    @Override
    public void updateTab(String tabId, int placeholder) {
        FragmentManager fm = hotelDetailsFrag.getFragmentManager();
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

    @Override
    public void roomItemClicked(HotelRoomListItem roomListItem) {
        Intent intent = new Intent(this, HotelBookingActivity.class);
        // TODO - have a singleton class and set these objects there to share with other activities?
        intent.putExtra("roomSelected", roomListItem.getHotelRoom());

        intent.putExtra(Const.EXTRA_TRAVEL_HOTEL_SEARCH_LOCATION, location);
        intent.putExtra(Const.EXTRA_TRAVEL_HOTEL_SEARCH_DURATION_OF_STAY, durationOfStayForDisplay);
        intent.putExtra(Const.EXTRA_TRAVEL_HOTEL_SEARCH_DURATION_NUM_OF_NIGHTS, numOfNights);
        intent.putExtra("headerImageURL", headerImageURL);
        intent.putExtra("hotelName", hotelListItem.getHotel().name);
        // intent.putExtra("maxEnforcementLevelString", roomListItem.maxEnforcementLevelString);
        intent.putExtra("violationReasons", violationReasons);
        startActivity(intent);

    }

    @Override
    public void setHeaderImageURL(String headerImageURL) {
        this.headerImageURL = headerImageURL;
    }

}
