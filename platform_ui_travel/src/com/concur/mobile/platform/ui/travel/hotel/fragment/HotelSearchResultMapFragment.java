package com.concur.mobile.platform.ui.travel.hotel.fragment;

import android.app.Activity;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.concur.mobile.platform.ui.common.fragment.PlatformFragmentV1;
import com.concur.mobile.platform.ui.travel.R;
import com.concur.mobile.platform.ui.travel.util.Const;
import com.google.android.gms.maps.*;
import com.google.android.gms.maps.model.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by tejoa on 11/05/2015.
 */
public class HotelSearchResultMapFragment extends PlatformFragmentV1 implements OnMapReadyCallback {

    private GoogleMap googleMap;
    private MapFragment mapFragment;
    private double latitude;
    private double longitude;
    private boolean searchNearMe;
    private LatLngBounds.Builder builder;
    private View hotelInfoView;
    private Marker previousMarker;
    private HotelSearchMapsFragmentListener callBackListener;
    private HotelSearchResultListItem itemClicked;
    private boolean progressbarVisible;
    private View mainView;
    private View progressBar;
    private View progressBarMsg;
    /*
     * Hashmap for all hotel markers and hotel list items
     */
    private Map<Marker, HotelSearchResultListItem> markerMap = new HashMap<Marker, HotelSearchResultListItem>();
    private List<HotelSearchResultListItem> hotels;

    public Bundle args;

    // empty constructor
    public HotelSearchResultMapFragment() {
        setRetainInstance(true);
    }

