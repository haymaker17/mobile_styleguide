package com.concur.mobile.platform.ui.travel.hotel.maps;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import com.concur.mobile.platform.ui.travel.R;
import com.concur.mobile.platform.ui.travel.util.Const;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

/**
 * Activity to show all hotels in Google Maps
 * 
 * @author tejoa
 * 
 */

public class ShowHotelMap extends Activity {

    private GoogleMap googleMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.map_layout);
        // googleMap initialized
        setUpMap();
        if (googleMap != null) {

            Intent i = this.getIntent();
            // TODO load custom icons
            LatLng position = i.getParcelableExtra(Const.EXTRA_HOTEL_LOCATION);
            MarkerOptions marker = new MarkerOptions().position(position);
            googleMap.addMarker(marker);
            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(position, 15));
        }

    }

    private void setUpMap() {
        if (googleMap == null) {
            googleMap = ((MapFragment) getFragmentManager().findFragmentById(R.id.map)).getMap();
        }
    }

}
