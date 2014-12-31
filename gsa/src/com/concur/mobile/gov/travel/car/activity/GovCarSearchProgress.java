package com.concur.mobile.gov.travel.car.activity;

import java.util.Calendar;

import android.content.Intent;
import android.os.Bundle;

import com.concur.mobile.core.travel.car.activity.CarSearchProgress;
import com.concur.mobile.core.util.Const;
import com.concur.mobile.core.util.Flurry;

public class GovCarSearchProgress extends CarSearchProgress {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void initValues() {
        Intent intent = getIntent();

        // Setup our future intents while we are here
        noResultsIntent = (Intent) getIntent().clone();
        noResultsIntent.setClass(this, GovCarSearchNoResults.class);
        resultsIntent = (Intent) getIntent().clone();
        resultsIntent.setClass(this, GovCarSearchResults.class);
        if (intent.hasExtra(Flurry.PARAM_NAME_BOOKED_FROM)) {
            resultsIntent.putExtra(Flurry.PARAM_NAME_BOOKED_FROM, intent.getStringExtra(Flurry.PARAM_NAME_BOOKED_FROM));
        }
        location = intent.getStringExtra(Const.EXTRA_TRAVEL_LOCATION);
        latitude = intent.getStringExtra(Const.EXTRA_TRAVEL_LATITUDE);
        longitude = intent.getStringExtra(Const.EXTRA_TRAVEL_LONGITUDE);
        pickupDateTime = (Calendar) intent.getSerializableExtra(Const.EXTRA_TRAVEL_CAR_SEARCH_PICK_UP_CALENDAR);
        pickUpDate = intent.getStringExtra(Const.EXTRA_TRAVEL_CAR_SEARCH_PICK_UP);
        dropoffDateTime = (Calendar) intent.getSerializableExtra(Const.EXTRA_TRAVEL_CAR_SEARCH_DROP_OFF_CALENDAR);
        dropOffDate = intent.getStringExtra(Const.EXTRA_TRAVEL_CAR_SEARCH_DROP_OFF);
        carType = intent.getStringExtra(Const.EXTRA_TRAVEL_CAR_TYPE);
    }
}
