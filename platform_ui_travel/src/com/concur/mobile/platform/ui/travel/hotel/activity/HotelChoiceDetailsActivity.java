package com.concur.mobile.platform.ui.travel.hotel.activity;

import android.app.ActivityOptions;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.os.Build;
import android.os.Build.VERSION_CODES;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;
import com.concur.mobile.platform.travel.search.hotel.Hotel;
import com.concur.mobile.platform.travel.search.hotel.HotelRate;
import com.concur.mobile.platform.travel.search.hotel.HotelViolation;
import com.concur.mobile.platform.ui.travel.R;
import com.concur.mobile.platform.ui.travel.activity.TravelBaseActivity;
import com.concur.mobile.platform.ui.travel.hotel.fragment.*;
import com.concur.mobile.platform.ui.travel.hotel.fragment.HotelChoiceDetailsFragment.HotelChoiceDetailsFragmentListener;
import com.concur.mobile.platform.ui.travel.loader.TravelCustomFieldsConfig;
import com.concur.mobile.platform.ui.travel.util.Const;
import com.concur.mobile.platform.ui.travel.util.ParallaxScollView;
import com.concur.mobile.platform.ui.travel.util.ViewUtil;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * @author tejoa
 */
public class HotelChoiceDetailsActivity extends TravelBaseActivity
        implements HotelChoiceDetailsFragmentListener, OnMapReadyCallback {

    public static final String CLS_TAG = HotelChoiceDetailsActivity.class.getSimpleName();
    public static final String FRAGMENT_HOTEL_DETAILS = "FRAGMENT_HOTEL_DETAILS";
    public static final String FRAGMENT_HOTEL_MAP = "FRAGMENT_HOTEL_MAP";
    public static final String TAB_DETAILS = "DETAILS";
    public static final String TAB_ROOMS = "ROOMS";
    public static final String TAB_IMAGES = "IMAGES";
    public static final String DIALOG_DEPOSITE_REQUIRED = "DIALOG_DEPOSITE_REQUIRED";

    private HotelChoiceDetailsFragment hotelDetailsFrag;
    private HotelSearchResultListItem hotelListItem;
    private Hotel hotel;
    private String location;
    private String durationOfStayForDisplay;
    private int numOfNights;
    private String headerImageURL;
    private ArrayList<String[]> violationReasons;
    private boolean ruleViolationExplanationRequired;
    private boolean showGDSName;
    private String currentTripId;
    private List<HotelViolation> violations;

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
            // Toast.makeText(getApplicationContext(), "Not implemented", Toast.LENGTH_SHORT).show();
            //            if (hotel != null) {
            //                LatLng post = new LatLng(hotel.latitude, hotel.longitude);
            //                onMapsClicked(post);
            //            } else {
            Toast.makeText(getApplicationContext(), "Maps unavailable", Toast.LENGTH_SHORT).show();
            // }
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
        hotel = hotelListItem.getHotel();

        hotelDetailsFrag = (HotelChoiceDetailsFragment) getFragmentManager().findFragmentByTag(FRAGMENT_HOTEL_DETAILS);
        if (hotelDetailsFrag == null) {
            hotelDetailsFrag = new HotelChoiceDetailsFragment();
            FragmentTransaction ft = getFragmentManager().beginTransaction();
            ft.add(R.id.tabcontainer, hotelDetailsFrag, FRAGMENT_HOTEL_DETAILS);
            ft.commit();
        }

        // will be passed on to booking activity
        location = i.getStringExtra(Const.EXTRA_TRAVEL_HOTEL_SEARCH_LOCATION);
        durationOfStayForDisplay = i.getStringExtra(Const.EXTRA_TRAVEL_HOTEL_SEARCH_DURATION_OF_STAY);
        numOfNights = i.getIntExtra(Const.EXTRA_TRAVEL_HOTEL_SEARCH_DURATION_NUM_OF_NIGHTS, 0);
        violationReasons = (ArrayList<String[]>) i.getSerializableExtra("violationReasons");
        ruleViolationExplanationRequired = i.getBooleanExtra("ruleViolationExplanationRequired", false);
        showGDSName = i.getBooleanExtra(Const.EXTRA_TRAVEL_HOTEL_SEARCH_SHOW_GDS_NAME, false);
        currentTripId = i.getStringExtra("currentTripId");

        List<HotelViolation> updatedViolations = ((List<HotelViolation>) bundle.getSerializable("updatedViolations"));

        if (updatedViolations != null && updatedViolations.size() > 0) {
            violations = updatedViolations;
        } else {
            violations = ((List<HotelViolation>) bundle.getSerializable("violations"));

            // TravelUtilHotel.getHotelViolations(getApplicationContext(), null, Integer.valueOf(searchId));
        }
        for (HotelRate rate : hotel.rates) {
            HotelViolation maxEnforcementViolation = ViewUtil
                    .getShowButNoBookingViolation(violations, rate.maxEnforceLevel, rate.maxEnforcementLevel);
            if (maxEnforcementViolation != null) {
                rate.greyFlag = true;
            }

        }

        if (i.hasExtra("travelCustomFieldsConfig")) {
            travelCustomFieldsConfig = (TravelCustomFieldsConfig) i.getSerializableExtra("travelCustomFieldsConfig");
        }

    }

    @Override
    public void onImageClicked(View v, int id) {
        if (isOffline) {
            showOfflineDialog();
        } else {
            final Intent i = new Intent(this, ImageDetailActivity.class);
            i.putExtra(ImageDetailActivity.EXTRA_IMAGE, id);
            Bundle bundle = new Bundle();
            bundle.putSerializable(Const.EXTRA_HOTEL_IMAGES, (Serializable) hotel.imagePairs);
            i.putExtras(bundle);
            if (Build.VERSION.SDK_INT >= VERSION_CODES.JELLY_BEAN) {
                // makeThumbnailScaleUpAnimation() looks kind of ugly here as the loading spinner may
                // show plus the thumbnail image in GridView is cropped. so using
                // makeScaleUpAnimation() instead.
                ActivityOptions options = ActivityOptions.makeScaleUpAnimation(v, 0, 0, v.getWidth(), v.getHeight());
                startActivity(i, options.toBundle());
            } else {
                startActivity(i);
            }
        }
    }

    @Override
    public void onFindRoomsClicked() {
        if (isOffline) {
            showOfflineDialog();
        } else {
            FragmentManager fm = hotelDetailsFrag.getFragmentManager();
            if (fm.findFragmentByTag(TAB_ROOMS) != null) {
                hotelDetailsFrag.mTabHost.setCurrentTab(1);
            }
        }

    }

    @Override
    public void onMapsClicked(LatLng post) {
        if (isOffline) {
            showOfflineDialog();

        } else {
            int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);

            // TODO customize ShowMaps to view single and multiple hotels
            // Intent i = new Intent(this, ShowHotelMap.class);
            if (resultCode == ConnectionResult.SUCCESS && post != null) {
                // i.putExtra(Const.EXTRA_HOTEL_LOCATION, post);
                // startActivity(i);
                // Intent i = this.getIntent();
                // i.putExtra(Const.EXTRA_HOTEL_LOCATION, post);
                HotelMapFragment hotelMapFragment = (HotelMapFragment) getFragmentManager()
                        .findFragmentByTag(FRAGMENT_HOTEL_MAP);
                if (hotelMapFragment == null) {
                    hotelMapFragment = new HotelMapFragment(post);
                    FragmentTransaction ft = getFragmentManager().beginTransaction();
                    ft.add(R.id.tabcontainer, hotelMapFragment, FRAGMENT_HOTEL_MAP);
                    ft.addToBackStack(null);
                    ft.commit();
                }

                // hotel = new HotelSearchResultListItem();
                // hotelListItem = (HotelSearchResultListItem) bundle.getSerializable(Const.EXTRA_HOTELS_DETAILS);

            } else {
                Toast.makeText(this, "Map Unavailable", Toast.LENGTH_LONG).show();
            }
        }

    }

    @Override
    public void updateTab(String tabId, int placeholder) {
        FragmentManager fm = hotelDetailsFrag.getFragmentManager();
        if (fm.findFragmentByTag(tabId) == null) {
            if (TAB_DETAILS.equals(tabId)) {
                fm.beginTransaction().replace(placeholder, new HotelDetailsFragment(hotel), tabId).commit();
            }
            if (TAB_ROOMS.equals(tabId)) {
                HotelRoomDetailFragment detailFrag = new HotelRoomDetailFragment(hotel.rates, showGDSName);
                //detailFrag.priceToBeat = hotel.priceToBeat;
                fm.beginTransaction().replace(placeholder, detailFrag, tabId).commit();
            }
            if (TAB_IMAGES.equals(tabId)) {
                fm.beginTransaction().replace(placeholder, new HotelImagesFragment(hotel.imagePairs), tabId).commit();
            }
        }
    }

    @Override
    public void roomItemClicked(HotelRoomListItem roomListItem) {

        if (isOffline) {
            showOfflineDialog();
        } else {

            if (roomListItem.getHotelRoom().greyFlag) {
                Toast.makeText(getApplicationContext(), R.string.hotel_dialog_deposit_required_msg, Toast.LENGTH_LONG)
                        .show();
            } else {
                Intent intent = new Intent(this, HotelBookingActivity.class);
                // TODO - have a singleton class and set these objects there to share with other activities?
                intent.putExtra("roomSelected", roomListItem.getHotelRoom());

                intent.putExtra(Const.EXTRA_TRAVEL_HOTEL_SEARCH_LOCATION, location);
                intent.putExtra(Const.EXTRA_TRAVEL_HOTEL_SEARCH_DURATION_OF_STAY, durationOfStayForDisplay);
                intent.putExtra(Const.EXTRA_TRAVEL_HOTEL_SEARCH_DURATION_NUM_OF_NIGHTS, numOfNights);
                intent.putExtra("headerImageURL", headerImageURL);
                intent.putExtra("hotelName", hotelListItem.getHotel().name);
                // startActivity(intent);
                intent.putExtra("violationReasons", violationReasons);
                intent.putExtra("ruleViolationExplanationRequired", ruleViolationExplanationRequired);
                intent.putExtra("currentTripId", currentTripId);
                intent.putExtra("hotelSearchId", hotel.search_id);
                intent.putExtra("violations", (Serializable) violations);
                if (travelCustomFieldsConfig != null) {
                    intent.putExtra("travelCustomFieldsConfig", travelCustomFieldsConfig);
                }
                startActivityForResult(intent, Const.REQUEST_CODE_BOOK_HOTEL);
            }

        }

    }

    @Override
    public void setHeaderImageURL(String headerImageURL) {
        this.headerImageURL = headerImageURL;
    }

    @Override
    public void onMapReady(GoogleMap map) {
        map.getUiSettings().setZoomControlsEnabled(false);
        // map.
        // map.setOnMapClickListener(//)
        LatLng position = new LatLng(hotel.latitude, hotel.longitude);
        MarkerOptions marker = new MarkerOptions().position(position);
        map.addMarker(marker);
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(position, 15));
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        switch (requestCode) {
        case Const.REQUEST_CODE_BOOK_HOTEL: {
            if (resultCode == RESULT_OK) {
                setResult(RESULT_OK, data);
                Log.i(com.concur.mobile.platform.util.Const.LOG_TAG,
                        "\n\n\n ****** HotelChoiceDetailsActivity onActivityResult with result code : " + resultCode);
                finish();
            }
            break;
        }
        }

        super.onActivityResult(requestCode, resultCode, data);

    } // onActivityResult()
}
