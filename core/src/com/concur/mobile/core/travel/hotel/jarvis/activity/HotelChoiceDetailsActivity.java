package com.concur.mobile.core.travel.hotel.jarvis.activity;

import android.app.ActivityOptions;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Build.VERSION_CODES;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.concur.core.R;
import com.concur.mobile.core.util.EventTracker;
import com.concur.mobile.core.util.Flurry;
import com.concur.mobile.platform.travel.search.hotel.Hotel;
import com.concur.mobile.platform.travel.search.hotel.HotelRate;
import com.concur.mobile.platform.travel.search.hotel.HotelViolation;
import com.concur.mobile.platform.ui.travel.activity.TravelBaseActivity;
import com.concur.mobile.platform.ui.travel.hotel.fragment.HotelChoiceDetailsFragment;
import com.concur.mobile.platform.ui.travel.hotel.fragment.HotelChoiceDetailsFragment.HotelChoiceDetailsFragmentListener;
import com.concur.mobile.platform.ui.travel.hotel.fragment.HotelDetailsFragment;
import com.concur.mobile.platform.ui.travel.hotel.fragment.HotelImagesFragment;
import com.concur.mobile.platform.ui.travel.hotel.fragment.HotelMapFragment;
import com.concur.mobile.platform.ui.travel.hotel.fragment.HotelRoomDetailFragment;
import com.concur.mobile.platform.ui.travel.hotel.fragment.HotelRoomListItem;
import com.concur.mobile.platform.ui.travel.hotel.fragment.HotelSearchResultListItem;
import com.concur.mobile.platform.ui.travel.loader.TravelCustomFieldsConfig;
import com.concur.mobile.platform.ui.travel.util.Const;
import com.concur.mobile.platform.ui.travel.util.ParallaxScollView;
import com.concur.mobile.platform.ui.travel.util.ViewUtil;
import com.google.android.m4b.maps.CameraUpdateFactory;
import com.google.android.m4b.maps.GoogleMap;
import com.google.android.m4b.maps.OnMapReadyCallback;
import com.google.android.m4b.maps.model.LatLng;
import com.google.android.m4b.maps.model.MarkerOptions;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;


/**
 * @author tejoa
 */
