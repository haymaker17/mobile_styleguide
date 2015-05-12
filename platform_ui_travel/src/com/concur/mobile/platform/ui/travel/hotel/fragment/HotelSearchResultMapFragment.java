package com.concur.mobile.platform.ui.travel.hotel.fragment;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Intent;
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

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        args = getArguments();
        // inflate the details fragment
        View mainView = inflater.inflate(R.layout.map_layout, container, false);
        Intent intent = getActivity().getIntent();
        latitude = Double.valueOf(intent.getStringExtra(Const.EXTRA_TRAVEL_LATITUDE));
        longitude = Double.valueOf(intent.getStringExtra(Const.EXTRA_TRAVEL_LONGITUDE));
        searchNearMe = intent.getBooleanExtra(Const.EXTRA_TRAVEL_SEARCH_NEAR_ME, false);
        setUpMap();
        if (googleMap != null) {
            addMarkers();
        }

        hotelInfoView = (View) mainView.findViewById(R.id.info_window);
        //        hotelInfoView.setOnClickListener(new View.OnClickListener() {
        //
        //            @Override public void onClick(View v) {
        //                callBackListener.hotelListItemClicked(itemClicked);
        //            }
        //        });

        return mainView;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        // This makes sure that the container activity has implemented
        // the callback interface. If not, it throws an exception
        try {
            callBackListener = (HotelSearchMapsFragmentListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement HotelSearchMapsFragmentListener");
        }

    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        // TODO Auto-generated method stub
        super.onSaveInstanceState(outState);

        Log.d(Const.LOG_TAG, " ***** HotelSearchResultMapFragment, in onSaveInstanceState *****  ");
    }

    @Override
    public void onPause() {
        // TODO Auto-generated method stub
        super.onPause();

        Log.d(Const.LOG_TAG, " ***** HotelSearchResultMapFragment, in onPause *****  ");

    }

    @Override
    public void onResume() {
        // TODO Auto-generated method stub
        super.onResume();

    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        Fragment fragment = (getFragmentManager().findFragmentById(R.id.map));
        FragmentTransaction ft = getActivity().getFragmentManager().beginTransaction();
        ft.remove(fragment);
        ft.commit();
    }

    /**
     * Google map setUp from MapFragment
     */
    private void setUpMap() {
        if (googleMap == null) {
            mapFragment = ((MapFragment) getFragmentManager().findFragmentById(R.id.map));
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
        // CameraUpdate cu = CameraUpdateFactory.newLatLngZoom(position, 14);

        googleMap.animateCamera(cu);
        googleMap.setInfoWindowAdapter(new HotelInfoWindowAdapter());

        googleMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {

            @Override public boolean onMarkerClick(Marker marker) {
                itemClicked = markerMap.get(marker);
                marker.showInfoWindow();
                itemClicked.buildView(getActivity(), hotelInfoView, null);
                if (previousMarker != null) {
                    previousMarker.setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE));
                }
                marker.setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
                marker.setVisible(true);
                previousMarker = marker;
                return true;
            }
        });
    }

    private void addHotelMarkers(List<HotelSearchResultListItem> hotels) {
        builder = new LatLngBounds.Builder();
        boolean firstMarker = true;
        for (HotelSearchResultListItem item : hotels) {
            LatLng position = new LatLng(item.getHotel().latitude, item.getHotel().longitude);
            MarkerOptions marker = new MarkerOptions().position(position);
            marker.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE));

            Marker hotelMarker = googleMap.addMarker(marker);
            builder.include(position);
            markerMap.put(hotelMarker, item);
            if (firstMarker) {
                hotelMarker.showInfoWindow();
                View v = item.buildView(getActivity(), hotelInfoView, null);
                hotelMarker.setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
                firstMarker = false;
                previousMarker = hotelMarker;
                itemClicked = item;

            }
        }
    }

    @Override
    public void onMapReady(GoogleMap map) {
        googleMap = map;
        addMarkers();

    }

    private class HotelInfoWindowAdapter implements GoogleMap.InfoWindowAdapter {

        @Override
        public View getInfoContents(Marker marker) {

            LayoutInflater inflater = null;

            inflater = LayoutInflater.from(getActivity());

            View v = inflater.inflate(R.layout.empty_info_window, null);

            return v;

        }

        @Override
        public View getInfoWindow(Marker marker) {

            return null;
        }

    }

    // Container Activity must implement this call back interface
    public interface HotelSearchMapsFragmentListener {

        public void hotelListItemClicked(HotelSearchResultListItem itemClicked);
    }

}
