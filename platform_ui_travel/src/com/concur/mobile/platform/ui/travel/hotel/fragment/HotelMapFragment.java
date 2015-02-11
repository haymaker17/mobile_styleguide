package com.concur.mobile.platform.ui.travel.hotel.fragment;

import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import com.concur.mobile.platform.ui.common.fragment.PlatformFragmentV1;
import com.concur.mobile.platform.ui.travel.R;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

/**
 * Fragment to show Hotel Map
 * 
 * @author tejoa
 * 
 */
public class HotelMapFragment extends PlatformFragmentV1 implements OnMapReadyCallback {

    private static GoogleMap googleMap;
    private LatLng position;
    private MapFragment mapFragment;
    private ImageView snapshotHolder;

    public HotelMapFragment(LatLng position) {
        this.position = position;
    }

    // empty constructor
    public HotelMapFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        // inflate the details fragment
        View mainView = inflater.inflate(R.layout.map_layout, container, false);

        setUpMap();
        if (googleMap != null) {

            addMarkers();

            googleMap.getUiSettings().setZoomControlsEnabled(false);
        }

        return mainView;
    }

    private void setUpMap() {
        if (googleMap == null) {
            Activity activity = getActivity();
            int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(activity);
            if (resultCode == ConnectionResult.SUCCESS) {
                mapFragment = ((MapFragment) getFragmentManager().findFragmentById(R.id.map));

                mapFragment.getMapAsync(this);

            } else {
                Toast.makeText(activity, "Map Unavailable", Toast.LENGTH_LONG).show();
            }

        }
    }

    private void addMarkers() {
        if (googleMap != null) {
            MarkerOptions marker = new MarkerOptions().position(position);
            googleMap.addMarker(marker);
            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(position, 15));
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

    /****
     * The mapfragment's id must be removed from the FragmentManager or else if the same it is passed on the next time then app
     * will crash
     ****/
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // if (googleMap != null) {
        // getFragmentManager().beginTransaction().remove(getFragmentManager().findFragmentById(R.layout.map_layout))
        // .commit();
        googleMap = null;
        // }

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
