package com.concur.mobile.gov.travel.car.activity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;

import com.concur.mobile.core.travel.car.activity.CarChoiceListItem;
import com.concur.mobile.core.travel.car.activity.CarSearchResults;
import com.concur.mobile.core.travel.car.data.CarChoice;
import com.concur.mobile.core.util.Const;
import com.concur.mobile.core.util.Flurry;

public class GovCarSearchResults extends CarSearchResults {

    private static final String CLS_TAG = GovCarSearchResults.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(Const.LOG_TAG, CLS_TAG + " in gov proj.");
    }

    @Override
    public void onItemClick(AdapterView<?> list, View view, int position, long id) {
        CarChoiceListItem listItem = (CarChoiceListItem) list.getItemAtPosition(position);
        if (listItem != null) {
            CarChoice carChoice = listItem.getCarChoice();
            if (carChoice != null) {
                Intent i = new Intent(this, GovCarSearchDetail.class);
                i.putExtra(Const.EXTRA_CAR_DETAIL_ID, carChoice.carId);
                i.putExtra(Const.EXTRA_TRAVEL_CLIQBOOK_TRIP_ID, cliqbookTripId);
                i.putExtra(Const.EXTRA_TRAVEL_CLIENT_LOCATOR, clientLocator);
                i.putExtra(Const.EXTRA_TRAVEL_RECORD_LOCATOR, recordLocator);
                Intent launchIntent = getIntent();
                if (launchIntent.hasExtra(Flurry.PARAM_NAME_BOOKED_FROM)) {
                    i.putExtra(Flurry.PARAM_NAME_BOOKED_FROM, launchIntent.getStringExtra(Flurry.PARAM_NAME_BOOKED_FROM));
                }
                startActivityForResult(i, 0);
            }
        }
    }

}
