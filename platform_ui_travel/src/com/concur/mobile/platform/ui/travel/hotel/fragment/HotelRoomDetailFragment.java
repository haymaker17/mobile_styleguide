package com.concur.mobile.platform.ui.travel.hotel.fragment;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.TextView;
import com.concur.mobile.platform.travel.search.hotel.HotelRate;
import com.concur.mobile.platform.ui.common.fragment.PlatformFragmentV1;
import com.concur.mobile.platform.ui.common.util.FormatUtil;
import com.concur.mobile.platform.ui.common.view.ListItemAdapter;
import com.concur.mobile.platform.ui.travel.R;
import com.concur.mobile.platform.ui.travel.hotel.fragment.HotelChoiceDetailsFragment.HotelChoiceDetailsFragmentListener;
import com.concur.mobile.platform.ui.travel.util.Const;
import com.concur.mobile.platform.ui.travel.view.CustomListView;

import java.util.ArrayList;
import java.util.List;

/**
 * Fragment for Hotel Room Details tab
 *
 * @author tejoa
 */
// @SuppressLint("ResourceAsColor")
public class HotelRoomDetailFragment extends PlatformFragmentV1 {

    private List<HotelRoomListItem> hotelRooms;
    private HotelRoomListItem room;
    private ListItemAdapter<HotelRoomListItem> listItemAdapater;
    private HotelChoiceDetailsFragmentListener callBackListener;
    private View mainView;
    public Double priceToBeat;

    public HotelRoomDetailFragment(List<HotelRate> rooms, boolean showGDSName) {
        hotelRooms = new ArrayList<HotelRoomListItem>();
        for (HotelRate r : rooms) {
            room = new HotelRoomListItem(r, showGDSName);
            hotelRooms.add(room);
        }
    }

    // empty constructor
    public HotelRoomDetailFragment() {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        // inflate the details fragment
        mainView = inflater.inflate(R.layout.hotel_rooms_layout1, container, false);

        // show the price to beat header
        if (hotelRooms != null && hotelRooms.size() > 0 && priceToBeat != null) {
            mainView.findViewById(R.id.priceToBeatView).setVisibility(View.VISIBLE);
            TextView priceToBeatView = (TextView) mainView.findViewById(R.id.priceToBeatText);
            priceToBeatView.setVisibility(View.VISIBLE);
            priceToBeatView.setText(getText(R.string.price_to_beat_label) + " : " + FormatUtil
                    .formatAmountWithNoDecimals(priceToBeat, this.getResources().getConfiguration().locale,
                            hotelRooms.get(0).getHotelRoom().currency, true, true));
        }// end of price to beat header

        CustomListView listView = (CustomListView) mainView.findViewById(R.id.hotel_rooms_list_view);
        TextView tv = (TextView) mainView.findViewById(R.id.no_rooms);
        listView.setVisibility(View.GONE);
        tv.setVisibility(View.GONE);

        // Configuration config = getResources().getConfiguration();
        if (hotelRooms != null && hotelRooms.size() > 0) {
            if (listItemAdapater == null) {
                listItemAdapater = new ListItemAdapter<HotelRoomListItem>(getActivity().getApplicationContext(),
                        hotelRooms);
            }
            listView.setAdapter(listItemAdapater);
            listView.setVisibility(View.VISIBLE);
            //ViewUtil.getListViewSize(listView);

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

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        // This makes sure that the container activity has implemented
        // the callback interface. If not, it throws an exception
        try {
            callBackListener = (HotelChoiceDetailsFragmentListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement HotelChoiceDetailsFragmentListener");
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
