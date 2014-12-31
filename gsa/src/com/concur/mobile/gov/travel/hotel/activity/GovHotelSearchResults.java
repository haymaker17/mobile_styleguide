package com.concur.mobile.gov.travel.hotel.activity;

import java.util.Calendar;

import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;

import com.concur.mobile.core.ConcurCore;
import com.concur.mobile.core.travel.hotel.activity.HotelChoiceListItem;
import com.concur.mobile.core.travel.hotel.activity.HotelSearchResults;
import com.concur.mobile.core.travel.hotel.data.HotelChoice;
import com.concur.mobile.core.travel.hotel.data.HotelChoiceDetail;
import com.concur.mobile.core.travel.hotel.service.HotelSearchReply;
import com.concur.mobile.core.util.Const;
import com.concur.mobile.core.util.Flurry;
import com.concur.mobile.gov.service.GovService;

public class GovHotelSearchResults extends HotelSearchResults {

    private static final String CLS_TAG = GovHotelSearchResults.class.getSimpleName();

    private Double perDiemRate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(Const.LOG_TAG, CLS_TAG + " in gov proj.");
        if (getIntent().getExtras() != null) {
            perDiemRate = getIntent().getDoubleExtra(GovHotelSearch.PER_DIEM_RATE, 0);
        } else {
            perDiemRate = new Double(0);
        }
    }

    @Override
    public void onItemClick(AdapterView<?> list, View view, int position, long id) {

        HotelChoiceListItem hotelChoiceListItem = (HotelChoiceListItem) list.getItemAtPosition(position);
        if (hotelChoiceListItem != null) {
            HotelChoice hotelChoice = hotelChoiceListItem.getHotelChoice();
            if (hotelChoice != null) {
                // Determine if the hotel details are already in our in-memory cache, if so, then
                // re-use them. A request to update will be made in the background.
                ConcurCore app = (ConcurCore) getApplication();
                HotelChoiceDetail hotelChoiceDetail = app.getHotelDetail(hotelChoice.propertyId);
                if (hotelChoiceDetail != null) {
                    startHotelChoiceDetailActivity(hotelChoice.propertyId, true);
                } else {
                    if (ConcurCore.isConnected()) {
                        sendHotelDetailRequest(hotelChoice.propertyId);
                    } else {
                        showDialog(Const.DIALOG_NO_CONNECTIVITY);
                    }
                }
            }
        }
    }

    @Override
    protected Intent getHotelsearchRoomIntent() {
        Intent intent = new Intent(this, GovHotelSearchRooms.class);
        Intent launchIntent = getIntent();
        if (launchIntent.hasExtra(Flurry.PARAM_NAME_BOOKED_FROM)) {
            intent.putExtra(Flurry.PARAM_NAME_BOOKED_FROM, launchIntent.getStringExtra(Flurry.PARAM_NAME_BOOKED_FROM));
        }
        return intent;
    }

    @Override
    protected void sendHotelSearchRequest() {
        Intent intent = getIntent();
        Calendar checkInDateCal = (Calendar) intent
            .getSerializableExtra(Const.EXTRA_TRAVEL_HOTEL_SEARCH_CHECK_IN_CALENDAR);
        Calendar checkOutDateCal = (Calendar) intent
            .getSerializableExtra(Const.EXTRA_TRAVEL_HOTEL_SEARCH_CHECK_OUT_CALENDAR);

        String latitude = intent.getStringExtra(Const.EXTRA_TRAVEL_LATITUDE);
        String longitude = intent.getStringExtra(Const.EXTRA_TRAVEL_LONGITUDE);

        String distanceId = intent.getStringExtra(Const.EXTRA_TRAVEL_HOTEL_SEARCH_DISTANCE_ID);
        String distanceUnitId = intent.getStringExtra(Const.EXTRA_TRAVEL_HOTEL_SEARCH_DISTANCE_UNIT_ID);
        String namesContaining = intent.getStringExtra(Const.EXTRA_TRAVEL_HOTEL_SEARCH_NAMES_CONTAINING);

        HotelSearchReply results = getConcurCore().getHotelSearchResults();
        int startIndex = results.length;
        int count = results.totalCount - results.length;
        count = Math.min(Const.HOTEL_RETRIEVE_COUNT, count);

        // Make the call
        GovService svc = (GovService) getConcurService();
        if (svc != null) {
            if (hotelSearchReceiver == null) {
                hotelSearchReceiver = new HotelSearchReceiver(this);
            }
            if (hotelSearchFilter == null) {
                hotelSearchFilter = new IntentFilter(Const.ACTION_HOTEL_SEARCH_RESULTS);
            }
            getApplicationContext().registerReceiver(hotelSearchReceiver, hotelSearchFilter);
            hotelSearchRequest = svc.searchPerdiemHotel(checkOutDateCal, checkInDateCal, namesContaining, latitude, longitude,
                distanceId, distanceUnitId, startIndex, Const.HOTEL_RETRIEVE_COUNT,
                perDiemRate);
            hotelSearchReceiver.setServiceRequest(hotelSearchRequest);
        }

    }

}
