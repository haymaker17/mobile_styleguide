package com.concur.mobile.gov.travel.hotel.activity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.concur.mobile.core.travel.hotel.activity.HotelSearchRooms;
import com.concur.mobile.core.util.Const;
import com.concur.mobile.core.util.Flurry;

public class GovHotelSearchRooms extends HotelSearchRooms {

    private static final String CLS_TAG = GovHotelSearchRooms.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(Const.LOG_TAG, CLS_TAG + " in gov proj.");
    }

    @Override
    protected Intent getHotelDetailIntent() {
        return new Intent(GovHotelSearchRooms.this, GovHotelSearchRoomDetails.class);
    }

    @Override
    protected Intent getReserveRoom() {
        Intent intent = new Intent(GovHotelSearchRooms.this, GovHotelReserveRoom.class);
        Intent launchIntent = getIntent();
        if (launchIntent.hasExtra(Flurry.PARAM_NAME_BOOKED_FROM)) {
            intent.putExtra(Flurry.PARAM_NAME_BOOKED_FROM,
                launchIntent.getStringExtra(Flurry.PARAM_NAME_BOOKED_FROM));
        }
        return intent;
    }
}