    @Override public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        args = getArguments();
        // inflate the details fragment
        mainView = inflater.inflate(R.layout.map_layout, container, false);
        //        Intent intent = getActivity().getIntent();
        //        latitude = Double.valueOf(intent.getStringExtra(Const.EXTRA_TRAVEL_LATITUDE));
        //        longitude = Double.valueOf(intent.getStringExtra(Const.EXTRA_TRAVEL_LONGITUDE));
        //        searchNearMe = intent.getBooleanExtra(Const.EXTRA_TRAVEL_SEARCH_NEAR_ME, false);
        setUpMap();
        if (googleMap != null) {
            addMarkers();
        }
        hotelInfoView = (View) mainView.findViewById(R.id.info_window);
        progressBar = mainView.findViewById(R.id.hotel_map_progress);
        progressBarMsg = mainView.findViewById(R.id.hotel_map_progress_msg);
        hotelInfoView.setOnClickListener(new View.OnClickListener() {

            @Override public void onClick(View v) {
                //  Handler mHandler = new Handler();
                // ProgressBar pb = new ProgressBar(getActivity(), null, android.R.attr.progressBarStyleHorizontal);
                //                new Thread(new Runnable() {
                //
                //                    public void run() {
                //                        showProgressBar();
                //                    }
                //                }).start();
                //   showProgressBar();
                callBackListener.mapsHotelListItemClicked(itemClicked);
            }
        });
        return mainView;
    }

    @Override public void onAttach(Activity activity) {
        super.onAttach(activity);

        // This makes sure that the container activity has implemented
        // the callback interface. If not, it throws an exception
        try {
            callBackListener = (HotelSearchMapsFragmentListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement HotelSearchMapsFragmentListener");
        }
    }

    @Override public void onSaveInstanceState(Bundle outState) {
        // TODO Auto-generated method stub
        super.onSaveInstanceState(outState);

        Log.d(Const.LOG_TAG, " ***** HotelSearchResultMapFragment, in onSaveInstanceState *****  ");
    }

    @Override public void onPause() {
        // TODO Auto-generated method stub
        super.onPause();

        Log.d(Const.LOG_TAG, " ***** HotelSearchResultMapFragment, in onPause *****  ");

    }

    @Override public void onResume() {
        // TODO Auto-generated method stub
        super.onResume();

    }

    @Override public void onDestroyView() {
        super.onDestroyView();
        if (mapFragment != null) {
            FragmentTransaction ft = getActivity().getFragmentManager().beginTransaction();
            ft.remove(mapFragment);
            ft.commit();
        }

        googleMap = null;
    }

    /**
     * Google map setUp from MapFragment
     */
    private void setUpMap() {
        if (googleMap == null) {
            mapFragment = null;
            FragmentManager fm = null;
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
                fm = getFragmentManager();
            } else {
                fm = getChildFragmentManager();
            }
            mapFragment = (MapFragment) fm.findFragmentById(R.id.map);
            mapFragment.getMapAsync(this);
        }
    }

    /**
     * add all hotel markers with bounds to set zoom level
     */
    private void addMarkers() {
        hotels = new ArrayList<HotelSearchResultListItem>();

        hotels = ((ArrayList<HotelSearchResultListItem>) args.getSerializable(Const.EXTRA_HOTELS_LIST));
        if (hotels != null && hotels.size() > 0) {
            addHotelMarkers(hotels);
            // Hotel firstHotel = hotels.get(0).getHotel();
        }
        latitude = Double.valueOf(args.getString(Const.EXTRA_TRAVEL_LATITUDE));
        longitude = Double.valueOf(args.getString(Const.EXTRA_TRAVEL_LONGITUDE));
        searchNearMe = args.getBoolean(Const.EXTRA_TRAVEL_SEARCH_NEAR_ME, false);
        // TODO load custom icons
        //  LatLng position1 = new LatLng(firstHotel.latitude, firstHotel.longitude);
        LatLng position = new LatLng(latitude, longitude);
        if (searchNearMe) {
            googleMap.setMyLocationEnabled(true);
            builder.include(position);
        }

        LatLngBounds bounds = builder.build();
        int padding = 10; // offset from edges of the map in pixels
        CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds, 400, 400, padding);
        googleMap.moveCamera(cu);
        googleMap.animateCamera(cu);
        googleMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {

            @Override public boolean onMarkerClick(Marker marker) {
                itemClicked = markerMap.get(marker);
                marker.showInfoWindow();
                View view = itemClicked.buildView(getActivity(), hotelInfoView, null);
                view.setAlpha(1);
                if (previousMarker != null) {
                    previousMarker.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.map_pin_blue));
                }
                marker.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.map_pin_red));
                previousMarker = marker;
                return true;
            }
        });
        //        hideProgressBar();
    }

    private void addHotelMarkers(List<HotelSearchResultListItem> hotels) {
        builder = new LatLngBounds.Builder();
        boolean firstMarker = true;
        for (HotelSearchResultListItem item : hotels) {
            LatLng position = new LatLng(item.getHotel().latitude, item.getHotel().longitude);
            MarkerOptions marker = new MarkerOptions().position(position);
            marker.icon(BitmapDescriptorFactory.fromResource(R.drawable.map_pin_blue));

            Marker hotelMarker = googleMap.addMarker(marker);
            builder.include(position);
            markerMap.put(hotelMarker, item);
            if (firstMarker) {
                googleMap.setInfoWindowAdapter(new HotelInfoWindowAdapter());
                hotelMarker.showInfoWindow();
                item.buildView(getActivity(), hotelInfoView, null);
                hotelMarker.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.map_pin_red));
                firstMarker = false;
                previousMarker = hotelMarker;
                itemClicked = item;

            }
        }
        //        hideProgressBar();
    }

    @Override public void onMapReady(GoogleMap map) {
        googleMap = map;
        addMarkers();
        // hideProgressBar();

    }

    private class HotelInfoWindowAdapter implements GoogleMap.InfoWindowAdapter {

        @Override public View getInfoWindow(Marker marker) {

            return null;
        }

        @Override public View getInfoContents(Marker marker) {

            LayoutInflater inflater = null;

            inflater = LayoutInflater.from(getActivity());

            View v = inflater.inflate(R.layout.empty_info_window, null);
            return v;
        }

    }

    // Container Activity must implement this call back interface
    public interface HotelSearchMapsFragmentListener {

        public void mapsHotelListItemClicked(HotelSearchResultListItem itemClicked);
    }

    //    public void showProgressBar() {
    //        if (!progressbarVisible) {
    //            //  View progressBar = mainView.findViewById(R.id.hotel_map_progress);
    //            progressbarVisible = true;
    //            progressBar.setVisibility(View.VISIBLE);
    //            progressBar.bringToFront();
    //            //  View progressBarMsg = mainView.findViewById(R.id.hotel_map_progress_msg);
    //            progressBarMsg.setVisibility(View.VISIBLE);
    //            progressBarMsg.bringToFront();
    //            mainView.invalidate();
    //        }
    //    }

    //    public void hideProgressBar() {
    //        if (progressbarVisible) {
    //            //View progressBar = mainView.findViewById(R.id.hotel_map_progress);
    //            progressbarVisible = false;
    //            progressBar.setVisibility(View.GONE);
    //            //  View progressBarMsg = mainView.findViewById(R.id.hotel_map_progress_msg);
    //            progressBarMsg.setVisibility(View.GONE);
    //            mainView.requestLayout();
    //        }
    //    }

}
