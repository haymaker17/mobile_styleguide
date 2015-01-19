package com.concur.mobile.platform.ui.travel.hotel.fragment;

import android.app.FragmentManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.concur.mobile.platform.travel.search.hotel.Hotel;
import com.concur.mobile.platform.ui.common.fragment.PlatformFragmentV1;
import com.concur.mobile.platform.ui.common.util.ViewUtil;
import com.concur.mobile.platform.ui.common.view.ListItemAdapter;
import com.concur.mobile.platform.ui.travel.R;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;

/**
 * Fragment for Hotel Details tab
 * 
 * @author tejoa
 * 
 */
public class HotelDetailsFragment extends PlatformFragmentV1 {

    private Hotel hotel;
    private GoogleMap hotelMap;
    SupportMapFragment mSupportMapFragment;
    private ListItemAdapter<HotelRoomListItem> listItemAdapater;

    public HotelDetailsFragment(Hotel hotel) {
        this.hotel = hotel;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        super.onCreateView(inflater, container, savedInstanceState);

        // inflate the details fragment
        View mainView = inflater.inflate(R.layout.hotel_details_layout, null, false);
        // mSupportMapFragment = SupportMapFragment.newInstance();

        // FragmentManager fm = getChildFragmentManager();
        // SupportMapFragment fragment = (SupportMapFragment) fm.findFragmentById(R.id.map_view);
        // if (fragment == null) {
        // fragment = SupportMapFragment.newInstance();
        // fm.beginTransaction().replace(R.id.map_view, fragment).commit();
        // }
        //
        // if (hotelMap == null) {
        // hotelMap = ((MapFragment) getFragmentManager().findFragmentById(R.id.map_view)).getMap();
        // }
        //
        // // MapView hotelMap = (MapView) mainView.findViewById(R.id.map);
        // if (hotelMap != null) {
        //
        // LatLng post = new LatLng(hotel.latitude, hotel.longitude);
        // // TODO load custom icons
        // MarkerOptions marker = new MarkerOptions().position(post);
        // hotelMap.addMarker(marker);
        // hotelMap.moveCamera(CameraUpdateFactory.newLatLngZoom(post, 15));
        // FragmentManager fm = getFragmentManager();
        // fm.beginTransaction().add(hotelMap, "Maps").commit();
        // }
        if (hotel.contact != null && hotel.contact.city != null) {
            // if (ViewUtil.isMappingAvailable(context)) {
            // ViewUtil.setText(mainView, R.id.hotel_address, hotel.contact.city, Linkify.MAP_ADDRESSES);
            // ViewUtil.setVisibility(mainView, R.id.hotel_address, View.VISIBLE);
            // } else {
            if (hotel.contact.getAddress() != null) {
                ((TextView) mainView.findViewById(R.id.hotel_address)).setText(hotel.contact.getAddress());

            } else {
                ViewUtil.setVisibility(mainView, R.id.hotel_address, View.GONE);
            }

            if (hotel.contact.phone != null) {
                ((TextView) mainView.findViewById(R.id.hotel_phone)).setText(hotel.contact.phone);
            }

        }
        if (hotel.rates != null && hotel.rates.size() > 0) {
            Button findRooms = (Button) mainView.findViewById(R.id.full_button);
            if (findRooms != null) {
                findRooms.setText(getText(R.string.find_rooms_button));
            }
            findRooms.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    FragmentManager fm = getFragmentManager();
                    fm.beginTransaction().add(new HotelRoomDetailFragment(hotel.rates), "ROOMS").commit();
                }
            });

        }

        return mainView;
    }
}