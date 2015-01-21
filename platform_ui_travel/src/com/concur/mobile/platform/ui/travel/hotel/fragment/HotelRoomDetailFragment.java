package com.concur.mobile.platform.ui.travel.hotel.fragment;

import java.util.ArrayList;
import java.util.List;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.LinearLayout.LayoutParams;
import android.widget.ListView;
import android.widget.TextView;

import com.concur.mobile.platform.travel.search.hotel.HotelRate;
import com.concur.mobile.platform.ui.common.fragment.PlatformFragmentV1;
import com.concur.mobile.platform.ui.common.view.ListItemAdapter;
import com.concur.mobile.platform.ui.travel.hotel.activity.HotelBookingActivity;
import com.concur.mobile.platform.ui.travel.hotel.fragment.HotelChoiceDetailsFragment.HotelChoiceDetailsFragmentListener;

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
        // View mainView = inflater.inflate(R.layout.hotel_rooms_layout1, container, false);

        // ListView listView = (ListView) mainView.findViewById(R.id.hotel_rooms_list_view);

        ListView listView = new ListView(getActivity());
        listView.setBackgroundColor(Color.parseColor("#FFFFFF"));
        LayoutParams lp = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
        listView.setLayoutParams(lp);
        // Configuration config = getResources().getConfiguration();
        if (hotelRooms != null && hotelRooms.size() > 0) {
            listItemAdapater = new ListItemAdapter<HotelRoomListItem>(getActivity().getApplicationContext(), hotelRooms);
            listView.setAdapter(listItemAdapater);

            listView.setOnItemClickListener(new OnItemClickListener() {

                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    roomItemClicked((HotelRoomListItem) parent.getItemAtPosition(position));
                }
            });

            return listView;
        } else {
            TextView tv = new TextView(getActivity());
            tv.setTextSize(25);
            tv.setGravity(Gravity.CENTER_HORIZONTAL);
            tv.setText("No Rooms Available");
            return tv;

        }
    }

    private void roomItemClicked(HotelRoomListItem roomListItem) {
        Intent intent = new Intent(getActivity(), HotelBookingActivity.class);
        intent.putExtra("roomSelected", roomListItem.getHotelRoom());

        startActivity(intent);

    }
}
