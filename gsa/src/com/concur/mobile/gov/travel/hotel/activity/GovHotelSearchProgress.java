package com.concur.mobile.gov.travel.hotel.activity;

import java.util.Calendar;

import android.content.Intent;
import android.os.Bundle;

import com.concur.mobile.core.travel.hotel.activity.HotelSearchProgress;
import com.concur.mobile.core.travel.hotel.receiver.HotelSearchReceiver;
import com.concur.mobile.core.util.Const;
import com.concur.mobile.core.util.Flurry;
import com.concur.mobile.gov.service.GovService;

public class GovHotelSearchProgress extends HotelSearchProgress {

    private Double perDiemRate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void initValues() {
        Intent intent = getIntent();

        // Setup our future intents while we are here
        noResultsIntent = (Intent) getIntent().clone();
        noResultsIntent.setClass(this, GovHotelSearchNoResults.class);
        resultsIntent = (Intent) getIntent().clone();
        resultsIntent.setClass(this, GovHotelSearchResults.class);
        if (intent.hasExtra(Flurry.PARAM_NAME_BOOKED_FROM)) {
            resultsIntent.putExtra(Flurry.PARAM_NAME_BOOKED_FROM, intent.getStringExtra(Flurry.PARAM_NAME_BOOKED_FROM));
        }
        checkInDate = intent.getStringExtra(Const.EXTRA_TRAVEL_HOTEL_SEARCH_CHECK_IN);
        checkInDateCal = (Calendar) intent.getSerializableExtra(Const.EXTRA_TRAVEL_HOTEL_SEARCH_CHECK_IN_CALENDAR);
        checkOutDate = intent.getStringExtra(Const.EXTRA_TRAVEL_HOTEL_SEARCH_CHECK_OUT);
        checkOutDateCal = (Calendar) intent.getSerializableExtra(Const.EXTRA_TRAVEL_HOTEL_SEARCH_CHECK_OUT_CALENDAR);

        location = intent.getStringExtra(Const.EXTRA_TRAVEL_HOTEL_SEARCH_LOCATION);

        latitude = intent.getStringExtra(Const.EXTRA_TRAVEL_LATITUDE);
        longitude = intent.getStringExtra(Const.EXTRA_TRAVEL_LONGITUDE);

        distanceId = intent.getStringExtra(Const.EXTRA_TRAVEL_HOTEL_SEARCH_DISTANCE_ID);
        distanceUnitId = intent.getStringExtra(Const.EXTRA_TRAVEL_HOTEL_SEARCH_DISTANCE_UNIT_ID);
        namesContaining = intent.getStringExtra(Const.EXTRA_TRAVEL_HOTEL_SEARCH_NAMES_CONTAINING);

        perDiemRate = intent.getDoubleExtra(GovHotelSearch.PER_DIEM_RATE, 0);
        // send perdiem rate in results intent
        resultsIntent.putExtra(GovHotelSearch.PER_DIEM_RATE, perDiemRate);
    }

    @Override
    protected void startSearch() {

        // Make the call
        GovService svc = (GovService) getConcurService();
        if (svc != null) {
            if (hotelSearchReceiver == null) {
                hotelSearchReceiver = new HotelSearchReceiver(this);
            }
            getApplicationContext().registerReceiver(hotelSearchReceiver, hotelResultsFilter);
            hotelSearchRequest = svc.searchPerdiemHotel(checkOutDateCal, checkInDateCal, namesContaining, latitude, longitude,
                distanceId, distanceUnitId, 0, Const.HOTEL_RETRIEVE_COUNT,
                perDiemRate);
            hotelSearchReceiver.setRequest(hotelSearchRequest);
        }
    }
}
