package com.concur.mobile.gov.travel.hotel.activity;

import android.os.Bundle;
import android.util.Log;

import com.concur.mobile.core.travel.hotel.activity.HotelSearchNoResults;
import com.concur.mobile.core.util.Const;

public class GovHotelSearchNoResults extends HotelSearchNoResults {

    private static final String CLS_TAG = GovHotelSearchNoResults.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.e(Const.LOG_TAG, CLS_TAG + ".onCreate: in gov proj.!");
    }
}
