package com.concur.mobile.platform.ui.travel.hotel.fragment;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.telephony.PhoneNumberUtils;
import android.text.util.Linkify;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import com.concur.mobile.platform.travel.search.hotel.Contact;
import com.concur.mobile.platform.travel.search.hotel.Hotel;
import com.concur.mobile.platform.ui.common.fragment.PlatformFragmentV1;
import com.concur.mobile.platform.ui.common.util.ViewUtil;
import com.concur.mobile.platform.ui.common.view.ListItemAdapter;
import com.concur.mobile.platform.ui.travel.R;
import com.concur.mobile.platform.ui.travel.hotel.fragment.HotelChoiceDetailsFragment.HotelChoiceDetailsFragmentListener;
import com.concur.mobile.platform.ui.travel.util.Const;
import com.google.android.m4b.maps.GoogleMap;
import com.google.android.m4b.maps.GoogleMapOptions;
import com.google.android.m4b.maps.MapFragment;
import com.google.android.m4b.maps.OnMapReadyCallback;
import com.google.android.m4b.maps.model.*;

/**
 * Fragment for Hotel Details tab
 *
 * @author tejoa
 */
public class HotelDetailsFragment extends PlatformFragmentV1 implements OnClickListener, OnMapReadyCallback {

    public static final String TAB_ROOMS = "ROOMS";
    public static final String FRAGMENT_HOTEL_MAP = "FRAGMENT_HOTEL_MAP";
    private Hotel hotel;
    private MapFragment mapFragment;
    private static GoogleMap googleMap;
    private ListItemAdapter<HotelRoomListItem> listItemAdapater;
    private HotelChoiceDetailsFragmentListener callBackListener;
    private LatLng post;
    private Button findRooms;
    private View mapView;
    private HotelDetailsFragmentListener detailsFragCallbackListener;

    public HotelDetailsFragment(Hotel hotel) {
        this.hotel = hotel;
    }

    // empty constructor
    public HotelDetailsFragment() {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        super.onCreateView(inflater, container, savedInstanceState);

        // inflate the details fragment
        View mainView = inflater.inflate(R.layout.hotel_details_layout, container, false);

        if (hotel != null && hotel.latitude != null && hotel.longitude != null) {
            post = new LatLng(hotel.latitude, hotel.longitude);

            GoogleMapOptions options = new GoogleMapOptions().liteMode(true);
            options.mapToolbarEnabled(false);
            CameraPosition camera = new CameraPosition(post, 15, 0, 0);
            options.camera(camera);

            mapView = mainView.findViewById(R.id.map_view);
            mapFragment = MapFragment.newInstance(options);
            mapFragment.getMapAsync(this);
            //
            getFragmentManager().beginTransaction().replace(R.id.map_view, mapFragment).commit();
        }

        //add hotel name
        TextView name = ((TextView) mainView.findViewById(R.id.hotel_name));
        name.setText(com.concur.mobile.base.util.Format.localizeText(getActivity(), hotel.name));
        name.setTextIsSelectable(true);

        Contact contact = hotel.contact;
        if (contact != null && contact.city != null) {

            StringBuilder stb = new StringBuilder();
            if (!contact.addressLine1.isEmpty()) {
                stb.append(contact.addressLine1).append(", ");
            }
            if (!contact.street.isEmpty()) {
                stb.append(com.concur.mobile.base.util.Format.localizeText(getActivity(), contact.street) + ", \n");
            }
            if (!contact.city.isEmpty()) {
                stb.append(com.concur.mobile.base.util.Format.localizeText(getActivity(), contact.city)).append((", "));
            }
            if (!contact.state.isEmpty()) {
                stb.append(com.concur.mobile.base.util.Format.localizeText(getActivity(), contact.state))
                        .append((", "));
            }
            if (!contact.country.isEmpty()) {
                stb.append(com.concur.mobile.base.util.Format.localizeText(getActivity(), contact.country))
                        .append((", "));
            }
            if (!contact.zip.isEmpty()) {
                stb.append(com.concur.mobile.base.util.Format.localizeText(getActivity(), contact.zip));
            }

            //            stb.append(com.concur.mobile.base.util.Format.localizeText(getActivity(), contact.street) + ", \n");
            //            stb.append(com.concur.mobile.base.util.Format
            //                    .localizeText(getActivity(), R.string.hotel_citystatecountry, contact.city, contact.state,
            //                            contact.country, contact.zip));
            TextView tv = ((TextView) mainView.findViewById(R.id.hotel_address));
            tv.setText(stb.toString());
            tv.setTextIsSelectable(true);

        } else {
            ViewUtil.setVisibility(mainView, R.id.hotel_address, View.GONE);
        }

        String s = contact.phone.trim();
        if (!s.isEmpty()) {
            final String formattedNumber = PhoneNumberUtils.formatNumber(s);
            // String formattedNumber = String.format("(%s) %s %s", s.subSequence(0, 3), s.subSequence(3, 6),
            // s.subSequence(6, 10));
            TextView tv = ((TextView) mainView.findViewById(R.id.hotel_phone));
            tv.setText(formattedNumber);

            tv.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    detailsFragCallbackListener.callHotel(formattedNumber);
                }
            });

            //Linkify.addLinks(tv, Linkify.PHONE_NUMBERS);
            //com.concur.mobile.platform.ui.travel.util.ViewUtil.stripUnderlines(tv);


        } else {
            ViewUtil.setVisibility(mainView, R.id.hotel_phone, View.GONE);
        }

        if (hotel.rates != null && hotel.rates.size() > 0) {
            findRooms = (Button) mainView.findViewById(R.id.footer_button);
            if (findRooms != null) {
                findRooms.setText(getText(R.string.find_rooms_button));
            }
            findRooms.setOnClickListener(this);
        }

        return mainView;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        // This makes sure that the container activity has implemented
        // the callback interface. If not, it throws an exception
        try {
            callBackListener = (HotelChoiceDetailsFragmentListener) activity;
            detailsFragCallbackListener = (HotelDetailsFragmentListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement HotelSearchResultsFragmentListener");
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        // TODO Auto-generated method stub
        super.onSaveInstanceState(outState);

        Log.d(Const.LOG_TAG, " ***** HotelChoiceDetailsFragment, in onSaveInstanceState *****  ");
    }

    @Override
    public void onPause() {
        // TODO Auto-generated method stub
        super.onPause();

        Log.d(Const.LOG_TAG, " ***** HotelChoiceDetailsFragment, in onPause *****  ");

        // retainer.put(STATE_HOTEL_LIST_ITEMS_KEY, hotelListItems);
    }

    @Override
    public void onResume() {
        // TODO Auto-generated method stub
        super.onResume();
    }

    @Override
    public void onClick(View v) {
        if (v == findRooms) {
            callBackListener.onFindRoomsClicked();
        } else if (v == mapView) {
            callBackListener.onMapsClicked(false);
        }

    }

    @Override
    public void onMapReady(GoogleMap map) {
        googleMap = map;
        MarkerOptions marker = new MarkerOptions().position(post);
        marker.icon(BitmapDescriptorFactory.fromResource(R.drawable.map_pin_red));
        googleMap.addMarker(marker);
        googleMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {

            @Override public boolean onMarkerClick(Marker marker) {
                callBackListener.onMapsClicked(true);
                return true;
            }
        });

        googleMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {

            @Override public void onMapClick(LatLng latLng) {
                callBackListener.onMapsClicked(true);
            }

        });
    }

    // Container Activity must implement this call back interface
    public interface HotelDetailsFragmentListener {
        public void callHotel(String phoneNumberCleaned);
    }

}
