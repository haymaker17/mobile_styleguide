package com.concur.mobile.platform.ui.travel.hotel.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.concur.mobile.platform.ui.common.fragment.PlatformFragmentV1;
import com.concur.mobile.platform.ui.travel.R;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

/**
 * Fragment to show Hotel Map
 * 
 * @author tejoa
 * 
 */
public class HotelMapFragment extends PlatformFragmentV1 {

    private static GoogleMap googleMap;
    private LatLng position;

    public HotelMapFragment(LatLng position) {
        this.position = position;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        // inflate the details fragment
        View mainView = inflater.inflate(R.layout.map_layout, container, false);

        setUpMap();
        if (googleMap != null) {

            // Intent i = getActivity().getIntent();
            // TODO load custom icons
            // position = i.getParcelableExtra(Const.EXTRA_HOTEL_LOCATION);
            addMarkers();

        }

        return mainView;
    }

    private void setUpMap() {
        if (googleMap == null) {
            MapFragment mapFragment = ((MapFragment) getFragmentManager().findFragmentById(R.id.map));
            ViewGroup.LayoutParams params = mapFragment.getView().getLayoutParams();

            // // paramsHeight = 96;
            // // params.height = paramsHeight;
            // // mapFragment.getView().setLayoutParams(params);
            googleMap = mapFragment.getMap();
            if (params.height == 96) {
                googleMap.getUiSettings().setZoomControlsEnabled(false);
            }

        }
    }

    private void addMarkers() {
        MarkerOptions marker = new MarkerOptions().position(position);
        googleMap.addMarker(marker);
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(position, 15));
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        if (googleMap != null)
            addMarkers();

        if (googleMap == null) {
            setUpMap();
            addMarkers();
        }
    }

    /****
     * The mapfragment's id must be removed from the FragmentManager or else if the same it is passed on the next time then app
     * will crash
     ****/
    // @Override
    // public void onDestroyView() {
    // super.onDestroyView();
    // if (googleMap != null) {
    // getFragmentManager().beginTransaction().remove(getFragmentManager().findFragmentById(R.layout.map_layout))
    // .commit();
    // googleMap = null;
    // }
    // }
}
