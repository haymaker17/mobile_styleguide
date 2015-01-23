package com.concur.mobile.platform.ui.travel.hotel.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;

import com.concur.mobile.platform.ui.common.fragment.PlatformFragmentV1;
import com.concur.mobile.platform.ui.travel.R;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnMapClickListener;
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
    private MapFragment mapFragment;

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

            googleMap.getUiSettings().setZoomControlsEnabled(false);
            googleMap.setOnMapClickListener(new OnMapClickListener() {

                @Override
                public void onMapClick(LatLng arg0) {

                    View view = mapFragment.getView();
                    LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT,
                            LayoutParams.MATCH_PARENT);
                    view.setLayoutParams(p);
                    view.requestLayout();

                    // LayoutParams params = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
                    // mapFragment.getFragmentManager().;

                }
            });
        }

        return mainView;
    }

    private void setUpMap() {
        if (googleMap == null) {
            mapFragment = ((MapFragment) getFragmentManager().findFragmentById(R.id.map));
            ViewGroup.LayoutParams params = mapFragment.getView().getLayoutParams();

            // // paramsHeight = 96;
            // // params.height = paramsHeight;
            // // mapFragment.getView().setLayoutParams(params);
            googleMap = mapFragment.getMap();

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

    // private void resizeFragment(Fragment f, int newWidth, int newHeight) {
    // if (f != null) {
    // View view = f.getView();
    // RelativeLayout.LayoutParams p = new RelativeLayout.LayoutParams(newWidth, newHeight);
    // view.setLayoutParams(p);
    // view.requestLayout();
    //
    // }
    // }

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
