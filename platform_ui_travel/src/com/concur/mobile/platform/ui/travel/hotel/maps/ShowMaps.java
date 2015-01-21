package com.concur.mobile.platform.ui.travel.hotel.maps;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;

import com.concur.mobile.platform.travel.search.hotel.Hotel;
import com.concur.mobile.platform.ui.travel.R;
import com.concur.mobile.platform.ui.travel.hotel.fragment.HotelSearchResultListItem;
import com.concur.mobile.platform.ui.travel.util.Const;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

/**
 * Activity to show all hotels in Google Maps
 * 
 * @author tejoa
 * 
 */

public class ShowMaps extends Activity {

    private GoogleMap googleMap;
    /*
     * Hashmap for all hotel markers and hotel list items
     */
    private Map<Marker, HotelSearchResultListItem> markerMap = new HashMap<Marker, HotelSearchResultListItem>();
    private List<HotelSearchResultListItem> hotels;

    @SuppressWarnings("unchecked")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.map_layout);
        // googleMap initialized
        setUpMap();
        if (googleMap != null) {

            Intent i = this.getIntent();
            final Bundle bundle = i.getExtras();
            hotels = new ArrayList<HotelSearchResultListItem>();
            hotels = ((ArrayList<HotelSearchResultListItem>) bundle.getSerializable(Const.EXTRA_HOTELS_LIST));

            addHotelMarkers(hotels);
            Hotel firstHotel = hotels.get(0).getHotel();
            // TODO load custom icons
            LatLng position = new LatLng(firstHotel.latitude, firstHotel.longitude);
            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(position, 15));
            googleMap.setInfoWindowAdapter(new HotelInfoWindowAdapter());
        }

    }

    private void setUpMap() {
        if (googleMap == null) {
            googleMap = ((MapFragment) getFragmentManager().findFragmentById(R.id.map)).getMap();
        }
    }

    private void addHotelMarkers(List<HotelSearchResultListItem> hotels) {

        for (HotelSearchResultListItem item : hotels) {
            LatLng position = new LatLng(item.getHotel().latitude, item.getHotel().longitude);
            MarkerOptions marker = new MarkerOptions().position(position);

            markerMap.put(googleMap.addMarker(marker), item);
        }
    }

    private class HotelInfoWindowAdapter implements GoogleMap.InfoWindowAdapter {

        @Override
        public View getInfoContents(Marker marker) {
            HotelSearchResultListItem item = markerMap.get(marker);

            View hotelView = null;
            LayoutInflater inflater = null;

            inflater = LayoutInflater.from(getApplicationContext());

            hotelView = inflater.inflate(R.layout.hotel_search_result_row, null);
            View v = item.buildView(getApplicationContext(), hotelView, null);
            //

            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT);
            lp.gravity = Gravity.BOTTOM;
            v.setLayoutParams(lp);
            return v;

        }

        @Override
        public View getInfoWindow(Marker arg0) {
            // TODO Auto-generated method stub
            return null;
        }

    }

}
