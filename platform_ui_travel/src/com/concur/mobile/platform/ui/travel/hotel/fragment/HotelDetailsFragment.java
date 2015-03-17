package com.concur.mobile.platform.ui.travel.hotel.fragment;

import android.app.Activity;
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
import com.google.android.gms.maps.GoogleMapOptions;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;

/**
 * Fragment for Hotel Details tab
 * 
 * @author tejoa
 * 
 */
public class HotelDetailsFragment extends PlatformFragmentV1 implements OnClickListener {

    public static final String TAB_ROOMS = "ROOMS";
    public static final String FRAGMENT_HOTEL_MAP = "FRAGMENT_HOTEL_MAP";
    private Hotel hotel;
    private MapFragment mapFragment;
    private ListItemAdapter<HotelRoomListItem> listItemAdapater;
    private HotelChoiceDetailsFragmentListener callBackListener;
    private LatLng post;
    private Button findRooms;

    // private int paramsHeight;
    // private MapFragment mapFragment;
    // private LatLng post;

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

        post = new LatLng(hotel.latitude, hotel.longitude);

        GoogleMapOptions options = new GoogleMapOptions().liteMode(true);
        CameraPosition camera = new CameraPosition(post, 15, 0, 0);
        options.camera(camera);

        mapFragment = MapFragment.newInstance(options);
        mapFragment.getMapAsync((OnMapReadyCallback) getActivity());
        //
        getFragmentManager().beginTransaction().replace(R.id.map_view, mapFragment).commit();
        // mapFrame.setOnClickListener(this);
        // mapFrame.getLayoutParams().height = 0;
        // mapView.getParent().
        // mapFrame.removeAllViews();
        // mapFrame.addView(mapView);

        // getFragmentManager().beginTransaction().replace(R.id.map_view, new HotelMapFragment(post, true)).commit();
        // if (mapFrame != null) {
        // mapFrame.setOnClickListener(this);
        // }
        //
        // }

        Contact contact = hotel.contact;
        if (contact != null && contact.city != null) {
            // if (ViewUtil.isMappingAvailable(context)) {
            // ViewUtil.setText(mainView, R.id.hotel_address, hotel.contact.city, Linkify.MAP_ADDRESSES);
            // ViewUtil.setVisibility(mainView, R.id.hotel_address, View.VISIBLE);
            // } else {
            StringBuilder stb = new StringBuilder();
            if (!contact.addressLine1.isEmpty()) {
                stb.append(contact.addressLine1).append(", ");
            }
            stb.append(com.concur.mobile.base.util.Format.localizeText(getActivity(),
                    R.string.general_citystatecountry, contact.street, contact.city, contact.state, contact.country,
                    contact.zip));
            TextView tv = ((TextView) mainView.findViewById(R.id.hotel_address));
            tv.setText(stb.toString());
            tv.setTextIsSelectable(true);

        } else {
            ViewUtil.setVisibility(mainView, R.id.hotel_address, View.GONE);
        }

        String s = contact.phone.trim();
        if (!s.isEmpty()) {
            String formattedNumber = PhoneNumberUtils.formatNumber(contact.phone);
            // String formattedNumber = String.format("(%s) %s %s", s.subSequence(0, 3), s.subSequence(3, 6),
            // s.subSequence(6, 10));
            TextView tv = ((TextView) mainView.findViewById(R.id.hotel_phone));
            tv.setText(formattedNumber);

            Linkify.addLinks(tv, Linkify.PHONE_NUMBERS);
            com.concur.mobile.platform.ui.travel.util.ViewUtil.stripUnderlines(tv);
        } else {
            ViewUtil.setVisibility(mainView, R.id.hotel_phone, View.GONE);
        }

        if (hotel.rates != null && hotel.rates.size() > 0) {
            findRooms = (Button) mainView.findViewById(R.id.full_button);
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

        // if (retainer.contains(STATE_HOTEL_LIST_ITEMS_KEY)) {
        // hotelListItems = (List<HotelSearchResultListItem>) retainer.get(STATE_HOTEL_LIST_ITEMS_KEY);
        // }

        // Log.d(Const.LOG_TAG, " ***** HotelSearchResultFragment, in onResume *****  hotelListItems = "
        // + (hotelListItems != null ? hotelListItems.size() : 0));
    }

    @Override
    public void onClick(View v) {
        // if (v == mapFrame) {
        // callBackListener.onMapsClicked(post);
        // } else
        if (v == findRooms) {
            callBackListener.onFindRoomsClicked();
        }

    }

    // public void showFullMap(View view) {
    // callBackListener.onMapsClicked(post);
    // }

    // @Override
    // public void onDestroyView() {
    // super.onDestroyView();
    // getFragmentManager().beginTransaction().remove(getFragmentManager().findFragmentById(R.layout.map_view))
    // .commit();
    // }
    // }

}
