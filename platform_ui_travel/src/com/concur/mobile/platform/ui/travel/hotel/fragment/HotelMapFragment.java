package com.concur.mobile.platform.ui.travel.hotel.fragment;

import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.concur.mobile.platform.ui.common.fragment.PlatformFragmentV1;
import com.concur.mobile.platform.ui.travel.R;
import com.concur.mobile.platform.ui.travel.util.Const;
import com.google.android.m4b.maps.CameraUpdateFactory;
import com.google.android.m4b.maps.GoogleMap;
import com.google.android.m4b.maps.MapFragment;
import com.google.android.m4b.maps.OnMapReadyCallback;
import com.google.android.m4b.maps.model.BitmapDescriptorFactory;
import com.google.android.m4b.maps.model.LatLng;
import com.google.android.m4b.maps.model.MarkerOptions;

/**
 * Fragment to show Hotel Map
 *
 * @author tejoa
 */
public class HotelMapFragment extends PlatformFragmentV1 implements OnMapReadyCallback {

    private static GoogleMap googleMap;
    private LatLng position;
    private MapFragment hotelmapFragment;
    private ImageView snapshotHolder;
    public Bundle args;
    private double latitude;
    private double longitude;
    private boolean searchNearMe;
    private View mainView;

    // empty constructor
    public HotelMapFragment() {
        setRetainInstance(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        args = getArguments();
        //Google maps are failing for S3 and HTC devices
        if (mainView == null) {
            // inflate the details fragment
            mainView = inflater.inflate(R.layout.hotel_map_layout, container, false);
        }

        setUpMap();
        if (googleMap != null) {

            addMarkers();

            googleMap.getUiSettings().setZoomControlsEnabled(false);
        }

        return mainView;
    }

    private void setUpMap() {
        if (googleMap == null) {
            hotelmapFragment = null;
            FragmentManager fm = null;
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
                fm = getFragmentManager();
            } else {
                fm = getChildFragmentManager();
            }
            hotelmapFragment = (MapFragment) fm.findFragmentById(R.id.hotel_map);
            hotelmapFragment.getMapAsync(this);
        }
    }

    private void addMarkers() {
        if (googleMap != null) {
            latitude = Double.valueOf(args.getString(Const.EXTRA_TRAVEL_LATITUDE));
            longitude = Double.valueOf(args.getString(Const.EXTRA_TRAVEL_LONGITUDE));
            searchNearMe = args.getBoolean(Const.EXTRA_TRAVEL_SEARCH_NEAR_ME, false);
            position = new LatLng(latitude, longitude);
            MarkerOptions marker = new MarkerOptions().position(position);
            marker.icon(BitmapDescriptorFactory.fromResource(R.drawable.map_pin_red));
            googleMap.addMarker(marker);
            if (searchNearMe) {
                googleMap.setMyLocationEnabled(true);
            }
            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(position, 17));
        }
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        if (googleMap != null)
            addMarkers();

        if (googleMap == null) {
            setUpMap();
        }
    }

    // @Override
    // public void onClick(View arg0) {
    // // TODO Auto-generated method stub
    //
    // }

    /**
     * *
     * The mapfragment's id must be removed from the FragmentManager or else if the same it is passed on the next time then app
     * will crash
     * **
     */
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (hotelmapFragment != null && Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            FragmentTransaction ft = getActivity().getFragmentManager().beginTransaction();
            ft.remove(hotelmapFragment);
            ft.commit();
        }

        googleMap = null;

    }

    /**
     * Called when the clear button is clicked.
     */
    public void onClearScreenshot(View view) {
        // ImageView snapshotHolder = (ImageView) findViewById(R.id.snapshot_holder);
        snapshotHolder.setImageDrawable(null);
    }

    @Override
    public void onMapReady(GoogleMap map) {
        googleMap = map;
        addMarkers();

    }

}
