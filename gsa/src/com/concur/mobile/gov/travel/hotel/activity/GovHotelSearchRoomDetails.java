package com.concur.mobile.gov.travel.hotel.activity;

import android.os.Bundle;
import android.util.Log;

import com.concur.mobile.core.travel.hotel.activity.HotelSearchRoomDetails;
import com.concur.mobile.core.util.Const;

public class GovHotelSearchRoomDetails extends HotelSearchRoomDetails {

    private static final String CLS_TAG = GovHotelSearchRoomDetails.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(Const.LOG_TAG, CLS_TAG + ".onCreate");
        super.onCreate(savedInstanceState);
    }

}
