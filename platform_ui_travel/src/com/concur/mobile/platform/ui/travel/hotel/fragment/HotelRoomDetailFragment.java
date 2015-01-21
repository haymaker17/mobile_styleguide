package com.concur.mobile.platform.ui.travel.hotel.fragment;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.TextView;

import com.concur.mobile.platform.travel.search.hotel.HotelRate;
import com.concur.mobile.platform.ui.common.fragment.PlatformFragmentV1;
import com.concur.mobile.platform.ui.common.view.ListItemAdapter;
import com.concur.mobile.platform.ui.travel.R;
import com.concur.mobile.platform.ui.travel.hotel.activity.HotelBookingActivity;
import com.concur.mobile.platform.ui.travel.hotel.fragment.HotelChoiceDetailsFragment.HotelChoiceDetailsFragmentListener;
import com.concur.mobile.platform.ui.travel.util.Const;

/**
 * Fragment for Hotel Room Details tab
 * 
 * @author tejoa
 * 
 */
// @SuppressLint("ResourceAsColor")
public class HotelRoomDetailFragment extends PlatformFragmentV1 {

    private List<HotelRoomListItem> hotelRooms;
    private HotelRoomListItem room;
    private ListItemAdapter<HotelRoomListItem> listItemAdapater;
    private HotelChoiceDetailsFragmentListener callBackListener;
    private View mainView;

    public HotelRoomDetailFragment(List<HotelRate> rooms) {
        hotelRooms = new ArrayList<HotelRoomListItem>();
        for (HotelRate r : rooms) {
            room = new HotelRoomListItem(r);
            hotelRooms.add(room);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        // inflate the details fragment
        mainView = inflater.inflate(R.layout.hotel_rooms_layout1, container, false);

        ListView listView = (ListView) mainView.findViewById(R.id.hotel_rooms_list_view);
        TextView tv = (TextView) mainView.findViewById(R.id.no_rooms);
        listView.setVisibility(View.GONE);
        tv.setVisibility(View.GONE);
        // Configuration config = getResources().getConfiguration();
        if (hotelRooms != null && hotelRooms.size() > 0) {
            listItemAdapater = new ListItemAdapter<HotelRoomListItem>(getActivity().getApplicationContext(), hotelRooms);
            listView.setAdapter(listItemAdapater);
            listView.setVisibility(View.VISIBLE);

            listView.setOnItemClickListener(new OnItemClickListener() {

                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                    callBackListener.roomItemClicked((HotelRoomListItem) parent.getItemAtPosition(position));
                }
            });

        } else {
            tv.setVisibility(View.VISIBLE);
        }
        return mainView;
    }

    private void roomItemClicked(HotelRoomListItem roomListItem) {
        Intent intent = new Intent(getActivity(), HotelBookingActivity.class);
        intent.putExtra("roomSelected", roomListItem.getHotelRoom());

        startActivity(intent);

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
        // (hotelListItems != null ? hotelListItems.size() : 0));
    }

}