@EventTracker.EventTrackerClassName(getClassName = Flurry.SCREEN_NAME_TRAVEL_HOTEL_OVERVIEW)
public class HotelChoiceDetailsActivity extends TravelBaseActivity
        implements HotelChoiceDetailsFragmentListener, HotelDetailsFragment.HotelDetailsFragmentListener, OnMapReadyCallback {

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
    private String customTravelText;
    private HotelMapFragment hotelMapFragment;

    // for GA tracking
    private boolean suggestedAvailable;

    @Override
    protected void onStart() {
        super.onStart();
        EventTracker.INSTANCE.activityStart(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        EventTracker.INSTANCE.activityStop(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.option_maps, menu);
        return true;
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);

        if (hasFocus && hotelDetailsFrag != null && hotelDetailsFrag.mListView != null) {
            hotelDetailsFrag.mListView.setViewsBounds(ParallaxScollView.ZOOM_X2);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.map) {
            if (hotel != null) {

                onMapsClicked(false);
            } else {
                Toast.makeText(getApplicationContext(), R.string.map_unavailable, Toast.LENGTH_SHORT).show();
            }
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }

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
        if (i.hasExtra("customTravelText")) {
            customTravelText = i.getStringExtra("customTravelText");
        }

        suggestedAvailable = i.getBooleanExtra(Const.EXTRA_TRAVEL_SUGGESTED_HOTEL_AVAILABLE, false);
    }

    @Override
    public void onImageClicked(View v, int id) {
        if (isOffline) {
            showOfflineDialog();
        } else {
            Log.d(Const.LOG_TAG, CLS_TAG + "*********************** EventTracker - " + Flurry.EVENT_CATEGORY_TRAVEL_HOTEL + " - " + Flurry.EVENT_ACTION_PHOTO_TAPPED);
            EventTracker.INSTANCE.eventTrack(Flurry.EVENT_CATEGORY_TRAVEL_HOTEL, Flurry.EVENT_ACTION_PHOTO_TAPPED);
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
        Log.d(Const.LOG_TAG, CLS_TAG + "*********************** EventTracker - " + Flurry.EVENT_CATEGORY_TRAVEL_HOTEL + " - " + Flurry.EVENT_ACTION_ROOM_BUTTON_SELECT);
        EventTracker.INSTANCE.eventTrack(Flurry.EVENT_CATEGORY_TRAVEL_HOTEL, Flurry.EVENT_ACTION_ROOM_BUTTON_SELECT);
        FragmentManager fm = hotelDetailsFrag.getFragmentManager();
        if (fm.findFragmentByTag(TAB_ROOMS) != null) {
            hotelDetailsFrag.mTabHost.setCurrentTab(1);
        }

    }

    @Override
    public void onMapsClicked(boolean fromDetailsFragmentMapView) { //LatLng post
        if (isOffline) {
            showOfflineDialog();

        } else {
            if (fromDetailsFragmentMapView) {
                Log.d(Const.LOG_TAG, CLS_TAG + "*********************** EventTracker - " + Flurry.EVENT_CATEGORY_TRAVEL_HOTEL + " - " + Flurry.EVENT_ACTION_MAP_DETAILS_SINGLE_HOTEL);
                EventTracker.INSTANCE.eventTrack(Flurry.EVENT_CATEGORY_TRAVEL_HOTEL, Flurry.EVENT_ACTION_MAP_DETAILS_SINGLE_HOTEL);
            } else {
                Log.d(Const.LOG_TAG, CLS_TAG + "*********************** EventTracker - " + Flurry.EVENT_CATEGORY_TRAVEL_HOTEL + " - " + Flurry.EVENT_ACTION_MAP_NAVBAR_SINGLE_HOTEL);
                EventTracker.INSTANCE.eventTrack(Flurry.EVENT_CATEGORY_TRAVEL_HOTEL, Flurry.EVENT_ACTION_MAP_NAVBAR_SINGLE_HOTEL);
            }
            //  int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);

            // TODO customize ShowMaps to view single and multiple hotels
            // Intent i = new Intent(this, ShowHotelMap.class);
            //  if (resultCode == ConnectionResult.SUCCESS && hotel != null) {
            // i.putExtra(Const.EXTRA_HOTEL_LOCATION, post);
            // startActivity(i);
            // Intent i = this.getIntent();
            // i.putExtra(Const.EXTRA_HOTEL_LOCATION, post);
            hotelMapFragment = (HotelMapFragment) getFragmentManager().findFragmentByTag(FRAGMENT_HOTEL_MAP);
            //  LatLng post = new LatLng(hotel.latitude, hotel.longitude);
            if (hotelMapFragment == null) {
                hotelMapFragment = new HotelMapFragment();

            }
            FragmentTransaction ft = getFragmentManager().beginTransaction();
            if (!hotelMapFragment.isVisible()) {
                Bundle args = new Bundle();
                //  args.putSerializable(Const.EXTRA_HOTELS_LIST, (Serializable) hotelList);
                args.putString(Const.EXTRA_TRAVEL_LATITUDE, hotel.latitude.toString());
                args.putString(Const.EXTRA_TRAVEL_LONGITUDE, hotel.longitude.toString());
                args.putBoolean(Const.EXTRA_TRAVEL_SEARCH_NEAR_ME, hotel.showNearMe);
                hotelMapFragment.setArguments(args);

                ft.hide(hotelDetailsFrag);

                // if (mapFragment != null && !mapFragment.isAdded()) {
                ft.add(R.id.tabcontainer, hotelMapFragment, FRAGMENT_HOTEL_MAP);
                ft.addToBackStack(null);
                ft.commit();
            } else {
                ft.hide(hotelMapFragment);
                getFragmentManager().popBackStackImmediate();
            }

            //            } else {
            //                Toast.makeText(this, R.string.map_unavailable, Toast.LENGTH_LONG).show();
            //            }
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
                detailFrag.priceToBeat = hotel.priceToBeat;
                fm.beginTransaction().replace(placeholder, detailFrag, tabId).commit();
            }
            if (TAB_IMAGES.equals(tabId)) {
                fm.beginTransaction().replace(placeholder, new HotelImagesFragment(hotel.imagePairs), tabId).commit();
            }
        }

        // log GA events
        if (TAB_DETAILS.equals(tabId)) {
            Log.d(Const.LOG_TAG, CLS_TAG + "*********************** EventTracker - " + Flurry.EVENT_CATEGORY_TRAVEL_HOTEL + " - " + Flurry.EVENT_ACTION_HOTEL_DETAILS_VIEWED);
            EventTracker.INSTANCE.eventTrack(Flurry.EVENT_CATEGORY_TRAVEL_HOTEL, Flurry.EVENT_ACTION_HOTEL_DETAILS_VIEWED);
        } else if (TAB_ROOMS.equals(tabId)) {
            Log.d(Const.LOG_TAG, CLS_TAG + "*********************** EventTracker - " + Flurry.EVENT_CATEGORY_TRAVEL_HOTEL + " - " + Flurry.EVENT_ACTION_HOTEL_ROOMS_VIEWED);
            EventTracker.INSTANCE.eventTrack(Flurry.EVENT_CATEGORY_TRAVEL_HOTEL, Flurry.EVENT_ACTION_HOTEL_ROOMS_VIEWED);
        } else if (TAB_IMAGES.equals(tabId)) {
            Log.d(Const.LOG_TAG, CLS_TAG + "*********************** EventTracker - " + Flurry.EVENT_CATEGORY_TRAVEL_HOTEL + " - " + Flurry.EVENT_ACTION_HOTEL_PHOTOS_VIEWED);
            EventTracker.INSTANCE.eventTrack(Flurry.EVENT_CATEGORY_TRAVEL_HOTEL, Flurry.EVENT_ACTION_HOTEL_PHOTOS_VIEWED);
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
                Log.d(Const.LOG_TAG, CLS_TAG + "*********************** EventTracker - " + Flurry.EVENT_CATEGORY_TRAVEL_HOTEL + " - " + Flurry.EVENT_ACTION_ROOM_SELECT);
                EventTracker.INSTANCE.eventTrack(Flurry.EVENT_CATEGORY_TRAVEL_HOTEL, Flurry.EVENT_ACTION_ROOM_SELECT);

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
                if (customTravelText != null) {
                    intent.putExtra("customTravelText", customTravelText);
                }
                intent.putExtra(Const.EXTRA_TRAVEL_SUGGESTED_HOTEL_AVAILABLE, suggestedAvailable);
                if (hotelListItem.getHotel().recommended != null) {
                    intent.putExtra(Const.EXTRA_TRAVEL_SUGGESTED_HOTEL, hotelListItem.getHotel().recommended.getSuggestedCategory() == null ? false : true);
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
                    Log.d(com.concur.mobile.platform.util.Const.LOG_TAG,
                            "\n\n\n ****** HotelChoiceDetailsActivity onActivityResult with result code : " + resultCode);
                    finish();
                }
                break;
            }
        }

        super.onActivityResult(requestCode, resultCode, data);

    } // onActivityResult()

    public void callHotel(String phoneNumberCleaned) {
        Log.d(Const.LOG_TAG, CLS_TAG + "*********************** EventTracker - " + Flurry.EVENT_CATEGORY_TRAVEL_HOTEL + " - " + Flurry.EVENT_ACTION_CALL_HOTEL_TAPPED);
        EventTracker.INSTANCE.eventTrack(Flurry.EVENT_CATEGORY_TRAVEL_HOTEL, Flurry.EVENT_ACTION_CALL_HOTEL_TAPPED);

        // start the phone dialer
        Intent callIntent = new Intent(Intent.ACTION_DIAL);
        callIntent.setData(Uri.parse("tel:" + phoneNumberCleaned));

        startActivity(callIntent);
    }
}
